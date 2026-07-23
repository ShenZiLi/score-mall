package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.score.mall.common.BusinessException;
import com.score.mall.common.PageResult;
import com.score.mall.common.ResultCode;
import com.score.mall.dto.CreateOrderDTO;
import com.score.mall.entity.Address;
import com.score.mall.entity.Order;
import com.score.mall.entity.Product;
import com.score.mall.entity.User;
import com.score.mall.mapper.OrderMapper;
import com.score.mall.mapper.ProductMapper;
import com.score.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final AddressService addressService;
    private final PointsService pointsService;

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, CreateOrderDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (user.getStatus() != 1) throw new BusinessException(ResultCode.USER_DISABLED);

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        if (product.getStatus() != 1) throw new BusinessException(ResultCode.PRODUCT_OFFLINE);

        int qty = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (qty < 1) throw new BusinessException(ResultCode.BAD_REQUEST, "数量必须大于0");
        if (product.getStock() < qty) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);

        // 等级折扣
        double discount = PointsService.levelDiscount(user.getLevel());

        // 计算所需积分
        int unitPoints = (int) Math.round(product.getPointsPrice() * discount);
        int totalPoints = unitPoints * qty;

        // 计算所需现金
        BigDecimal unitCash = product.getCashPrice() == null ? BigDecimal.ZERO : product.getCashPrice();
        BigDecimal totalCash = unitCash.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(discount)).setScale(2, RoundingMode.HALF_UP);

        // 校验支付方式
        String payType = dto.getPayType();
        if (!"POINTS_ONLY".equals(payType) && !"POINTS_CASH".equals(payType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "支付方式不合法");
        }
        if ("POINTS_ONLY".equals(payType) && totalCash.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该商品需积分+现金兑换");
        }

        // 校验积分余额
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < totalPoints) {
            throw new BusinessException(ResultCode.POINTS_NOT_ENOUGH);
        }

        // 收货地址（实物商品必填）
        String addressSnapshot = null;
        if (product.getCashPrice() == null || product.getCashPrice().compareTo(BigDecimal.ZERO) >= 0) {
            // 默认所有商品都是实物（虚拟商品也走地址可选流程）
            if (dto.getAddressId() != null) {
                Address addr = addressService.get(dto.getAddressId(), userId);
                addressSnapshot = AddressService.snapshot(addr);
            }
        }

        // 扣库存（乐观锁）
        int updated = deductStock(product.getId(), qty, product.getVersion());
        if (updated == 0) {
            // 重试一次
            Product fresh = productMapper.selectById(product.getId());
            if (fresh == null || fresh.getStock() < qty) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            updated = deductStock(product.getId(), qty, fresh.getVersion());
            if (updated == 0) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, "库存不足，请重试");
            }
        }

        // 扣积分（同事务）
        if (totalPoints > 0) {
            pointsService.changePoints(userId, -totalPoints, "EXCHANGE", null,
                    "兑换商品: " + product.getName() + " x" + qty);
        }

        // 增加销量
        Product salesUp = new Product();
        salesUp.setId(product.getId());
        salesUp.setSales(product.getSales() + qty);
        productMapper.updateById(salesUp);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductImage(product.getCoverImage());
        order.setQuantity(qty);
        order.setPointsUsed(totalPoints);
        order.setCashPaid(totalCash);
        order.setAddressSnapshot(addressSnapshot);
        order.setStatus(0);
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        return order;
    }

    /** 乐观锁扣库存 SQL：UPDATE product SET stock=stock-?, sales=sales+?, version=version+1 WHERE id=? AND version=? AND stock>=? */
    private int deductStock(Long productId, int qty, int version) {
        Product update = new Product();
        update.setId(productId);
        update.setVersion(version);
        // 用 MyBatis-Plus 乐观锁 + setSql 完成原子扣减
        return productMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getVersion, version)
                        .ge(Product::getStock, qty)
                        .setSql("stock = stock - " + qty + ", sales = sales + " + qty + ", version = version + 1"));
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /** App端订单列表 */
    public PageResult<Order> userList(Long userId, int page, int size, Integer status) {
        Page<Order> p = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId);
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getId);
        return PageResult.of(orderMapper.selectPage(p, wrapper));
    }

    public Order getUserOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /** 用户取消订单（仅待发货状态可取消，退回积分） */
    @Transactional(rollbackFor = Exception.class)
    public void cancelByUser(Long userId, Long orderId) {
        Order order = getUserOrder(userId, orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // 退积分
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            pointsService.changePoints(userId, order.getPointsUsed(), "REFUND", order.getId(),
                    "取消订单退回积分: " + order.getOrderNo());
        }
        // 还库存
        Product p = productMapper.selectById(order.getProductId());
        if (p != null) {
            productMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                            .eq(Product::getId, p.getId())
                            .setSql("stock = stock + " + order.getQuantity() + ", sales = sales - " + order.getQuantity()));
        }
        Order update = new Order();
        update.setId(orderId);
        update.setStatus(3);
        orderMapper.updateById(update);
    }

    /** 用户确认收货 */
    @Transactional(rollbackFor = Exception.class)
    public void confirmByUser(Long userId, Long orderId) {
        Order order = getUserOrder(userId, orderId);
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        Order update = new Order();
        update.setId(orderId);
        update.setStatus(2);
        update.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(update);

        // 完成订单后：按消费现金返积分（1元=1积分），仅当有现金支付
        if (order.getCashPaid() != null && order.getCashPaid().compareTo(BigDecimal.ZERO) > 0) {
            int earn = order.getCashPaid().intValue();
            if (earn > 0) {
                pointsService.changePoints(userId, earn, "ORDER_EARN", order.getId(),
                        "订单消费返积分: " + order.getOrderNo());
            }
        }
    }

    // ============= 后管 =============

    public PageResult<Order> adminList(int page, int size, String keyword, Integer status, Long userId) {
        Page<Order> p = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.like(Order::getOrderNo, keyword)
                    .or().like(Order::getProductName, keyword);
        }
        if (status != null) wrapper.eq(Order::getStatus, status);
        if (userId != null) wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getId);
        return PageResult.of(orderMapper.selectPage(p, wrapper));
    }

    /** 管理员发货 */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId, String shipNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (order.getStatus() != 0) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        Order update = new Order();
        update.setId(orderId);
        update.setStatus(1);
        update.setShipNo(shipNo);
        update.setShipTime(LocalDateTime.now());
        orderMapper.updateById(update);
    }

    /** 管理员备注 */
    public void updateRemark(Long orderId, String remark) {
        Order update = new Order();
        update.setId(orderId);
        update.setRemark(remark);
        orderMapper.updateById(update);
    }

    /** 管理员查询订单详情 */
    public Order adminGet(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        return order;
    }
}

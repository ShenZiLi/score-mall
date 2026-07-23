package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.score.mall.entity.Order;
import com.score.mall.entity.PointsLog;
import com.score.mall.entity.Product;
import com.score.mall.entity.User;
import com.score.mall.mapper.OrderMapper;
import com.score.mall.mapper.PointsLogMapper;
import com.score.mall.mapper.ProductMapper;
import com.score.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final PointsLogMapper pointsLogMapper;

    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("productCount", productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        data.put("orderCount", orderMapper.selectCount(null));
        data.put("pendingShipCount", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));

        // 今日新增用户
        LocalDate today = LocalDate.now();
        data.put("todayNewUsers", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, today.atStartOfDay())));

        // 今日订单
        data.put("todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, today.atStartOfDay())));

        // 累计发放积分
        Long totalEarn = pointsLogMapper.selectCount(new LambdaQueryWrapper<PointsLog>()
                .gt(PointsLog::getChangePoints, 0));
        data.put("totalPointsIssued", totalEarn);
        return data;
    }

    public Map<String, Object> orderStatusStat() {
        Map<String, Object> data = new HashMap<>();
        data.put("pendingShip", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        data.put("shipped", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        data.put("completed", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
        data.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        return data;
    }

    public Map<String, Object> pointsTrend(int days) {
        String startDate = LocalDate.now().minusDays(days).format(DateTimeFormatter.ISO_DATE);
        List<Map<String, Object>> trend = pointsLogMapper.dailyPointsTrend(startDate);
        Map<String, Object> data = new HashMap<>();
        data.put("trend", trend);
        data.put("typeStat", pointsLogMapper.pointsTypeStat(startDate));
        return data;
    }

    /** 兑换 TOP 商品 */
    public List<Product> topProducts(int limit) {
        Page<Product> page = new Page<>(1, limit);
        return productMapper.selectPage(page,
                new LambdaQueryWrapper<Product>()
                        .orderByDesc(Product::getSales))
                .getRecords();
    }
}

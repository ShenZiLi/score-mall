package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.score.mall.common.PageResult;
import com.score.mall.dto.ProductDTO;
import com.score.mall.entity.Product;
import com.score.mall.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /** App端首页-热门商品 */
    public List<Product> hotProducts(int limit) {
        Page<Product> page = new Page<>(1, limit);
        return productMapper.selectPage(page,
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getIsHot, 1)
                        .orderByAsc(Product::getSort)
                        .orderByDesc(Product::getId))
                .getRecords();
    }

    /** App端首页-推荐商品 */
    public List<Product> recommendProducts(int limit) {
        Page<Product> page = new Page<>(1, limit);
        return productMapper.selectPage(page,
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getIsRecommend, 1)
                        .orderByAsc(Product::getSort)
                        .orderByDesc(Product::getId))
                .getRecords();
    }

    /** App端商品列表（分页+搜索+分类） */
    public PageResult<Product> appList(int page, int size, Long categoryId, String keyword, String order) {
        Page<Product> p = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getSubtitle, keyword));
        }
        if ("sales".equals(order)) {
            wrapper.orderByDesc(Product::getSales);
        } else if ("points".equals(order)) {
            wrapper.orderByAsc(Product::getPointsPrice);
        } else {
            wrapper.orderByAsc(Product::getSort).orderByDesc(Product::getId);
        }
        return PageResult.of(productMapper.selectPage(p, wrapper));
    }

    public Product get(Long id) {
        return productMapper.selectById(id);
    }

    /** 后管分页查询 */
    public PageResult<Product> adminList(int page, int size, String keyword, Long categoryId, Integer status) {
        Page<Product> p = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null) wrapper.eq(Product::getCategoryId, categoryId);
        if (status != null) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getId);
        return PageResult.of(productMapper.selectPage(p, wrapper));
    }

    public void save(ProductDTO dto) {
        Product p = dto.toEntity();
        p.setSales(0);
        p.setVersion(0);
        productMapper.insert(p);
    }

    public void update(ProductDTO dto) {
        Product exists = productMapper.selectById(dto.getId());
        if (exists == null) return;
        Product p = dto.toEntity();
        productMapper.updateById(p);
    }

    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    /** 更新库存（管理员手动调整） */
    public void updateStock(Long id, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setStock(stock);
        productMapper.updateById(p);
    }
}

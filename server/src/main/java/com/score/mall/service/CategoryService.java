package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.entity.Category;
import com.score.mall.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> listAll(Boolean onlyEnabled) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(onlyEnabled)) {
            wrapper.eq(Category::getStatus, 1);
        }
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getId);
        return categoryMapper.selectList(wrapper);
    }

    public Category get(Long id) {
        return categoryMapper.selectById(id);
    }

    public void save(Category category) {
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSort() == null) category.setSort(0);
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}

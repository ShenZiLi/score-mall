package com.score.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.score.mall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}

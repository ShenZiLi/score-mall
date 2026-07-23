package com.score.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.score.mall.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

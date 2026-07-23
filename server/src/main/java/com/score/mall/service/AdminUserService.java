package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.score.mall.common.PageResult;
import com.score.mall.entity.PointsLog;
import com.score.mall.entity.User;
import com.score.mall.mapper.PointsLogMapper;
import com.score.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final PointsService pointsService;

    public PageResult<User> list(int page, int size, String keyword, Integer status) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getPhone, keyword).or().like(User::getNickname, keyword);
        }
        if (status != null) wrapper.eq(User::getStatus, status);
        wrapper.orderByDesc(User::getId);
        IPage<User> result = userMapper.selectPage(p, wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(result);
    }

    public void updateStatus(Long userId, int status) {
        User u = new User();
        u.setId(userId);
        u.setStatus(status);
        userMapper.updateById(u);
    }

    public void adjustPoints(Long userId, int points, String remark) {
        pointsService.changePoints(userId, points, "ADMIN", null,
                remark == null ? "管理员调整积分" : remark);
    }

    public PageResult<PointsLog> pointsLogs(Long userId, int page, int size) {
        Page<PointsLog> p = new Page<>(page, size);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(PointsLog::getUserId, userId);
        wrapper.orderByDesc(PointsLog::getId);
        return PageResult.of(pointsLogMapper.selectPage(p, wrapper));
    }
}

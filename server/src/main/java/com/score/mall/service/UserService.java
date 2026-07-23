package com.score.mall.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.common.BusinessException;
import com.score.mall.common.ResultCode;
import com.score.mall.dto.ChangePasswordDTO;
import com.score.mall.dto.LoginDTO;
import com.score.mall.dto.RegisterDTO;
import com.score.mall.entity.User;
import com.score.mall.mapper.UserMapper;
import com.score.mall.security.JwtUtil;
import com.score.mall.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PointsService pointsService;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? "用户" + IdUtil.simpleUUID().substring(0, 6) : dto.getNickname());
        user.setPoints(0);
        user.setTotalPoints(0);
        user.setLevel(0);
        user.setStatus(1);
        userMapper.insert(user);
        // 注册赠送积分
        if (PointsService.REGISTER_GIFT > 0) {
            pointsService.changePoints(user.getId(), PointsService.REGISTER_GIFT, "ADMIN", null, "新用户注册赠送");
        }
        return buildLoginResult(user);
    }

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return buildLoginResult(user);
    }

    private Map<String, Object> buildLoginResult(User user) {
        String token = jwtUtil.generateAppToken(user.getId(), user.getPhone());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", toVO(user));
        return data;
    }

    public Map<String, Object> getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    private Map<String, Object> toVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("phone", user.getPhone());
        vo.put("nickname", user.getNickname());
        vo.put("avatar", user.getAvatar());
        vo.put("points", user.getPoints());
        vo.put("totalPoints", user.getTotalPoints());
        vo.put("level", user.getLevel());
        vo.put("levelName", PointsService.levelName(user.getLevel()));
        vo.put("nextLevelPoints", nextLevelPoints(user.getTotalPoints()));
        return vo;
    }

    private Integer nextLevelPoints(int totalPoints) {
        if (totalPoints < 1000) return 1000;
        if (totalPoints < 5000) return 5000;
        if (totalPoints < 10000) return 10000;
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, String nickname, String avatar) {
        User user = new User();
        user.setId(userId);
        if (StrUtil.isNotBlank(nickname)) user.setNickname(nickname);
        if (StrUtil.isNotBlank(avatar)) user.setAvatar(avatar);
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}

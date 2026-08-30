package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.common.BusinessException;
import com.score.mall.common.ResultCode;
import com.score.mall.dto.LoginDTO;
import com.score.mall.entity.Admin;
import com.score.mall.mapper.AdminMapper;
import com.score.mall.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(LoginDTO dto) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, dto.getUsername()));
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> info = new HashMap<>();
        info.put("id", admin.getId());
        info.put("username", admin.getUsername());
        info.put("nickname", admin.getNickname());
        info.put("avatar", admin.getAvatar());
        info.put("role", admin.getRole());
        data.put("adminInfo", info);
        return data;
    }

    public Map<String, Object> currentInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) throw new BusinessException(ResultCode.ADMIN_NOT_FOUND);
        Map<String, Object> info = new HashMap<>();
        info.put("id", admin.getId());
        info.put("username", admin.getUsername());
        info.put("nickname", admin.getNickname());
        info.put("avatar", admin.getAvatar());
        info.put("role", admin.getRole());
        return info;
    }
}

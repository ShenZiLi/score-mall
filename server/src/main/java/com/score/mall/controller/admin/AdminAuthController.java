package com.score.mall.controller.admin;

import com.score.mall.common.Result;
import com.score.mall.dto.LoginDTO;
import com.score.mall.security.LoginContext;
import com.score.mall.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(adminService.login(dto));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(adminService.currentInfo(LoginContext.get().currentAdminId()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

package com.score.mall.controller.app;

import com.score.mall.common.Result;
import com.score.mall.dto.ChangePasswordDTO;
import com.score.mall.dto.LoginDTO;
import com.score.mall.dto.RegisterDTO;
import com.score.mall.security.LoginContext;
import com.score.mall.service.PointsService;
import com.score.mall.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final UserService userService;
    private final PointsService pointsService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = LoginContext.get().currentUserId();
        Map<String, Object> data = userService.getCurrentUserInfo(userId);
        data.put("signedToday", pointsService.signedToday(userId));
        return Result.success(data);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestParam(required = false) String nickname,
                                      @RequestParam(required = false) String avatar) {
        userService.updateProfile(LoginContext.get().currentUserId(), nickname, avatar);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(LoginContext.get().currentUserId(), dto);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

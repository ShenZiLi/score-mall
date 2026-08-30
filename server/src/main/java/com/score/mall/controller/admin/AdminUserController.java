package com.score.mall.controller.admin;

import com.score.mall.common.PageResult;
import com.score.mall.common.Result;
import com.score.mall.entity.User;
import com.score.mall.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<User>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(adminUserService.list(page, size, keyword, status));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam int status) {
        adminUserService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/points")
    public Result<Void> adjustPoints(@PathVariable Long id, @RequestParam int points,
                                     @RequestParam(required = false) String remark) {
        adminUserService.adjustPoints(id, points, remark);
        return Result.success();
    }
}

package com.score.mall.controller.admin;

import com.score.mall.common.PageResult;
import com.score.mall.common.Result;
import com.score.mall.entity.PointsLog;
import com.score.mall.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {

    private final AdminUserService adminUserService;

    @GetMapping("/logs")
    public Result<PageResult<PointsLog>> logs(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) Long userId) {
        return Result.success(adminUserService.pointsLogs(userId, page, size));
    }
}

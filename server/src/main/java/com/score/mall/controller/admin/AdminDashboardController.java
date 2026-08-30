package com.score.mall.controller.admin;

import com.score.mall.common.Result;
import com.score.mall.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(dashboardService.overview());
    }

    @GetMapping("/order-status")
    public Result<Map<String, Object>> orderStatus() {
        return Result.success(dashboardService.orderStatusStat());
    }

    @GetMapping("/points-trend")
    public Result<Map<String, Object>> pointsTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.pointsTrend(days));
    }

    @GetMapping("/top-products")
    public Result<Object> topProducts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(dashboardService.topProducts(limit));
    }
}

package com.score.mall.controller.admin;

import com.score.mall.common.PageResult;
import com.score.mall.common.Result;
import com.score.mall.entity.Order;
import com.score.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<Order>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) Long userId) {
        return Result.success(orderService.adminList(page, size, keyword, status, userId));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.adminGet(id));
    }

    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @RequestParam(required = false) String shipNo) {
        orderService.ship(id, shipNo);
        return Result.success();
    }

    @PutMapping("/{id}/remark")
    public Result<Void> remark(@PathVariable Long id, @RequestParam String remark) {
        orderService.updateRemark(id, remark);
        return Result.success();
    }
}

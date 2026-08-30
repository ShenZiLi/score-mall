package com.score.mall.controller.app;

import com.score.mall.common.Result;
import com.score.mall.dto.CreateOrderDTO;
import com.score.mall.entity.Order;
import com.score.mall.security.LoginContext;
import com.score.mall.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/orders")
@RequiredArgsConstructor
public class AppOrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Order> create(@Valid @RequestBody CreateOrderDTO dto) {
        Long userId = LoginContext.get().currentUserId();
        return Result.success(orderService.createOrder(userId, dto));
    }

    @GetMapping
    public Result<Object> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Integer status) {
        return Result.success(orderService.userList(LoginContext.get().currentUserId(), page, size, status));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.getUserOrder(LoginContext.get().currentUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelByUser(LoginContext.get().currentUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirmByUser(LoginContext.get().currentUserId(), id);
        return Result.success();
    }
}

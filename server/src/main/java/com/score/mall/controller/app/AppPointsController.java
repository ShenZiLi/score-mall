package com.score.mall.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.common.PageResult;
import com.score.mall.common.Result;
import com.score.mall.entity.PointsLog;
import com.score.mall.entity.SignIn;
import com.score.mall.mapper.PointsLogMapper;
import com.score.mall.mapper.SignInMapper;
import com.score.mall.security.LoginContext;
import com.score.mall.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/points")
@RequiredArgsConstructor
public class AppPointsController {

    private final PointsService pointsService;
    private final PointsLogMapper pointsLogMapper;
    private final SignInMapper signInMapper;

    /** 每日签到 */
    @PostMapping("/sign")
    public Result<Map<String, Object>> sign() {
        Long userId = LoginContext.get().currentUserId();
        int points = pointsService.signIn(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
        data.put("signDate", LocalDate.now());
        return Result.success("签到成功", data);
    }

    /** 签到状态 */
    @GetMapping("/sign/status")
    public Result<Map<String, Object>> signStatus() {
        Long userId = LoginContext.get().currentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("signedToday", pointsService.signedToday(userId));
        // 近30天签到记录
        LocalDate start = LocalDate.now().minusDays(30);
        List<SignIn> records = signInMapper.selectList(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUserId, userId)
                .ge(SignIn::getSignDate, start)
                .orderByDesc(SignIn::getSignDate));
        data.put("recent", records);
        return Result.success(data);
    }

    /** 积分流水 */
    @GetMapping("/logs")
    public Result<PageResult<PointsLog>> logs(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Long userId = LoginContext.get().currentUserId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PointsLog> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        PageResult<PointsLog> result = PageResult.of(pointsLogMapper.selectPage(p,
                new LambdaQueryWrapper<PointsLog>().eq(PointsLog::getUserId, userId).orderByDesc(PointsLog::getId)));
        return Result.success(result);
    }
}

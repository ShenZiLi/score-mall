package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.common.BusinessException;
import com.score.mall.common.ResultCode;
import com.score.mall.entity.PointsLog;
import com.score.mall.entity.SignIn;
import com.score.mall.entity.User;
import com.score.mall.mapper.PointsLogMapper;
import com.score.mall.mapper.SignInMapper;
import com.score.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final SignInMapper signInMapper;

    public static final int SIGN_POINTS = 5;
    public static final int REGISTER_GIFT = 100;

    /**
     * 改变用户积分（事务内调用，加锁）
     * @param userId 用户ID
     * @param change 变动积分（正数增加，负数扣减）
     * @param type   类型 SIGN/ORDER_EARN/EXCHANGE/REFUND/ADMIN
     * @param relatedId 关联ID
     * @param remark 备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePoints(Long userId, int change, String type, Long relatedId, String remark) {
        // 行锁查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        int before = user.getPoints() == null ? 0 : user.getPoints();
        int after = before + change;
        if (after < 0) {
            throw new BusinessException(ResultCode.POINTS_NOT_ENOUGH);
        }
        user.setPoints(after);
        if (change > 0) {
            // 累计积分增加，触发等级更新
            int total = (user.getTotalPoints() == null ? 0 : user.getTotalPoints()) + change;
            user.setTotalPoints(total);
            user.setLevel(calcLevel(total));
        }
        userMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangePoints(change);
        log.setBeforePoints(before);
        log.setAfterPoints(after);
        log.setType(type);
        log.setRelatedId(relatedId);
        log.setRemark(remark);
        pointsLogMapper.insert(log);
    }

    /** 计算会员等级 0普通 1银卡 2金卡 3钻石 */
    public static int calcLevel(int totalPoints) {
        if (totalPoints >= 10000) return 3;
        if (totalPoints >= 5000) return 2;
        if (totalPoints >= 1000) return 1;
        return 0;
    }

    /** 等级对应的兑换折扣（0.85 ~ 1.0） */
    public static double levelDiscount(int level) {
        return switch (level) {
            case 3 -> 0.85;
            case 2 -> 0.90;
            case 1 -> 0.95;
            default -> 1.0;
        };
    }

    public static String levelName(int level) {
        return switch (level) {
            case 3 -> "钻石会员";
            case 2 -> "金卡会员";
            case 1 -> "银卡会员";
            default -> "普通会员";
        };
    }

    /** 每日签到 */
    @Transactional(rollbackFor = Exception.class)
    public int signIn(Long userId) {
        LocalDate today = LocalDate.now();
        // 检查今日是否已签到
        Long count = signInMapper.selectCount(
                new LambdaQueryWrapper<SignIn>()
                        .eq(SignIn::getUserId, userId)
                        .eq(SignIn::getSignDate, today));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.SIGN_ALREADY);
        }
        SignIn sign = new SignIn();
        sign.setUserId(userId);
        sign.setSignDate(today);
        sign.setPoints(SIGN_POINTS);
        signInMapper.insert(sign);

        changePoints(userId, SIGN_POINTS, "SIGN", sign.getId(), "每日签到");
        return SIGN_POINTS;
    }

    public boolean signedToday(Long userId) {
        Long count = signInMapper.selectCount(
                new LambdaQueryWrapper<SignIn>()
                        .eq(SignIn::getUserId, userId)
                        .eq(SignIn::getSignDate, LocalDate.now()));
        return count != null && count > 0;
    }
}

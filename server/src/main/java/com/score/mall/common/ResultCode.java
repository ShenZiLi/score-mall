package com.score.mall.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误 1xxxx
    USER_EXISTS(10001, "用户已存在"),
    USER_NOT_FOUND(10002, "用户不存在"),
    PASSWORD_ERROR(10003, "密码错误"),
    USER_DISABLED(10004, "账号已被禁用"),
    OLD_PASSWORD_ERROR(10005, "原密码错误"),

    PRODUCT_NOT_FOUND(20001, "商品不存在"),
    PRODUCT_OFFLINE(20002, "商品已下架"),
    STOCK_NOT_ENOUGH(20003, "库存不足"),

    POINTS_NOT_ENOUGH(30001, "积分不足"),
    SIGN_ALREADY(30002, "今日已签到"),

    ORDER_NOT_FOUND(40001, "订单不存在"),
    ORDER_STATUS_ERROR(40002, "订单状态不允许该操作"),
    ORDER_NOT_YOURS(40003, "无权操作该订单"),

    ADDRESS_NOT_FOUND(50001, "地址不存在"),
    ADDRESS_LIMIT(50002, "地址数量超过限制"),

    FILE_UPLOAD_ERROR(60001, "文件上传失败"),

    ADMIN_EXISTS(70001, "管理员已存在"),
    ADMIN_NOT_FOUND(70002, "管理员不存在");

    private final int code;
    private final String message;
}

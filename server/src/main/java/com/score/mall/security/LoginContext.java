package com.score.mall.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;

/**
 * 当前登录上下文（存放在 ThreadLocal）
 */
@Getter
@Setter
public class LoginContext {
    private static final ThreadLocal<LoginContext> HOLDER = new ThreadLocal<>();

    /** APP 用户ID */
    private Long userId;
    /** APP 手机号 */
    private String phone;
    /** 管理员ID */
    private Long adminId;
    /** 管理员用户名 */
    private String username;
    /** 管理员角色 */
    private String role;
    /** token 类型 APP / ADMIN */
    private String type;

    public static LoginContext of(Claims c) {
        LoginContext ctx = new LoginContext();
        ctx.setType(c.get("type", String.class));
        Object uid = c.get("uid");
        if (uid != null) ctx.setUserId(Long.valueOf(uid.toString()));
        ctx.setPhone(c.get("phone", String.class));
        Object aid = c.get("aid");
        if (aid != null) ctx.setAdminId(Long.valueOf(aid.toString()));
        ctx.setUsername(c.get("username", String.class));
        ctx.setRole(c.get("role", String.class));
        return ctx;
    }

    public static void set(LoginContext ctx) {
        HOLDER.set(ctx);
    }

    public static LoginContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public boolean isApp() {
        return "APP".equals(type);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(type);
    }

    public Long currentUserId() {
        if (userId == null) {
            throw new IllegalStateException("当前非APP用户上下文");
        }
        return userId;
    }

    public Long currentAdminId() {
        if (adminId == null) {
            throw new IllegalStateException("当前非管理员上下文");
        }
        return adminId;
    }
}

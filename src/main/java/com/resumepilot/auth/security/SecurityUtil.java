package com.resumepilot.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户工具类
 *
 * 用法：String email = SecurityUtil.getCurrentUser();
 *
 * 原理：
 *   JwtAuthFilter 验证 token 后，把 email 放进了 SecurityContext
 *   这个类从 SecurityContext 里取出来
 */
public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * 获取当前登录用户的邮箱
     *
     * @return 用户邮箱，未登录时返回 null
     */
    public static String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}

package com.resumepilot.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.resumepilot.auth.entity.User;
import com.resumepilot.auth.mapper.UserMapper;
import com.resumepilot.auth.security.SecurityUtil;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 * 提供"当前登录用户"的快捷获取方法
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 获取当前登录用户的 ID
     * 从 SecurityContext 取邮箱 → 查库取 ID
     *
     * 为什么不直接在 JWT 里存 userId？
     *   也可以，但邮箱更直观，调试时一眼看出是谁
     *   查一次 Redis/DB 的性能损耗可以忽略
     */
    public Long getCurrentUserId() {
        String email = SecurityUtil.getCurrentUser();
        if (email == null) return null;
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        return user == null ? null : user.getId();
    }
}

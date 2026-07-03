package com.resumepilot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功的响应
 * 前端拿到 token 后存到 localStorage，后续请求带上
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
}

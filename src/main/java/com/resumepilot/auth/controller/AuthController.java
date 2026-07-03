package com.resumepilot.auth.controller;

import com.resumepilot.auth.dto.LoginRequest;
import com.resumepilot.auth.dto.LoginResponse;
import com.resumepilot.auth.dto.RegisterRequest;
import com.resumepilot.auth.service.AuthService;
import com.resumepilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 *
 * POST /auth/send-code   → 发验证码（不需要认证）
 * POST /auth/register    → 注册（不需要认证）
 * POST /auth/login       → 登录（不需要认证）
 *
 * 这三个接口在 SecurityConfig 里设了 permitAll()，不需要 JWT
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     * Body: { "email": "xxx@qq.com" }
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestParam String email) {
        authService.sendCode(email);
        return Result.success("验证码已发送", null);
    }

    /**
     * 注册
     * Body: { "email": "xxx@qq.com", "password": "123456", "code": "123456" }
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 登录
     * Body: { "email": "xxx@qq.com", "password": "123456" }
     * 返回: { "token": "eyJhbGciOi...", "email": "xxx@qq.com" }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }
}

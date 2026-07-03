package com.resumepilot.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resumepilot.auth.dto.LoginRequest;
import com.resumepilot.auth.dto.LoginResponse;
import com.resumepilot.auth.dto.RegisterRequest;
import com.resumepilot.auth.entity.User;
import com.resumepilot.auth.mapper.UserMapper;
import com.resumepilot.auth.security.JwtUtil;
import com.resumepilot.common.exception.BusinessException;
import com.resumepilot.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 *
 * 三个核心方法：
 *   sendCode()   → 生成验证码 → 存 Redis → 发邮件
 *   register()   → 校验验证码 → 加密密码 → 存库
 *   login()      → 校验密码 → 生成 JWT
 *
 * 为什么验证码存 Redis 不存数据库？
 *   - 验证码是临时的（5 分钟过期），Redis 自带 TTL 过期机制
 *   - 读写频率高（发码 + 验码），Redis 比 MySQL 快 100 倍
 *   - 不需要持久化，丢了重新发就行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String CODE_KEY_PREFIX = "auth:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    /**
     * 发送验证码
     * 1. 生成 6 位随机数
     * 2. 存入 Redis，key = auth:code:xxx@qq.com，5 分钟过期
     * 3. 发邮件
     */
    public void sendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(
                CODE_KEY_PREFIX + email, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2832005374@qq.com"); // 发件人
        message.setTo(email);
        message.setSubject("ResumePilot 验证码");
        message.setText("您的验证码是：" + code + "，" + CODE_EXPIRE_MINUTES + " 分钟内有效。");
        mailSender.send(message);

        log.info("验证码已发送，email={}", email);
    }

    /**
     * 注册
     * 1. 校验验证码（从 Redis 取出来比对）
     * 2. 检查邮箱是否已注册
     * 3. 密码 BCrypt 加密
     * 4. 存库
     */
    public void register(RegisterRequest request) {
        // 校验验证码
        String cachedCode = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + request.getEmail());
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 检查邮箱是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该邮箱已注册");
        }

        // 存库
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);

        // 注册成功，删除验证码
        redisTemplate.delete(CODE_KEY_PREFIX + request.getEmail());

        log.info("用户注册成功，email={}", request.getEmail());
    }

    /**
     * 登录
     * 1. 查用户
     * 2. 验密码
     * 3. 生成 JWT
     */
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "邮箱未注册");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "密码错误");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("用户登录成功，email={}", user.getEmail());
        return new LoginResponse(token, user.getEmail());
    }
}

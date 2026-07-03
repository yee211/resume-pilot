package com.resumepilot.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 *
 * JWT 组成：Header.Payload.Signature（三段用 . 分隔）
 *   Header  = { "alg": "HS256" }           → 算法
 *   Payload = { "sub": "email", "exp": ... } → 用户信息 + 过期时间
 *   Signature = HMAC-SHA256(header+payload, secret) → 签名防篡改
 *
 * 为什么用 JWT 不用 Session？
 *   - 无状态：服务端不存登录状态，天然支持分布式部署
 *   - 跨服务：一个 token 可以访问多个微服务
 *   - 移动端友好：不像 Cookie 有跨域限制
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        // 用 secret 字符串生成 HMAC-SHA256 密钥
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 Token
     * subject = 用户邮箱（唯一标识）
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)                              // 载荷：用户标识
                .issuedAt(new Date())                        // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
                .signWith(key)                               // 签名
                .compact();
    }

    /**
     * 从 Token 中提取邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效（未过期 + 签名正确）
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

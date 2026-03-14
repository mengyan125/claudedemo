package com.feedback.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 token 的生成、解析和验证
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT 过期时间（毫秒） */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成 JWT token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @return 生成的 token 字符串
     */
    public String generateToken(Long userId, String username, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 解析 token，返回 Claims
     *
     * @param token JWT token
     * @return 解析后的 Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 token 中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从 token 中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 token 中获取用户类型
     *
     * @param token JWT token
     * @return 用户类型
     */
    public String getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType", String.class);
    }

    /**
     * 判断 token 是否已过期
     *
     * @param token JWT token
     * @return true 表示已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 token 是否有效（签名正确且未过期）
     *
     * @param token JWT token
     * @return true 表示有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

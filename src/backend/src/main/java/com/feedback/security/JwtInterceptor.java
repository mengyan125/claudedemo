package com.feedback.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.common.result.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 拦截器
 * 拦截需要认证的请求，验证 token 并设置用户上下文
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 请求预处理：验证 token 并设置用户上下文
     *
     * @return true 放行，false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        // 检查 Authorization 头是否存在且以 Bearer 开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        // 提取 token 并验证
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response);
            return false;
        }

        // 解析用户信息并设置到上下文
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String userType = jwtUtil.getUserType(token);

        UserContext.UserInfo userInfo = new UserContext.UserInfo(
                userId, username, null, userType, new ArrayList<>()
        );
        UserContext.setCurrentUser(userInfo);

        return true;
    }

    /**
     * 请求完成后清理用户上下文，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    /**
     * 写入 401 未授权响应
     *
     * @param response HTTP 响应对象
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.fail(401, "未登录或登录已过期");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

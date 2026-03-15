package com.feedback.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.common.result.Result;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 角色权限拦截器
 * 配合 @RequiresRole 注解实现接口级别的角色权限控制
 * 必须在 JwtInterceptor 之后执行（依赖 UserContext 已设置）
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 方法级注解优先，类级注解兜底
        RequiresRole annotation = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        }

        // 无注解，直接放行
        if (annotation == null) {
            return true;
        }

        UserContext.UserInfo user = UserContext.getCurrentUser();

        // userType 匹配检查
        if (StringUtils.hasText(annotation.userType())
                && annotation.userType().equals(user.getUserType())) {
            return true;
        }

        // 角色编码匹配检查（满足其一即可）
        String[] requiredRoles = annotation.value();
        if (requiredRoles.length > 0 && user.getRoles() != null) {
            for (String role : requiredRoles) {
                if (user.getRoles().contains(role)) {
                    return true;
                }
            }
        }

        writeForbidden(response);
        return false;
    }

    /**
     * 写入 403 权限不足响应
     */
    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.fail(403, "权限不足");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

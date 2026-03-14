package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.LoginDTO;
import com.feedback.model.vo.LoginVO;
import com.feedback.model.vo.UserInfoVO;
import com.feedback.security.UserContext;
import com.feedback.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * 处理用户登录、登出等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录响应（包含token和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.ok(loginVO);
    }

    /**
     * 获取当前登录用户信息
     * 根据JWT中的用户ID查询并返回用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        UserInfoVO userInfo = authService.getCurrentUserInfo(userId);
        return Result.ok(userInfo);
    }

    /**
     * 用户登出
     * 前端清除 token 即可，后端无需额外操作
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}

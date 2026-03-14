package com.feedback.service;

import com.feedback.model.dto.LoginDTO;
import com.feedback.model.vo.LoginVO;

/**
 * 认证服务接口
 * 提供用户登录等认证相关功能
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录响应（包含token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);
}

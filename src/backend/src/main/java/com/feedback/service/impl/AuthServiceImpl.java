package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.SysRoleMapper;
import com.feedback.mapper.SysUserMapper;
import com.feedback.mapper.SysUserRoleMapper;
import com.feedback.model.dto.LoginDTO;
import com.feedback.model.entity.SysRole;
import com.feedback.model.entity.SysUser;
import com.feedback.model.entity.SysUserRole;
import com.feedback.model.vo.LoginVO;
import com.feedback.model.vo.UserInfoVO;
import com.feedback.security.JwtUtil;
import com.feedback.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 处理用户登录认证逻辑
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /** 密码加密编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 4. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());

        // 5. 查询用户角色
        List<String> roles = getUserRoles(user.getId());

        // 6. 构建登录响应
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .roles(roles)
                .schoolName(user.getSchoolName())
                .build();

        return new LoginVO(token, userInfo);
    }

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        List<String> roles = getUserRoles(user.getId());
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .roles(roles)
                .schoolName(user.getSchoolName())
                .build();
    }

    /**
     * 查询用户的角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    private List<String> getUserRoles(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(urWrapper);

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询角色详情
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);

        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}

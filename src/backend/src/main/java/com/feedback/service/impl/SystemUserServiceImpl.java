package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feedback.common.exception.BusinessException;
import com.feedback.common.result.PageResult;
import com.feedback.mapper.SysRoleMapper;
import com.feedback.mapper.SysUserMapper;
import com.feedback.mapper.SysUserRoleMapper;
import com.feedback.model.dto.CreateUserDTO;
import com.feedback.model.dto.UpdateUserDTO;
import com.feedback.model.dto.UpdateUserStatusDTO;
import com.feedback.model.entity.SysRole;
import com.feedback.model.entity.SysUser;
import com.feedback.model.entity.SysUserRole;
import com.feedback.model.vo.UserItemVO;
import com.feedback.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 * 处理用户的增删改查及状态管理业务逻辑
 */
@Service
public class SystemUserServiceImpl implements SystemUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    /** 密码加密编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 日期时间格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<UserItemVO> getUserList(String keyword, Integer pageNum, Integer pageSize) {
        // 1. 构建分页查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword));
        }
        wrapper.orderByDesc(SysUser::getId);

        // 2. 执行分页查询
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        // 3. 转换为VO列表
        List<UserItemVO> voList = result.getRecords().stream()
                .map(this::convertToUserItemVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserDTO dto) {
        // 1. 校验用户类型，不允许创建admin类型用户
        if ("admin".equals(dto.getUserType())) {
            throw new BusinessException("不允许创建管理员类型用户");
        }

        // 2. 检查用户名是否已存在
        checkUsernameUnique(dto.getUsername(), null);

        // 3. 判断是否为管理员角色（前端传 role_admin/category_admin，实际创建 teacher 并分配对应角色）
        String roleCode = null;
        if ("role_admin".equals(dto.getUserType())) {
            roleCode = "ROLE_ADMIN";
        } else if ("category_admin".equals(dto.getUserType())) {
            roleCode = "CATEGORY_ADMIN";
        }
        String actualUserType = roleCode != null ? "teacher" : dto.getUserType();

        // 4. 构建用户实体并保存
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserType(actualUserType);
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        // 5. 管理员角色自动分配对应角色
        if (roleCode != null) {
            assignRole(user.getId(), roleCode);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateUsers(List<CreateUserDTO> dtoList) {
        for (CreateUserDTO dto : dtoList) {
            createUser(dto);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UpdateUserDTO dto) {
        // 1. 查询用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 如果修改了用户名，检查唯一性
        if (StringUtils.hasText(dto.getUsername())) {
            checkUsernameUnique(dto.getUsername(), id);
            user.setUsername(dto.getUsername());
        }

        // 3. 更新其他字段
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (StringUtils.hasText(dto.getUserType())) {
            // 判断是否为管理员角色类型，与 createUser 保持一致
            String newRoleCode = null;
            if ("role_admin".equals(dto.getUserType())) {
                newRoleCode = "ROLE_ADMIN";
            } else if ("category_admin".equals(dto.getUserType())) {
                newRoleCode = "CATEGORY_ADMIN";
            }
            String actualUserType = newRoleCode != null ? "teacher" : dto.getUserType();
            user.setUserType(actualUserType);

            // 清除旧的管理员角色
            removeRoleByCode(id, "ROLE_ADMIN");
            removeRoleByCode(id, "CATEGORY_ADMIN");

            // 分配新角色
            if (newRoleCode != null) {
                assignRole(id, newRoleCode);
            }
        }

        sysUserMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 逻辑删除（@TableLogic注解自动处理）
        sysUserMapper.deleteById(id);
    }

    @Override
    public void updateUserStatus(Long id, UpdateUserStatusDTO dto) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(dto.getStatus());
        sysUserMapper.updateById(user);
    }

    /**
     * 检查用户名唯一性
     *
     * @param username  用户名
     * @param excludeId 排除的用户ID（更新时排除自身）
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        // 使用自定义 SQL 查询，绕过 @TableLogic 的自动 deleted=0 过滤
        // 因为数据库唯一索引不区分 deleted 字段
        Long count = sysUserMapper.countByUsernameIncludeDeleted(username, excludeId);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    /**
     * 为用户分配指定角色
     *
     * @param userId   用户ID
     * @param roleCode 角色编码
     */
    private void assignRole(Long userId, String roleCode) {
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, roleCode);
        SysRole role = sysRoleMapper.selectOne(roleWrapper);
        if (role != null) {
            // 检查是否已存在，避免重复
            LambdaQueryWrapper<SysUserRole> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, role.getId());
            if (sysUserRoleMapper.selectCount(checkWrapper) == 0) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(role.getId());
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    /**
     * 移除用户的指定角色
     *
     * @param userId   用户ID
     * @param roleCode 角色编码
     */
    private void removeRoleByCode(Long userId, String roleCode) {
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, roleCode);
        SysRole role = sysRoleMapper.selectOne(roleWrapper);
        if (role != null) {
            LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUserRole::getUserId, userId)
                   .eq(SysUserRole::getRoleId, role.getId());
            sysUserRoleMapper.delete(wrapper);
        }
    }

    /**
     * 将用户实体转换为列表项VO
     *
     * @param user 用户实体
     * @return 用户列表项VO
     */
    private UserItemVO convertToUserItemVO(SysUser user) {
        List<String> roles = getUserRoles(user.getId());
        String createTimeStr = user.getCreateTime() != null
                ? user.getCreateTime().format(DATE_FORMATTER)
                : "";

        // 角色管理员特殊显示，类别管理员保持教师类型（角色列已展示）
        String displayUserType = user.getUserType();
        if (roles.contains("ROLE_ADMIN")) {
            displayUserType = "role_admin";
        }

        return UserItemVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(displayUserType)
                .roles(roles)
                .status(user.getStatus())
                .createTime(createTimeStr)
                .build();
    }

    /**
     * 查询用户的角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    private List<String> getUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(urWrapper);

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);

        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}

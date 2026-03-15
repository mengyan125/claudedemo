package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.mapper.FbCategoryAdminMapper;
import com.feedback.mapper.FbCategoryMapper;
import com.feedback.mapper.SysRoleMapper;
import com.feedback.mapper.SysUserMapper;
import com.feedback.mapper.SysUserRoleMapper;
import com.feedback.model.entity.FbCategory;
import com.feedback.model.entity.FbCategoryAdmin;
import com.feedback.model.entity.SysRole;
import com.feedback.model.entity.SysUser;
import com.feedback.model.entity.SysUserRole;
import com.feedback.model.vo.CategoryPermissionVO;
import com.feedback.model.vo.RoleItemVO;
import com.feedback.model.vo.UserRoleItemVO;
import com.feedback.service.SystemPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统权限服务实现类
 */
@Service
public class SystemPermissionServiceImpl implements SystemPermissionService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private FbCategoryMapper fbCategoryMapper;

    @Autowired
    private FbCategoryAdminMapper fbCategoryAdminMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<RoleItemVO> getRoleList() {
        List<SysRole> roles = sysRoleMapper.selectList(null);
        return roles.stream()
                .map(this::convertToRoleItemVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryPermissionVO> getCategoryPermissions() {
        List<FbCategory> categories = fbCategoryMapper.selectList(null);
        return categories.stream()
                .map(this::buildCategoryPermissionVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateCategoryPermission(Long categoryId, List<Long> adminIds) {
        // 记录修改前该类别的管理员，用于后续清理角色
        List<Long> oldAdminIds = getAdminIdsByCategoryId(categoryId);
        // 先删除该类别的所有管理员关联
        deleteExistingPermissions(categoryId);
        // 批量插入新的管理员关联
        insertNewPermissions(categoryId, adminIds);
        // 自动维护 CATEGORY_ADMIN 角色
        syncCategoryAdminRole(oldAdminIds, adminIds);
    }

    @Override
    public List<UserRoleItemVO> getRoleUsers(Long roleId) {
        // 查询角色下的用户ID列表
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(wrapper);
        List<Long> userIds = userRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询用户信息
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        return users.stream()
                .map(this::convertToUserRoleItemVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        // 检查是否已存在该关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        Long count = sysUserRoleMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }
        // 插入新关联
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    @Transactional
    public void revokeRole(Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        sysUserRoleMapper.delete(wrapper);
    }

    // ==================== 私有方法 ====================

    /** 将用户实体转为UserRoleItemVO */
    private UserRoleItemVO convertToUserRoleItemVO(SysUser user) {
        return UserRoleItemVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .build();
    }

    /** 将角色实体转为VO */
    private RoleItemVO convertToRoleItemVO(SysRole role) {
        return RoleItemVO.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .build();
    }

    /** 构建单个类别的权限VO */
    private CategoryPermissionVO buildCategoryPermissionVO(FbCategory category) {
        List<Long> adminIds = getAdminIdsByCategoryId(category.getId());
        return CategoryPermissionVO.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .adminIds(adminIds)
                .build();
    }

    /** 查询指定类别的管理员ID列表 */
    private List<Long> getAdminIdsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<FbCategoryAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCategoryAdmin::getCategoryId, categoryId);
        List<FbCategoryAdmin> relations = fbCategoryAdminMapper.selectList(wrapper);
        return relations.stream()
                .map(FbCategoryAdmin::getUserId)
                .collect(Collectors.toList());
    }

    /** 删除指定类别的所有管理员关联 */
    private void deleteExistingPermissions(Long categoryId) {
        LambdaQueryWrapper<FbCategoryAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCategoryAdmin::getCategoryId, categoryId);
        fbCategoryAdminMapper.delete(wrapper);
    }

    /** 批量插入新的管理员关联 */
    private void insertNewPermissions(Long categoryId, List<Long> adminIds) {
        if (adminIds == null || adminIds.isEmpty()) {
            return;
        }
        for (Long adminId : adminIds) {
            FbCategoryAdmin record = new FbCategoryAdmin();
            record.setCategoryId(categoryId);
            record.setUserId(adminId);
            fbCategoryAdminMapper.insert(record);
        }
    }

    /**
     * 自动维护 CATEGORY_ADMIN 角色：
     * - 新增的管理员自动分配 CATEGORY_ADMIN 角色
     * - 被移除且不再管理任何类别的用户自动撤销 CATEGORY_ADMIN 角色
     */
    private void syncCategoryAdminRole(List<Long> oldAdminIds, List<Long> newAdminIds) {
        Long categoryAdminRoleId = getCategoryAdminRoleId();
        if (categoryAdminRoleId == null) {
            return;
        }
        List<Long> safeNew = newAdminIds != null ? newAdminIds : Collections.emptyList();
        List<Long> safeOld = oldAdminIds != null ? oldAdminIds : Collections.emptyList();

        // 新增的用户：确保拥有 CATEGORY_ADMIN 角色
        for (Long userId : safeNew) {
            if (!safeOld.contains(userId)) {
                assignRole(userId, categoryAdminRoleId);
            }
        }
        // 被移除的用户：若不再管理任何类别，撤销 CATEGORY_ADMIN 角色
        for (Long userId : safeOld) {
            if (!safeNew.contains(userId) && !hasAnyCategoryPermission(userId)) {
                revokeRole(userId, categoryAdminRoleId);
            }
        }
    }

    /** 查询 CATEGORY_ADMIN 角色ID */
    private Long getCategoryAdminRoleId() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, "CATEGORY_ADMIN");
        SysRole role = sysRoleMapper.selectOne(wrapper);
        return role != null ? role.getId() : null;
    }

    /** 判断用户是否仍管理至少一个类别 */
    private boolean hasAnyCategoryPermission(Long userId) {
        LambdaQueryWrapper<FbCategoryAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCategoryAdmin::getUserId, userId);
        return fbCategoryAdminMapper.selectCount(wrapper) > 0;
    }
}

package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.mapper.FbCategoryAdminMapper;
import com.feedback.mapper.FbCategoryMapper;
import com.feedback.mapper.SysRoleMapper;
import com.feedback.model.entity.FbCategory;
import com.feedback.model.entity.FbCategoryAdmin;
import com.feedback.model.entity.SysRole;
import com.feedback.model.vo.CategoryPermissionVO;
import com.feedback.model.vo.RoleItemVO;
import com.feedback.service.SystemPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 先删除该类别的所有管理员关联
        deleteExistingPermissions(categoryId);
        // 批量插入新的管理员关联
        insertNewPermissions(categoryId, adminIds);
    }

    // ==================== 私有方法 ====================

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
}

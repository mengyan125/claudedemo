package com.feedback.service;

import com.feedback.model.vo.CategoryPermissionVO;
import com.feedback.model.vo.RoleItemVO;

import java.util.List;

/**
 * 系统权限服务接口
 * 管理角色查询和类别权限配置
 */
public interface SystemPermissionService {

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<RoleItemVO> getRoleList();

    /**
     * 获取类别权限列表
     * 返回每个类别及其对应的管理员ID列表
     *
     * @return 类别权限列表
     */
    List<CategoryPermissionVO> getCategoryPermissions();

    /**
     * 更新类别权限
     * 先删除该类别的所有管理员关联，再批量插入新的关联
     *
     * @param categoryId 类别ID
     * @param adminIds   管理员用户ID列表
     */
    void updateCategoryPermission(Long categoryId, List<Long> adminIds);
}

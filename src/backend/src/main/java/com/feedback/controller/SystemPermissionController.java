package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.vo.CategoryPermissionVO;
import com.feedback.model.vo.RoleItemVO;
import com.feedback.service.SystemPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统权限控制器
 * 管理角色查询和类别权限配置
 */
@RestController
@RequestMapping("/api/system")
public class SystemPermissionController {

    @Autowired
    private SystemPermissionService systemPermissionService;

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @GetMapping("/role/list")
    public Result<List<RoleItemVO>> getRoleList() {
        List<RoleItemVO> roles = systemPermissionService.getRoleList();
        return Result.ok(roles);
    }

    /**
     * 获取类别权限列表
     *
     * @return 类别权限列表
     */
    @GetMapping("/category-permission/list")
    public Result<List<CategoryPermissionVO>> getCategoryPermissions() {
        List<CategoryPermissionVO> permissions =
                systemPermissionService.getCategoryPermissions();
        return Result.ok(permissions);
    }

    /**
     * 更新类别权限
     *
     * @param categoryId 类别ID
     * @param body       请求体，包含 adminIds 字段
     */
    @SuppressWarnings("unchecked")
    @PutMapping("/category-permission/{categoryId}")
    public Result<Void> updateCategoryPermission(
            @PathVariable Long categoryId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> adminIds = body.get("adminIds");
        systemPermissionService.updateCategoryPermission(categoryId, adminIds);
        return Result.ok();
    }
}

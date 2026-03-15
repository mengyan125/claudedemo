package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.AssignRoleDTO;
import com.feedback.model.vo.CategoryPermissionVO;
import com.feedback.model.vo.RoleItemVO;
import com.feedback.model.vo.UserRoleItemVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.SystemPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    @RequiresRole("SYSTEM_ADMIN")
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
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
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
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
    public Result<Void> updateCategoryPermission(
            @PathVariable Long categoryId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> adminIds = body.get("adminIds");
        systemPermissionService.updateCategoryPermission(categoryId, adminIds);
        return Result.ok();
    }

    /**
     * 获取角色下的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @GetMapping("/role/users")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<List<UserRoleItemVO>> getRoleUsers(@RequestParam Long roleId) {
        List<UserRoleItemVO> users = systemPermissionService.getRoleUsers(roleId);
        return Result.ok(users);
    }

    /**
     * 分配角色给用户
     *
     * @param dto 分配角色DTO
     */
    @PostMapping("/role/assign")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> assignRole(@Valid @RequestBody AssignRoleDTO dto) {
        systemPermissionService.assignRole(dto.getUserId(), dto.getRoleId());
        return Result.ok();
    }

    /**
     * 撤销用户的角色
     *
     * @param dto 撤销角色DTO
     */
    @DeleteMapping("/role/revoke")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> revokeRole(@Valid @RequestBody AssignRoleDTO dto) {
        systemPermissionService.revokeRole(dto.getUserId(), dto.getRoleId());
        return Result.ok();
    }
}

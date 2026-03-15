package com.feedback.controller;

import com.feedback.common.result.PageResult;
import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateUserDTO;
import com.feedback.model.dto.UpdateUserDTO;
import com.feedback.model.dto.UpdateUserStatusDTO;
import com.feedback.model.vo.UserItemVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统用户管理控制器
 * 处理用户的增删改查及状态管理请求
 */
@RestController
@RequestMapping("/api/system/user")
@RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
public class SystemUserController {

    @Autowired
    private SystemUserService systemUserService;

    /**
     * 分页查询用户列表
     *
     * @param keyword  搜索关键词（可选）
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return 分页用户列表
     */
    @GetMapping("/list")
    public Result<PageResult<UserItemVO>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {
        PageResult<UserItemVO> pageResult = systemUserService.getUserList(keyword, pageNum, pageSize);
        return Result.ok(pageResult);
    }

    /**
     * 创建用户
     *
     * @param dto 创建用户请求参数
     * @return 成功响应
     */
    @PostMapping
    public Result<Void> createUser(@RequestBody @Valid CreateUserDTO dto) {
        systemUserService.createUser(dto);
        return Result.ok();
    }

    /**
     * 批量创建用户
     *
     * @param dtoList 批量创建用户请求参数列表
     * @return 成功响应
     */
    @PostMapping("/batch")
    public Result<Void> batchCreateUsers(@RequestBody @Valid List<CreateUserDTO> dtoList) {
        systemUserService.batchCreateUsers(dtoList);
        return Result.ok();
    }

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param dto 更新用户请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserDTO dto) {
        systemUserService.updateUser(id, dto);
        return Result.ok();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        systemUserService.deleteUser(id);
        return Result.ok();
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id  用户ID
     * @param dto 更新状态请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody @Valid UpdateUserStatusDTO dto) {
        systemUserService.updateUserStatus(id, dto);
        return Result.ok();
    }
}

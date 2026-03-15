package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryStatusDTO;
import com.feedback.model.vo.CategoryItemVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端-反馈类别控制器
 * 处理反馈类别的增删改查请求
 */
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    @Autowired
    private AdminCategoryService adminCategoryService;

    /**
     * 查询所有类别列表
     *
     * @return 类别列表
     */
    @GetMapping("/list")
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN", "CATEGORY_ADMIN"})
    public Result<List<CategoryItemVO>> getCategoryList() {
        List<CategoryItemVO> list = adminCategoryService.getCategoryList();
        return Result.ok(list);
    }

    /**
     * 创建类别
     *
     * @param dto 创建类别请求参数
     * @return 成功响应
     */
    @PostMapping
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
    public Result<Void> createCategory(@RequestBody @Valid CreateCategoryDTO dto) {
        adminCategoryService.createCategory(dto);
        return Result.ok();
    }

    /**
     * 更新类别信息
     *
     * @param id  类别ID
     * @param dto 更新类别请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody @Valid UpdateCategoryDTO dto) {
        adminCategoryService.updateCategory(id, dto);
        return Result.ok();
    }

    /**
     * 删除类别
     *
     * @param id 类别ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.ok();
    }

    /**
     * 更新类别状态
     *
     * @param id  类别ID
     * @param dto 更新状态请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN"})
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @RequestBody @Valid UpdateCategoryStatusDTO dto) {
        adminCategoryService.updateCategoryStatus(id, dto);
        return Result.ok();
    }
}

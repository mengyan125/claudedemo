package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateClassDTO;
import com.feedback.model.dto.UpdateClassDTO;
import com.feedback.model.vo.ClassItemVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.BaseClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 班级管理控制器
 * 处理班级的增删改查请求
 */
@RestController
@RequestMapping("/api/base/class")
public class BaseClassController {

    @Autowired
    private BaseClassService baseClassService;

    /**
     * 查询班级列表
     *
     * @param gradeId 年级ID（可选，用于按年级筛选）
     * @return 班级列表
     */
    @GetMapping("/list")
    public Result<List<ClassItemVO>> getClassList(@RequestParam(required = false) Long gradeId) {
        List<ClassItemVO> list = baseClassService.getClassList(gradeId);
        return Result.ok(list);
    }

    /**
     * 创建班级
     *
     * @param dto 创建班级请求参数
     * @return 成功响应
     */
    @PostMapping
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> createClass(@RequestBody @Valid CreateClassDTO dto) {
        baseClassService.createClass(dto);
        return Result.ok();
    }

    /**
     * 更新班级信息
     *
     * @param id  班级ID
     * @param dto 更新班级请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> updateClass(@PathVariable Long id, @RequestBody @Valid UpdateClassDTO dto) {
        baseClassService.updateClass(id, dto);
        return Result.ok();
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> deleteClass(@PathVariable Long id) {
        baseClassService.deleteClass(id);
        return Result.ok();
    }
}

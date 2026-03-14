package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateGradeDTO;
import com.feedback.model.dto.UpdateGradeDTO;
import com.feedback.model.entity.BaseGrade;
import com.feedback.service.BaseGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 年级管理控制器
 * 处理年级的增删改查请求
 */
@RestController
@RequestMapping("/api/base/grade")
public class BaseGradeController {

    @Autowired
    private BaseGradeService baseGradeService;

    /**
     * 查询所有年级列表
     *
     * @return 年级列表
     */
    @GetMapping("/list")
    public Result<List<BaseGrade>> getGradeList() {
        List<BaseGrade> list = baseGradeService.getGradeList();
        return Result.ok(list);
    }

    /**
     * 创建年级
     *
     * @param dto 创建年级请求参数
     * @return 成功响应
     */
    @PostMapping
    public Result<Void> createGrade(@RequestBody @Valid CreateGradeDTO dto) {
        baseGradeService.createGrade(dto);
        return Result.ok();
    }

    /**
     * 更新年级信息
     *
     * @param id  年级ID
     * @param dto 更新年级请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateGrade(@PathVariable Long id, @RequestBody @Valid UpdateGradeDTO dto) {
        baseGradeService.updateGrade(id, dto);
        return Result.ok();
    }

    /**
     * 删除年级
     *
     * @param id 年级ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        baseGradeService.deleteGrade(id);
        return Result.ok();
    }
}

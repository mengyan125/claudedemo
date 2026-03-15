package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateSemesterDTO;
import com.feedback.model.dto.UpdateSemesterDTO;
import com.feedback.model.vo.SemesterItemVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.BaseSemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 学期管理控制器
 */
@RestController
@RequestMapping("/api/base/semester")
public class BaseSemesterController {

    @Autowired
    private BaseSemesterService baseSemesterService;

    @GetMapping("/list")
    public Result<List<SemesterItemVO>> getSemesterList() {
        List<SemesterItemVO> list = baseSemesterService.getSemesterList();
        return Result.ok(list);
    }

    @GetMapping("/current")
    public Result<SemesterItemVO> getCurrentSemester() {
        SemesterItemVO semester = baseSemesterService.getCurrentSemester();
        return Result.ok(semester);
    }

    /**
     * 创建学期
     *
     * @param dto 创建学期请求参数
     * @return 成功响应
     */
    @PostMapping
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> createSemester(@RequestBody @Valid CreateSemesterDTO dto) {
        baseSemesterService.createSemester(dto);
        return Result.ok();
    }

    /**
     * 更新学期
     */
    @PutMapping("/{id}")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> updateSemester(@PathVariable Long id, @RequestBody @Valid UpdateSemesterDTO dto) {
        baseSemesterService.updateSemester(id, dto);
        return Result.ok();
    }

    /**
     * 设置当前学期
     *
     * @param id 学期ID
     * @return 成功响应
     */
    @PutMapping("/{id}/current")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> setCurrentSemester(@PathVariable Long id) {
        baseSemesterService.setCurrentSemester(id);
        return Result.ok();
    }

    /**
     * 删除学期
     *
     * @param id 学期ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresRole("SYSTEM_ADMIN")
    public Result<Void> deleteSemester(@PathVariable Long id) {
        baseSemesterService.deleteSemester(id);
        return Result.ok();
    }
}

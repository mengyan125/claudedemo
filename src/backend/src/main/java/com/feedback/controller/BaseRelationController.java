package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.AssignStudentsDTO;
import com.feedback.model.dto.AssignTeacherDTO;
import com.feedback.model.vo.StudentAssignmentVO;
import com.feedback.model.vo.TeacherAssignmentVO;
import com.feedback.service.BaseRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 班级关系管理控制器
 * 处理学生分班、教师分配等关系管理请求
 */
@RestController
@RequestMapping("/api/base/relation")
public class BaseRelationController {

    @Autowired
    private BaseRelationService baseRelationService;

    /**
     * 获取班级下的学生列表
     *
     * @param classId 班级ID
     * @return 学生分配信息列表
     */
    @GetMapping("/class/{classId}/students")
    public Result<List<StudentAssignmentVO>> getClassStudents(@PathVariable Long classId) {
        List<StudentAssignmentVO> list = baseRelationService.getClassStudents(classId);
        return Result.ok(list);
    }

    /**
     * 获取未分配班级的学生列表
     *
     * @return 未分配学生列表
     */
    @GetMapping("/students/unassigned")
    public Result<List<StudentAssignmentVO>> getUnassignedStudents() {
        List<StudentAssignmentVO> list = baseRelationService.getUnassignedStudents();
        return Result.ok(list);
    }

    /**
     * 将学生分配到班级
     *
     * @param classId 班级ID
     * @param dto     学生分配请求参数
     * @return 成功响应
     */
    @PostMapping("/class/{classId}/students")
    public Result<Void> assignStudentsToClass(@PathVariable Long classId, @RequestBody @Valid AssignStudentsDTO dto) {
        baseRelationService.assignStudentsToClass(classId, dto);
        return Result.ok();
    }

    /**
     * 移除学生班级分配
     *
     * @param id 学生班级关系ID
     * @return 成功响应
     */
    @DeleteMapping("/student-assignment/{id}")
    public Result<Void> removeStudentAssignment(@PathVariable Long id) {
        baseRelationService.removeStudentAssignment(id);
        return Result.ok();
    }

    /**
     * 获取班级下的教师列表
     *
     * @param classId 班级ID
     * @return 教师分配信息列表
     */
    @GetMapping("/class/{classId}/teachers")
    public Result<List<TeacherAssignmentVO>> getClassTeachers(@PathVariable Long classId) {
        List<TeacherAssignmentVO> list = baseRelationService.getClassTeachers(classId);
        return Result.ok(list);
    }

    /**
     * 分配教师到班级
     *
     * @param classId 班级ID
     * @param dto     教师分配请求参数
     * @return 成功响应
     */
    @PostMapping("/class/{classId}/teachers")
    public Result<Void> assignTeacherToClass(@PathVariable Long classId, @RequestBody @Valid AssignTeacherDTO dto) {
        baseRelationService.assignTeacherToClass(classId, dto);
        return Result.ok();
    }

    /**
     * 移除教师班级分配
     *
     * @param id 教师班级关系ID
     * @return 成功响应
     */
    @DeleteMapping("/teacher-assignment/{id}")
    public Result<Void> removeTeacherAssignment(@PathVariable Long id) {
        baseRelationService.removeTeacherAssignment(id);
        return Result.ok();
    }
}

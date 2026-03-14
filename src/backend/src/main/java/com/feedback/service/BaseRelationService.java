package com.feedback.service;

import com.feedback.model.dto.AssignStudentsDTO;
import com.feedback.model.dto.AssignTeacherDTO;
import com.feedback.model.vo.StudentAssignmentVO;
import com.feedback.model.vo.TeacherAssignmentVO;

import java.util.List;

/**
 * 班级关系管理服务接口
 * 提供学生分班、教师分配等关系管理功能
 */
public interface BaseRelationService {

    /**
     * 获取班级下的学生列表
     *
     * @param classId 班级ID
     * @return 学生分配信息列表
     */
    List<StudentAssignmentVO> getClassStudents(Long classId);

    /**
     * 获取未分配班级的学生列表
     *
     * @return 未分配学生列表
     */
    List<StudentAssignmentVO> getUnassignedStudents();

    /**
     * 将学生分配到班级
     *
     * @param classId 班级ID
     * @param dto     学生分配请求参数
     */
    void assignStudentsToClass(Long classId, AssignStudentsDTO dto);

    /**
     * 移除学生班级分配
     *
     * @param id 学生班级关系ID
     */
    void removeStudentAssignment(Long id);

    /**
     * 获取班级下的教师列表
     *
     * @param classId 班级ID
     * @return 教师分配信息列表
     */
    List<TeacherAssignmentVO> getClassTeachers(Long classId);

    /**
     * 分配教师到班级
     *
     * @param classId 班级ID
     * @param dto     教师分配请求参数
     */
    void assignTeacherToClass(Long classId, AssignTeacherDTO dto);

    /**
     * 移除教师班级分配
     *
     * @param id 教师班级关系ID
     */
    void removeTeacherAssignment(Long id);
}

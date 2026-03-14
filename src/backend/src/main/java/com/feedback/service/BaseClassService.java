package com.feedback.service;

import com.feedback.model.dto.CreateClassDTO;
import com.feedback.model.dto.UpdateClassDTO;
import com.feedback.model.vo.ClassItemVO;

import java.util.List;

/**
 * 班级管理服务接口
 * 提供班级的增删改查功能
 */
public interface BaseClassService {

    /**
     * 查询班级列表
     *
     * @param gradeId 年级ID（可选，用于按年级筛选）
     * @return 班级列表
     */
    List<ClassItemVO> getClassList(Long gradeId);

    /**
     * 创建班级
     *
     * @param dto 创建班级请求参数
     */
    void createClass(CreateClassDTO dto);

    /**
     * 更新班级信息
     *
     * @param id  班级ID
     * @param dto 更新班级请求参数
     */
    void updateClass(Long id, UpdateClassDTO dto);

    /**
     * 删除班级
     *
     * @param id 班级ID
     */
    void deleteClass(Long id);
}

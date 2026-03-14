package com.feedback.service;

import com.feedback.model.dto.CreateGradeDTO;
import com.feedback.model.dto.UpdateGradeDTO;
import com.feedback.model.entity.BaseGrade;

import java.util.List;

/**
 * 年级管理服务接口
 * 提供年级的增删改查功能
 */
public interface BaseGradeService {

    /**
     * 查询所有年级列表
     *
     * @return 年级列表
     */
    List<BaseGrade> getGradeList();

    /**
     * 创建年级
     *
     * @param dto 创建年级请求参数
     */
    void createGrade(CreateGradeDTO dto);

    /**
     * 更新年级信息
     *
     * @param id  年级ID
     * @param dto 更新年级请求参数
     */
    void updateGrade(Long id, UpdateGradeDTO dto);

    /**
     * 删除年级
     *
     * @param id 年级ID
     */
    void deleteGrade(Long id);
}

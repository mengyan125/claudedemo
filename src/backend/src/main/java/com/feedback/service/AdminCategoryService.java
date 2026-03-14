package com.feedback.service;

import com.feedback.model.dto.CreateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryStatusDTO;
import com.feedback.model.vo.CategoryItemVO;

import java.util.List;

/**
 * 管理端-反馈类别服务接口
 * 提供反馈类别的增删改查功能
 */
public interface AdminCategoryService {

    /**
     * 查询所有类别列表
     *
     * @return 类别列表
     */
    List<CategoryItemVO> getCategoryList();

    /**
     * 创建类别
     *
     * @param dto 创建类别请求参数
     */
    void createCategory(CreateCategoryDTO dto);

    /**
     * 更新类别信息
     *
     * @param id  类别ID
     * @param dto 更新类别请求参数
     */
    void updateCategory(Long id, UpdateCategoryDTO dto);

    /**
     * 删除类别
     *
     * @param id 类别ID
     */
    void deleteCategory(Long id);

    /**
     * 更新类别状态
     *
     * @param id  类别ID
     * @param dto 更新状态请求参数
     */
    void updateCategoryStatus(Long id, UpdateCategoryStatusDTO dto);
}

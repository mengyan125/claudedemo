package com.feedback.service;

import com.feedback.model.dto.CreateSemesterDTO;
import com.feedback.model.dto.UpdateSemesterDTO;
import com.feedback.model.vo.SemesterItemVO;

import java.util.List;

/**
 * 学期管理服务接口
 * 提供学期的增删查及当前学期设置功能
 */
public interface BaseSemesterService {

    /**
     * 查询所有学期列表
     *
     * @return 学期列表
     */
    List<SemesterItemVO> getSemesterList();

    /**
     * 获取当前学期
     *
     * @return 当前学期
     */
    SemesterItemVO getCurrentSemester();

    /**
     * 创建学期
     *
     * @param dto 创建学期请求参数
     */
    void createSemester(CreateSemesterDTO dto);

    /**
     * 更新学期
     *
     * @param id 学期ID
     * @param dto 更新学期请求参数
     */
    void updateSemester(Long id, UpdateSemesterDTO dto);

    /**
     * 设置当前学期
     *
     * @param id 学期ID
     */
    void setCurrentSemester(Long id);

    /**
     * 取消当前学期
     *
     * @param id 学期ID
     */
    void unsetCurrentSemester(Long id);

    /**
     * 删除学期
     *
     * @param id 学期ID
     */
    void deleteSemester(Long id);
}

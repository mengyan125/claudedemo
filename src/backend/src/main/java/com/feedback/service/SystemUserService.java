package com.feedback.service;

import com.feedback.common.result.PageResult;
import com.feedback.model.dto.CreateUserDTO;
import com.feedback.model.dto.UpdateUserDTO;
import com.feedback.model.dto.UpdateUserStatusDTO;
import com.feedback.model.vo.UserItemVO;

import java.util.List;

/**
 * 系统用户管理服务接口
 * 提供用户的增删改查及状态管理功能
 */
public interface SystemUserService {

    /**
     * 分页查询用户列表
     *
     * @param keyword  搜索关键词（可选）
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return 分页用户列表
     */
    PageResult<UserItemVO> getUserList(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 创建用户
     *
     * @param dto 创建用户请求参数
     */
    void createUser(CreateUserDTO dto);

    /**
     * 批量创建用户
     *
     * @param dtoList 批量创建用户请求参数列表
     */
    void batchCreateUsers(List<CreateUserDTO> dtoList);

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param dto 更新用户请求参数
     */
    void updateUser(Long id, UpdateUserDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id  用户ID
     * @param dto 更新状态请求参数
     */
    void updateUserStatus(Long id, UpdateUserStatusDTO dto);
}

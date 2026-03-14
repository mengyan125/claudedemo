package com.feedback.service;

import com.feedback.model.dto.CreateQuickReplyDTO;
import com.feedback.model.dto.UpdateQuickReplyDTO;
import com.feedback.model.vo.QuickReplyItemVO;

import java.util.List;

/**
 * 管理端-快捷回复服务接口
 * 提供快捷回复的增删改查功能
 */
public interface AdminQuickReplyService {

    /**
     * 查询所有快捷回复列表
     *
     * @return 快捷回复列表
     */
    List<QuickReplyItemVO> getQuickReplyList();

    /**
     * 创建快捷回复
     *
     * @param dto 创建快捷回复请求参数
     */
    void createQuickReply(CreateQuickReplyDTO dto);

    /**
     * 更新快捷回复
     *
     * @param id  快捷回复ID
     * @param dto 更新快捷回复请求参数
     */
    void updateQuickReply(Long id, UpdateQuickReplyDTO dto);

    /**
     * 删除快捷回复
     *
     * @param id 快捷回复ID
     */
    void deleteQuickReply(Long id);
}

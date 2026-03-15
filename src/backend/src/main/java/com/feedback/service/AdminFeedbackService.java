package com.feedback.service;

import com.feedback.common.result.PageResult;
import com.feedback.model.dto.CreateReminderDTO;
import com.feedback.model.vo.AdminFeedbackDetailVO;
import com.feedback.model.vo.AdminFeedbackItemVO;
import com.feedback.model.vo.FeedbackStatusCountVO;
import com.feedback.model.vo.ReminderItemVO;
import com.feedback.model.vo.UserSearchVO;

import java.util.List;

/**
 * 管理员端-反馈服务接口
 */
public interface AdminFeedbackService {

    /**
     * 获取反馈列表（分页）
     *
     * @param categoryId 类别ID（可选）
     * @param status     状态筛选（可选，submitted/replied）
     * @param keyword    搜索关键词（可选，匹配标题和内容）
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    PageResult<AdminFeedbackItemVO> getFeedbackList(Long categoryId, String status,
                                                     String keyword, Long gradeId,
                                                     Long classId, Long teacherId,
                                                     String dateStart, String dateEnd,
                                                     String replyStatus, Boolean isFavorited,
                                                     Integer pageNum, Integer pageSize);

    /**
     * 获取反馈状态数量统计
     */
    FeedbackStatusCountVO getStatusCount(Long categoryId, String keyword, Long gradeId,
                                          Long classId, Long teacherId,
                                          String dateStart, String dateEnd);

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    AdminFeedbackDetailVO getFeedbackDetail(Long id);

    /**
     * 管理员回复反馈
     *
     * @param feedbackId 反馈ID
     * @param content    回复内容
     */
    void replyFeedback(Long feedbackId, String content);

    /**
     * 收藏/取消收藏反馈
     *
     * @param feedbackId 反馈ID
     */
    void toggleFavorite(Long feedbackId);

    /**
     * 发送备注提醒
     *
     * @param feedbackId 反馈ID
     * @param dto        提醒内容和接收人
     */
    void sendReminder(Long feedbackId, CreateReminderDTO dto);

    /**
     * 获取提醒关注列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 提醒列表
     */
    PageResult<ReminderItemVO> getReminderList(Integer pageNum, Integer pageSize);

    /**
     * 搜索管理员用户（备注提醒时选择人员）
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    List<UserSearchVO> searchUsers(String keyword, String userType);
}

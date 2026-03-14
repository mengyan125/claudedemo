package com.feedback.service;

import com.feedback.common.result.PageResult;
import com.feedback.model.dto.FeedbackSubmitDTO;
import com.feedback.model.vo.*;

import java.util.List;

/**
 * 学生端-反馈服务接口
 */
public interface StudentFeedbackService {

    /**
     * 获取学生可见的反馈类别列表
     *
     * @return 类别列表
     */
    List<StudentCategoryVO> getStudentCategories();

    /**
     * 获取当前学生的任课教师列表
     *
     * @return 教师列表
     */
    List<TeacherOptionVO> getStudentTeachers();

    /**
     * 获取反馈列表（分页）
     *
     * @param categoryId 类别ID（可选）
     * @param keyword    搜索关键词（可选）
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    PageResult<FeedbackListItemVO> getFeedbackList(Long categoryId, String keyword,
                                                    Integer pageNum, Integer pageSize);

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    FeedbackDetailVO getFeedbackDetail(Long id);

    /**
     * 提交/暂存反馈
     *
     * @param dto 反馈提交数据
     * @return 反馈ID
     */
    Long submitFeedback(FeedbackSubmitDTO dto);

    /**
     * 学生追加回复
     *
     * @param feedbackId 反馈ID
     * @param content    回复内容
     */
    void addReply(Long feedbackId, String content);
}

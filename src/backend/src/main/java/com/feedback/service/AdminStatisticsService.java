package com.feedback.service;

import com.feedback.model.vo.FeedbackStatisticsVO;
import com.feedback.model.vo.TeacherFeedbackResultVO;

/**
 * 管理员端-统计服务接口
 */
public interface AdminStatisticsService {

    /**
     * 获取统计概览数据
     *
     * @return 统计概览VO
     */
    FeedbackStatisticsVO getStatistics();

    /**
     * 获取指定教师的被反馈列表
     *
     * @param teacherId 教师ID
     * @return 教师被反馈结果VO
     */
    TeacherFeedbackResultVO getTeacherFeedbackList(Long teacherId);
}

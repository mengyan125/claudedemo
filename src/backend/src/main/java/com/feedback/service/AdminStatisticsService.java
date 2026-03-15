package com.feedback.service;

import com.feedback.model.vo.FeedbackStatisticsVO;
import com.feedback.model.vo.FeedbackStatisticsVO.TeacherTop10Item;
import com.feedback.model.vo.TeacherFeedbackResultVO;

import java.util.Date;
import java.util.List;

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
     * 获取教师被反馈TOP10（支持时间筛选）
     *
     * @param startDate 开始时间（可为null）
     * @param endDate   结束时间（可为null）
     * @return TOP10列表
     */
    List<TeacherTop10Item> getTeacherTop10(Date startDate, Date endDate);

    /**
     * 获取指定教师的被反馈列表
     *
     * @param teacherId 教师ID
     * @return 教师被反馈结果VO
     */
    TeacherFeedbackResultVO getTeacherFeedbackList(Long teacherId);
}

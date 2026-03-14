package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.vo.FeedbackStatisticsVO;
import com.feedback.model.vo.TeacherFeedbackResultVO;
import com.feedback.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端-统计控制器
 * 提供反馈统计概览和教师被反馈列表接口
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 获取统计概览
     *
     * @return 统计概览数据
     */
    @GetMapping
    public Result<FeedbackStatisticsVO> getStatistics() {
        FeedbackStatisticsVO statistics = adminStatisticsService.getStatistics();
        return Result.ok(statistics);
    }

    /**
     * 获取教师被反馈列表
     *
     * @param teacherId 教师ID
     * @return 教师被反馈结果
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<TeacherFeedbackResultVO> getTeacherFeedbackList(
            @PathVariable Long teacherId) {
        TeacherFeedbackResultVO result =
                adminStatisticsService.getTeacherFeedbackList(teacherId);
        return Result.ok(result);
    }
}

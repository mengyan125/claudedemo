package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.vo.FeedbackStatisticsVO;
import com.feedback.model.vo.FeedbackStatisticsVO.TeacherTop10Item;
import com.feedback.model.vo.TeacherFeedbackResultVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 管理员端-统计控制器
 * 提供反馈统计概览和教师被反馈列表接口
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiresRole("SYSTEM_ADMIN")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 获取统计概览
     *
     * @return 统计概览数据
     */
    @GetMapping
    public Result<FeedbackStatisticsVO> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        FeedbackStatisticsVO statistics = adminStatisticsService.getStatistics(startDate, endDate);
        return Result.ok(statistics);
    }

    /**
     * 获取教师被反馈TOP10（支持时间筛选）
     */
    @GetMapping("/teacher-top10")
    public Result<List<TeacherTop10Item>> getTeacherTop10(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<TeacherTop10Item> top10 = adminStatisticsService.getTeacherTop10(startDate, endDate);
        return Result.ok(top10);
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

package com.feedback.controller;

import com.feedback.common.result.PageResult;
import com.feedback.common.result.Result;
import com.feedback.model.vo.AdminFeedbackDetailVO;
import com.feedback.model.vo.AdminFeedbackItemVO;
import com.feedback.service.AdminFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员端-反馈控制器
 * 处理管理员查看反馈列表、详情、回复及收藏等请求
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    @Autowired
    private AdminFeedbackService adminFeedbackService;

    /**
     * 获取反馈列表（分页）
     *
     * @param categoryId 类别ID（可选）
     * @param status     状态筛选（可选，submitted/replied）
     * @param keyword    搜索关键词（可选）
     * @param pageNum    页码，默认1
     * @param pageSize   每页大小，默认10
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<AdminFeedbackItemVO>> getFeedbackList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AdminFeedbackItemVO> result = adminFeedbackService
                .getFeedbackList(categoryId, status, keyword, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    @GetMapping("/{id}")
    public Result<AdminFeedbackDetailVO> getFeedbackDetail(@PathVariable Long id) {
        AdminFeedbackDetailVO detail = adminFeedbackService.getFeedbackDetail(id);
        return Result.ok(detail);
    }

    /**
     * 管理员回复反馈
     *
     * @param feedbackId 反馈ID
     * @param body       请求体，包含 content 字段
     */
    @PostMapping("/{feedbackId}/reply")
    public Result<Void> replyFeedback(@PathVariable Long feedbackId,
                                       @RequestBody Map<String, String> body) {
        String content = body.get("content");
        adminFeedbackService.replyFeedback(feedbackId, content);
        return Result.ok();
    }

    /**
     * 收藏/取消收藏反馈
     *
     * @param feedbackId 反馈ID
     */
    @PostMapping("/{feedbackId}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long feedbackId) {
        adminFeedbackService.toggleFavorite(feedbackId);
        return Result.ok();
    }
}

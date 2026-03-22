package com.feedback.controller;

import com.feedback.common.result.PageResult;
import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateReminderDTO;
import com.feedback.model.vo.AdminFeedbackDetailVO;
import com.feedback.model.vo.AdminFeedbackItemVO;
import com.feedback.model.vo.FeedbackStatusCountVO;
import com.feedback.model.vo.ReminderItemVO;
import com.feedback.model.vo.UserSearchVO;
import com.feedback.security.RequiresRole;
import com.feedback.service.AdminFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 管理员端-反馈控制器
 * 处理管理员查看反馈列表、详情、回复、收藏及备注提醒等请求
 */
@RestController
@RequestMapping("/api/admin")
@RequiresRole({"SYSTEM_ADMIN", "ROLE_ADMIN", "CATEGORY_ADMIN"})
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
    @GetMapping("/feedback/list")
    public Result<PageResult<AdminFeedbackItemVO>> getFeedbackList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String dateStart,
            @RequestParam(required = false) String dateEnd,
            @RequestParam(required = false) String replyStatus,
            @RequestParam(required = false) Boolean isFavorited,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AdminFeedbackItemVO> result = adminFeedbackService
                .getFeedbackList(categoryId, status, keyword, gradeId, classId,
                        teacherId, dateStart, dateEnd, replyStatus, isFavorited,
                        pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 获取反馈状态数量统计
     */
    @GetMapping("/feedback/status-count")
    public Result<FeedbackStatusCountVO> getStatusCount(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String dateStart,
            @RequestParam(required = false) String dateEnd) {
        FeedbackStatusCountVO result = adminFeedbackService.getStatusCount(
                categoryId, keyword, gradeId, classId, teacherId, dateStart, dateEnd);
        return Result.ok(result);
    }

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    @GetMapping("/feedback/{id}")
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
    @PostMapping("/feedback/{feedbackId}/reply")
    public Result<Void> replyFeedback(@PathVariable Long feedbackId,
                                       @RequestBody Map<String, String> body) {
        String content = body.get("content");
        adminFeedbackService.replyFeedback(feedbackId, content);
        return Result.ok();
    }

    /**
     * 收藏/取消收藏反馈
     */
    @PostMapping("/feedback/{feedbackId}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long feedbackId) {
        adminFeedbackService.toggleFavorite(feedbackId);
        return Result.ok();
    }

    /**
     * 删除反馈（仅系统管理员）
     */
    @DeleteMapping("/feedback/{feedbackId}")
    @RequiresRole({"SYSTEM_ADMIN"})
    public Result<Void> deleteFeedback(@PathVariable Long feedbackId) {
        adminFeedbackService.deleteFeedback(feedbackId);
        return Result.ok();
    }

    /**
     * 发送备注提醒
     *
     * @param feedbackId 反馈ID
     * @param dto        提醒内容和接收人
     */
    @PostMapping("/feedback/{feedbackId}/reminder")
    public Result<Void> sendReminder(@PathVariable Long feedbackId,
                                      @Valid @RequestBody CreateReminderDTO dto) {
        adminFeedbackService.sendReminder(feedbackId, dto);
        return Result.ok();
    }

    /**
     * 获取提醒关注列表
     *
     * @param pageNum  页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 提醒列表
     */
    @GetMapping("/reminder/list")
    public Result<PageResult<ReminderItemVO>> getReminderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ReminderItemVO> result =
                adminFeedbackService.getReminderList(pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 搜索管理员用户（备注提醒时选择人员）
     *
     * @param keyword 搜索关键词（可选）
     * @return 用户列表
     */
    @GetMapping("/user/search")
    public Result<List<UserSearchVO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userType) {
        List<UserSearchVO> result =
                adminFeedbackService.searchUsers(keyword, userType);
        return Result.ok(result);
    }
}

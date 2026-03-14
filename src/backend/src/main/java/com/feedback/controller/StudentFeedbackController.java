package com.feedback.controller;

import com.feedback.common.result.PageResult;
import com.feedback.common.result.Result;
import com.feedback.model.dto.FeedbackSubmitDTO;
import com.feedback.model.vo.*;
import com.feedback.service.StudentFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生端-反馈控制器
 * 处理学生提交反馈、查看反馈列表与详情等请求
 */
@RestController
@RequestMapping("/api/student")
public class StudentFeedbackController {

    @Autowired
    private StudentFeedbackService studentFeedbackService;

    /**
     * 获取学生可见的类别列表
     *
     * @return 类别列表
     */
    @GetMapping("/categories")
    public Result<List<StudentCategoryVO>> getCategories() {
        List<StudentCategoryVO> list = studentFeedbackService.getStudentCategories();
        return Result.ok(list);
    }

    /**
     * 获取当前学生的任课教师列表
     *
     * @return 教师列表
     */
    @GetMapping("/teachers")
    public Result<List<TeacherOptionVO>> getTeachers() {
        List<TeacherOptionVO> list = studentFeedbackService.getStudentTeachers();
        return Result.ok(list);
    }

    /**
     * 获取反馈列表（分页）
     *
     * @param categoryId 类别ID（可选）
     * @param keyword    搜索关键词（可选）
     * @param pageNum    页码，默认1
     * @param pageSize   每页大小，默认10
     * @return 分页结果
     */
    @GetMapping("/feedback/list")
    public Result<PageResult<FeedbackListItemVO>> getFeedbackList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<FeedbackListItemVO> result = studentFeedbackService
                .getFeedbackList(categoryId, keyword, pageNum, pageSize);
        return Result.ok(result);
    }

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    @GetMapping("/feedback/{id}")
    public Result<FeedbackDetailVO> getFeedbackDetail(@PathVariable Long id) {
        FeedbackDetailVO detail = studentFeedbackService.getFeedbackDetail(id);
        return Result.ok(detail);
    }

    /**
     * 提交/暂存反馈
     *
     * @param dto 反馈提交数据
     * @return 包含反馈ID的响应
     */
    @PostMapping("/feedback")
    public Result<Map<String, Long>> submitFeedback(@RequestBody FeedbackSubmitDTO dto) {
        Long id = studentFeedbackService.submitFeedback(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.ok(data);
    }

    /**
     * 学生追加回复
     *
     * @param feedbackId 反馈ID
     * @param body       请求体，包含 content 字段
     */
    @PostMapping("/feedback/{feedbackId}/reply")
    public Result<Void> addReply(@PathVariable Long feedbackId,
                                  @RequestBody Map<String, String> body) {
        String content = body.get("content");
        studentFeedbackService.addReply(feedbackId, content);
        return Result.ok();
    }
}

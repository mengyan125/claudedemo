package com.feedback.controller;

import com.feedback.common.result.Result;
import com.feedback.model.dto.CreateQuickReplyDTO;
import com.feedback.model.dto.UpdateQuickReplyDTO;
import com.feedback.model.vo.QuickReplyItemVO;
import com.feedback.service.AdminQuickReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端-快捷回复控制器
 * 处理快捷回复的增删改查请求
 */
@RestController
@RequestMapping("/api/admin/quick-reply")
public class AdminQuickReplyController {

    @Autowired
    private AdminQuickReplyService adminQuickReplyService;

    /**
     * 查询所有快捷回复列表
     *
     * @return 快捷回复列表
     */
    @GetMapping("/list")
    public Result<List<QuickReplyItemVO>> getQuickReplyList() {
        List<QuickReplyItemVO> list = adminQuickReplyService.getQuickReplyList();
        return Result.ok(list);
    }

    /**
     * 创建快捷回复
     *
     * @param dto 创建快捷回复请求参数
     * @return 成功响应
     */
    @PostMapping
    public Result<Void> createQuickReply(@RequestBody @Valid CreateQuickReplyDTO dto) {
        adminQuickReplyService.createQuickReply(dto);
        return Result.ok();
    }

    /**
     * 更新快捷回复
     *
     * @param id  快捷回复ID
     * @param dto 更新快捷回复请求参数
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateQuickReply(@PathVariable Long id, @RequestBody @Valid UpdateQuickReplyDTO dto) {
        adminQuickReplyService.updateQuickReply(id, dto);
        return Result.ok();
    }

    /**
     * 删除快捷回复
     *
     * @param id 快捷回复ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuickReply(@PathVariable Long id) {
        adminQuickReplyService.deleteQuickReply(id);
        return Result.ok();
    }
}

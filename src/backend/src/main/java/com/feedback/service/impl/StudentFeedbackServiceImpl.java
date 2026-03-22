package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feedback.common.exception.BusinessException;
import com.feedback.common.result.PageResult;
import com.feedback.mapper.*;
import com.feedback.model.dto.FeedbackSubmitDTO;
import com.feedback.model.entity.*;
import com.feedback.model.vo.*;
import com.feedback.security.UserContext;
import com.feedback.service.StudentFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生端-反馈服务实现类
 */
@Service
public class StudentFeedbackServiceImpl implements StudentFeedbackService {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private FbCategoryMapper fbCategoryMapper;

    @Autowired
    private FbFeedbackMapper fbFeedbackMapper;

    @Autowired
    private FbFeedbackAdminReadMapper fbFeedbackAdminReadMapper;

    @Autowired
    private FbFeedbackAttachmentMapper fbFeedbackAttachmentMapper;

    @Autowired
    private FbReplyMapper fbReplyMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private BaseStudentClassMapper baseStudentClassMapper;

    @Autowired
    private BaseTeacherClassMapper baseTeacherClassMapper;

    @Autowired
    private BaseSemesterMapper baseSemesterMapper;

    @Override
    public List<StudentCategoryVO> getStudentCategories() {
        LambdaQueryWrapper<FbCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCategory::getStatus, 1)
               .orderByAsc(FbCategory::getSortOrder);
        List<FbCategory> categories = fbCategoryMapper.selectList(wrapper);

        return categories.stream().map(c -> StudentCategoryVO.builder()
                .id(c.getId())
                .name(c.getName())
                .isTeachingRelated(c.getIsTeachingRelated() != null && c.getIsTeachingRelated() == 1)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<TeacherOptionVO> getStudentTeachers() {
        Long studentId = UserContext.getCurrentUserId();
        // 查询当前学期
        Long semesterId = getCurrentSemesterId();
        // 查询学生所在班级
        List<Long> classIds = getStudentClassIds(studentId, semesterId);
        if (CollectionUtils.isEmpty(classIds)) {
            return new ArrayList<>();
        }
        // 查询班级的任课教师
        return getTeachersByClassIds(classIds, semesterId);
    }

    @Override
    public PageResult<FeedbackListItemVO> getFeedbackList(Long categoryId, String keyword,
                                                           Integer pageNum, Integer pageSize) {
        Long studentId = UserContext.getCurrentUserId();
        // 构建查询条件
        LambdaQueryWrapper<FbFeedback> wrapper = buildFeedbackListWrapper(studentId, categoryId, keyword);
        // 分页查询
        Page<FbFeedback> page = new Page<>(pageNum, pageSize);
        Page<FbFeedback> result = fbFeedbackMapper.selectPage(page, wrapper);
        // 转换为VO
        List<FeedbackListItemVO> voList = result.getRecords().stream()
                .map(this::convertToListItemVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public FeedbackDetailVO getFeedbackDetail(Long id) {
        Long studentId = UserContext.getCurrentUserId();
        FbFeedback feedback = fbFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!feedback.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该反馈");
        }
        // 构建详情VO
        FeedbackDetailVO vo = buildDetailVO(feedback);
        // 查看详情后自动标记已读
        markAsRead(feedback);
        return vo;
    }

    @Override
    @Transactional
    public Long submitFeedback(FeedbackSubmitDTO dto) {
        Long studentId = UserContext.getCurrentUserId();
        FbFeedback feedback;
        if (dto.getId() != null) {
            // 更新已有反馈
            feedback = updateExistingFeedback(dto, studentId);
        } else {
            // 新建反馈
            feedback = createNewFeedback(dto, studentId);
        }
        // 关联附件
        linkAttachments(feedback.getId(), dto.getAttachmentIds());
        return feedback.getId();
    }

    @Override
    @Transactional
    public void addReply(Long feedbackId, String content) {
        Long studentId = UserContext.getCurrentUserId();
        FbFeedback feedback = fbFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!feedback.getStudentId().equals(studentId)) {
            throw new BusinessException("无权回复该反馈");
        }
        // 插入回复记录
        FbReply reply = new FbReply();
        reply.setFeedbackId(feedbackId);
        reply.setReplyUserId(studentId);
        reply.setContent(content);
        reply.setCreateTime(new Date());
        fbReplyMapper.insert(reply);
        // 学生回复后标记管理员未读（按反馈维度）
        feedback.setHasUnreadForAdmin(1);
        feedback.setUpdateTime(new Date());
        fbFeedbackMapper.updateById(feedback);
        clearAdminReadRecords(feedbackId);
    }

    // ==================== 私有方法 ====================

    /** 获取当前学期ID */
    private Long getCurrentSemesterId() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSemester::getIsCurrent, 1).last("LIMIT 1");
        BaseSemester semester = baseSemesterMapper.selectOne(wrapper);
        if (semester == null) {
            throw new BusinessException("未找到当前学期");
        }
        return semester.getId();
    }

    /** 获取学生当前学期的班级ID列表 */
    private List<Long> getStudentClassIds(Long studentId, Long semesterId) {
        LambdaQueryWrapper<BaseStudentClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseStudentClass::getStudentId, studentId)
               .eq(BaseStudentClass::getSemesterId, semesterId);
        List<BaseStudentClass> studentClasses = baseStudentClassMapper.selectList(wrapper);
        return studentClasses.stream()
                .map(BaseStudentClass::getClassId)
                .collect(Collectors.toList());
    }

    /** 根据班级ID列表查询任课教师 */
    private List<TeacherOptionVO> getTeachersByClassIds(List<Long> classIds, Long semesterId) {
        LambdaQueryWrapper<BaseTeacherClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BaseTeacherClass::getClassId, classIds)
               .eq(BaseTeacherClass::getSemesterId, semesterId);
        List<BaseTeacherClass> teacherClasses = baseTeacherClassMapper.selectList(wrapper);

        // 去重并查询教师信息
        return teacherClasses.stream()
                .map(tc -> {
                    SysUser teacher = sysUserMapper.selectById(tc.getTeacherId());
                    if (teacher == null) {
                        return null;
                    }
                    return TeacherOptionVO.builder()
                            .id(teacher.getId())
                            .name(teacher.getRealName())
                            .subject(tc.getSubject())
                            .build();
                })
                .filter(vo -> vo != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /** 构建反馈列表查询条件 */
    private LambdaQueryWrapper<FbFeedback> buildFeedbackListWrapper(Long studentId,
                                                                     Long categoryId,
                                                                     String keyword) {
        LambdaQueryWrapper<FbFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedback::getStudentId, studentId);
        if (categoryId != null) {
            wrapper.eq(FbFeedback::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(FbFeedback::getTitle, keyword);
        }
        wrapper.orderByDesc(FbFeedback::getCreateTime);
        return wrapper;
    }

    /** 将反馈实体转换为列表项VO */
    /** 截取内容摘要，超过50字显示"……" */
    private String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= 50) {
            return content;
        }
        return content.substring(0, 50) + "……";
    }

    private FeedbackListItemVO convertToListItemVO(FbFeedback feedback) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        String teacherName = getTeacherName(feedback.getTeacherId());

        return FeedbackListItemVO.builder()
                .id(feedback.getId())
                .categoryId(feedback.getCategoryId())
                .categoryName(categoryName)
                .teacherId(feedback.getTeacherId())
                .teacherName(teacherName)
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .isAnonymous(feedback.getIsAnonymous() != null && feedback.getIsAnonymous() == 1)
                .status(feedback.getStatus())
                .replyStatus(feedback.getReplyStatus())
                .hasUnread(feedback.getHasUnreadReply() != null && feedback.getHasUnreadReply() == 1)
                .createTime(feedback.getCreateTime() != null ? sdf.format(feedback.getCreateTime()) : null)
                .build();
    }

    /** 构建反馈详情VO */
    private FeedbackDetailVO buildDetailVO(FbFeedback feedback) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        String teacherName = getTeacherName(feedback.getTeacherId());
        Boolean isTeachingRelated = getCategoryTeachingRelated(feedback.getCategoryId());
        List<AttachmentItemVO> attachments = getAttachmentsByFeedbackId(feedback.getId());
        List<ReplyItemVO> replies = getRepliesByFeedbackId(feedback.getId());

        return FeedbackDetailVO.builder()
                .id(feedback.getId())
                .categoryId(feedback.getCategoryId())
                .categoryName(categoryName)
                .teacherId(feedback.getTeacherId())
                .teacherName(teacherName)
                .isTeachingRelated(isTeachingRelated)
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .isAnonymous(feedback.getIsAnonymous() != null && feedback.getIsAnonymous() == 1)
                .status(feedback.getStatus())
                .replyStatus(feedback.getReplyStatus())
                .createTime(feedback.getCreateTime() != null ? sdf.format(feedback.getCreateTime()) : null)
                .attachments(attachments)
                .replies(replies)
                .build();
    }

    /** 获取类别名称 */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        FbCategory category = fbCategoryMapper.selectById(categoryId);
        return category != null ? category.getName() : null;
    }

    /** 获取类别是否教学相关 */
    private Boolean getCategoryTeachingRelated(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        FbCategory category = fbCategoryMapper.selectById(categoryId);
        return category != null && category.getIsTeachingRelated() != null && category.getIsTeachingRelated() == 1;
    }

    /** 获取教师姓名 */
    private String getTeacherName(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        SysUser teacher = sysUserMapper.selectById(teacherId);
        return teacher != null ? teacher.getRealName() : null;
    }

    /** 查询反馈的附件列表 */
    private List<AttachmentItemVO> getAttachmentsByFeedbackId(Long feedbackId) {
        LambdaQueryWrapper<FbFeedbackAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedbackAttachment::getFeedbackId, feedbackId);
        List<FbFeedbackAttachment> attachments = fbFeedbackAttachmentMapper.selectList(wrapper);
        return attachments.stream().map(a -> AttachmentItemVO.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .fileUrl("/api/files/" + a.getFilePath())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .build()
        ).collect(Collectors.toList());
    }

    /** 查询反馈的回复列表 */
    private List<ReplyItemVO> getRepliesByFeedbackId(Long feedbackId) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        LambdaQueryWrapper<FbReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbReply::getFeedbackId, feedbackId)
               .orderByAsc(FbReply::getCreateTime);
        List<FbReply> replies = fbReplyMapper.selectList(wrapper);

        return replies.stream().map(r -> {
            SysUser user = sysUserMapper.selectById(r.getReplyUserId());
            return ReplyItemVO.builder()
                    .id(r.getId())
                    .replyUserId(r.getReplyUserId())
                    .replyUserName(user != null ? user.getRealName() : null)
                    .userType(user != null ? resolveDisplayUserType(user) : null)
                    .content(r.getContent())
                    .createTime(r.getCreateTime() != null ? sdf.format(r.getCreateTime()) : null)
                    .build();
        }).collect(Collectors.toList());
    }

    /** 清理反馈的管理员已读记录（当学生有新回复时） */
    private void clearAdminReadRecords(Long feedbackId) {
        LambdaQueryWrapper<FbFeedbackAdminRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedbackAdminRead::getFeedbackId, feedbackId);
        fbFeedbackAdminReadMapper.delete(wrapper);
    }

    /** 标记反馈已读 */
    private void markAsRead(FbFeedback feedback) {
        if (feedback.getHasUnreadReply() != null && feedback.getHasUnreadReply() == 1) {
            feedback.setHasUnreadReply(0);
            feedback.setUpdateTime(new Date());
            fbFeedbackMapper.updateById(feedback);
        }
    }

    /** 更新已有反馈 */
    private FbFeedback updateExistingFeedback(FeedbackSubmitDTO dto, Long studentId) {
        FbFeedback feedback = fbFeedbackMapper.selectById(dto.getId());
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!feedback.getStudentId().equals(studentId)) {
            throw new BusinessException("无权修改该反馈");
        }
        if (!"draft".equals(feedback.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        feedback.setCategoryId(dto.getCategoryId());
        feedback.setTeacherId(dto.getTeacherId());
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setIsAnonymous(Boolean.TRUE.equals(dto.getIsAnonymous()) ? 1 : 0);
        feedback.setStatus(dto.getStatus());
        feedback.setHasUnreadForAdmin("submitted".equals(dto.getStatus()) ? 1 : 0);
        feedback.setUpdateTime(new Date());
        fbFeedbackMapper.updateById(feedback);
        clearAdminReadRecords(feedback.getId());
        return feedback;
    }

    /** 新建反馈 */
    private FbFeedback createNewFeedback(FeedbackSubmitDTO dto, Long studentId) {
        FbFeedback feedback = new FbFeedback();
        feedback.setCategoryId(dto.getCategoryId());
        feedback.setStudentId(studentId);
        feedback.setTeacherId(dto.getTeacherId());
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setIsAnonymous(Boolean.TRUE.equals(dto.getIsAnonymous()) ? 1 : 0);
        feedback.setStatus(dto.getStatus());
        feedback.setReplyStatus("unreplied");
        feedback.setHasUnreadReply(0);
        feedback.setHasUnreadForAdmin("submitted".equals(dto.getStatus()) ? 1 : 0);
        feedback.setCreateTime(new Date());
        feedback.setUpdateTime(new Date());
        fbFeedbackMapper.insert(feedback);
        clearAdminReadRecords(feedback.getId());
        return feedback;
    }

    /** 关联附件到反馈 */
    private void linkAttachments(Long feedbackId, List<Long> attachmentIds) {
        // 先清除旧的关联
        LambdaQueryWrapper<FbFeedbackAttachment> clearWrapper = new LambdaQueryWrapper<>();
        clearWrapper.eq(FbFeedbackAttachment::getFeedbackId, feedbackId);
        List<FbFeedbackAttachment> oldAttachments = fbFeedbackAttachmentMapper.selectList(clearWrapper);
        for (FbFeedbackAttachment old : oldAttachments) {
            old.setFeedbackId(null);
            fbFeedbackAttachmentMapper.updateById(old);
        }
        // 关联新的附件
        if (!CollectionUtils.isEmpty(attachmentIds)) {
            for (Long attachmentId : attachmentIds) {
                FbFeedbackAttachment attachment = fbFeedbackAttachmentMapper.selectById(attachmentId);
                if (attachment != null) {
                    attachment.setFeedbackId(feedbackId);
                    fbFeedbackAttachmentMapper.updateById(attachment);
                }
            }
        }
    }

    /** 根据用户角色转换显示用户类型 */
    private String resolveDisplayUserType(SysUser user) {
        List<String> roleCodes = getUserRoleCodes(user.getId());
        if (roleCodes.contains("ROLE_ADMIN")) {
            return "role_admin";
        }
        if (roleCodes.contains("CATEGORY_ADMIN")) {
            return "category_admin";
        }
        return user.getUserType();
    }

    /** 查询用户角色编码列表 */
    private List<String> getUserRoleCodes(Long userId) {
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(urWrapper);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        return roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }
}

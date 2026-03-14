package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feedback.common.exception.BusinessException;
import com.feedback.common.result.PageResult;
import com.feedback.mapper.*;
import com.feedback.model.entity.*;
import com.feedback.model.vo.*;
import com.feedback.security.UserContext;
import com.feedback.service.AdminFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员端-反馈服务实现类
 */
@Service
public class AdminFeedbackServiceImpl implements AdminFeedbackService {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 内容截断长度 */
    private static final int CONTENT_TRUNCATE_LENGTH = 30;

    @Autowired
    private FbFeedbackMapper fbFeedbackMapper;

    @Autowired
    private FbCategoryMapper fbCategoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FbCollectionMapper fbCollectionMapper;

    @Autowired
    private FbFeedbackAttachmentMapper fbFeedbackAttachmentMapper;

    @Autowired
    private FbReplyMapper fbReplyMapper;

    @Autowired
    private BaseStudentClassMapper baseStudentClassMapper;

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Autowired
    private BaseGradeMapper baseGradeMapper;

    @Autowired
    private BaseSemesterMapper baseSemesterMapper;

    @Override
    public PageResult<AdminFeedbackItemVO> getFeedbackList(Long categoryId, String status,
                                                            String keyword, Integer pageNum,
                                                            Integer pageSize) {
        Long adminId = UserContext.getCurrentUserId();
        // 构建查询条件
        LambdaQueryWrapper<FbFeedback> wrapper = buildListWrapper(categoryId, status, keyword);
        // 分页查询
        Page<FbFeedback> page = new Page<>(pageNum, pageSize);
        Page<FbFeedback> result = fbFeedbackMapper.selectPage(page, wrapper);
        // 转换为VO列表
        List<AdminFeedbackItemVO> voList = result.getRecords().stream()
                .map(fb -> convertToItemVO(fb, adminId))
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public AdminFeedbackDetailVO getFeedbackDetail(Long id) {
        Long adminId = UserContext.getCurrentUserId();
        FbFeedback feedback = fbFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        return buildDetailVO(feedback, adminId);
    }

    @Override
    @Transactional
    public void replyFeedback(Long feedbackId, String content) {
        Long adminId = UserContext.getCurrentUserId();
        FbFeedback feedback = fbFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        // 插入回复记录
        FbReply reply = new FbReply();
        reply.setFeedbackId(feedbackId);
        reply.setReplyUserId(adminId);
        reply.setContent(content);
        reply.setCreateTime(new Date());
        fbReplyMapper.insert(reply);
        // 更新反馈状态
        feedback.setStatus("replied");
        feedback.setReplyStatus("replied");
        feedback.setHasUnreadReply(1);
        feedback.setUpdateTime(new Date());
        fbFeedbackMapper.updateById(feedback);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long feedbackId) {
        Long adminId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<FbCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCollection::getUserId, adminId)
               .eq(FbCollection::getFeedbackId, feedbackId);
        FbCollection existing = fbCollectionMapper.selectOne(wrapper);
        if (existing != null) {
            // 已收藏则取消
            fbCollectionMapper.deleteById(existing.getId());
        } else {
            // 未收藏则添加
            FbCollection collection = new FbCollection();
            collection.setUserId(adminId);
            collection.setFeedbackId(feedbackId);
            collection.setCreateTime(new Date());
            fbCollectionMapper.insert(collection);
        }
    }

    // ==================== 私有方法 ====================

    /** 构建反馈列表查询条件 */
    private LambdaQueryWrapper<FbFeedback> buildListWrapper(Long categoryId,
                                                             String status,
                                                             String keyword) {
        LambdaQueryWrapper<FbFeedback> wrapper = new LambdaQueryWrapper<>();
        // 过滤掉草稿
        wrapper.ne(FbFeedback::getStatus, "draft");
        if (categoryId != null) {
            wrapper.eq(FbFeedback::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(FbFeedback::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FbFeedback::getTitle, keyword)
                              .or()
                              .like(FbFeedback::getContent, keyword));
        }
        wrapper.orderByDesc(FbFeedback::getCreateTime);
        return wrapper;
    }

    /** 将反馈实体转换为列表项VO */
    private AdminFeedbackItemVO convertToItemVO(FbFeedback feedback, Long adminId) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        String studentName = getStudentName(feedback.getStudentId(), feedback.getIsAnonymous());
        String teacherName = getTeacherName(feedback.getTeacherId());
        String[] gradeAndClass = getGradeAndClassName(feedback.getStudentId());
        boolean isFavorited = checkIsFavorited(adminId, feedback.getId());
        String contentStr = truncateContent(feedback.getContent());

        return AdminFeedbackItemVO.builder()
                .id(feedback.getId())
                .title(feedback.getTitle())
                .content(contentStr)
                .categoryName(categoryName)
                .gradeName(gradeAndClass[0])
                .className(gradeAndClass[1])
                .teacherName(teacherName)
                .studentName(studentName)
                .isAnonymous(feedback.getIsAnonymous() != null && feedback.getIsAnonymous() == 1)
                .status(feedback.getStatus())
                .hasUnread(feedback.getHasUnreadReply() != null && feedback.getHasUnreadReply() == 1)
                .isFavorited(isFavorited)
                .createTime(feedback.getCreateTime() != null ? sdf.format(feedback.getCreateTime()) : null)
                .build();
    }

    /** 构建反馈详情VO */
    private AdminFeedbackDetailVO buildDetailVO(FbFeedback feedback, Long adminId) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        Boolean isTeachingRelated = getIsTeachingRelated(feedback.getCategoryId());
        String studentName = getStudentName(feedback.getStudentId(), feedback.getIsAnonymous());
        String teacherName = getTeacherName(feedback.getTeacherId());
        String[] gradeAndClass = getGradeAndClassName(feedback.getStudentId());
        boolean isFavorited = checkIsFavorited(adminId, feedback.getId());
        List<AttachmentItemVO> attachments = getAttachments(feedback.getId());
        List<ReplyItemVO> replies = getReplies(feedback.getId());

        return AdminFeedbackDetailVO.builder()
                .id(feedback.getId())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .categoryId(feedback.getCategoryId())
                .categoryName(categoryName)
                .isTeachingRelated(isTeachingRelated)
                .teacherId(feedback.getTeacherId())
                .teacherName(teacherName)
                .studentName(studentName)
                .isAnonymous(feedback.getIsAnonymous() != null && feedback.getIsAnonymous() == 1)
                .status(feedback.getStatus())
                .gradeName(gradeAndClass[0])
                .className(gradeAndClass[1])
                .isFavorited(isFavorited)
                .attachments(attachments)
                .replies(replies)
                .createTime(feedback.getCreateTime() != null ? sdf.format(feedback.getCreateTime()) : null)
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
    private Boolean getIsTeachingRelated(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        FbCategory category = fbCategoryMapper.selectById(categoryId);
        return category != null && category.getIsTeachingRelated() != null
                && category.getIsTeachingRelated() == 1;
    }

    /** 获取学生姓名（匿名时返回"匿名"） */
    private String getStudentName(Long studentId, Integer isAnonymous) {
        if (isAnonymous != null && isAnonymous == 1) {
            return "匿名";
        }
        if (studentId == null) {
            return null;
        }
        SysUser student = sysUserMapper.selectById(studentId);
        return student != null ? student.getRealName() : null;
    }

    /** 获取教师姓名 */
    private String getTeacherName(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        SysUser teacher = sysUserMapper.selectById(teacherId);
        return teacher != null ? teacher.getRealName() : null;
    }

    /** 获取学生的年级和班级名称，返回 [gradeName, className] */
    private String[] getGradeAndClassName(Long studentId) {
        String[] result = new String[]{"", ""};
        if (studentId == null) {
            return result;
        }
        // 查询当前学期
        Long semesterId = getCurrentSemesterId();
        if (semesterId == null) {
            return result;
        }
        // 查询学生当前学期的班级
        LambdaQueryWrapper<BaseStudentClass> scWrapper = new LambdaQueryWrapper<>();
        scWrapper.eq(BaseStudentClass::getStudentId, studentId)
                 .eq(BaseStudentClass::getSemesterId, semesterId)
                 .last("LIMIT 1");
        BaseStudentClass studentClass = baseStudentClassMapper.selectOne(scWrapper);
        if (studentClass == null) {
            return result;
        }
        // 查询班级信息
        BaseClass baseClass = baseClassMapper.selectById(studentClass.getClassId());
        if (baseClass == null) {
            return result;
        }
        result[1] = baseClass.getClassName() != null ? baseClass.getClassName() : "";
        // 查询年级信息
        BaseGrade baseGrade = baseGradeMapper.selectById(baseClass.getGradeId());
        if (baseGrade != null && baseGrade.getGradeName() != null) {
            result[0] = baseGrade.getGradeName();
        }
        return result;
    }

    /** 获取当前学期ID */
    private Long getCurrentSemesterId() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSemester::getIsCurrent, 1).last("LIMIT 1");
        BaseSemester semester = baseSemesterMapper.selectOne(wrapper);
        return semester != null ? semester.getId() : null;
    }

    /** 检查当前管理员是否已收藏指定反馈 */
    private boolean checkIsFavorited(Long adminId, Long feedbackId) {
        LambdaQueryWrapper<FbCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCollection::getUserId, adminId)
               .eq(FbCollection::getFeedbackId, feedbackId);
        return fbCollectionMapper.selectCount(wrapper) > 0;
    }

    /** 截断内容为前30字 */
    private String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= CONTENT_TRUNCATE_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_TRUNCATE_LENGTH) + "...";
    }

    /** 查询反馈的附件列表 */
    private List<AttachmentItemVO> getAttachments(Long feedbackId) {
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

    /** 查询反馈的回复列表（按创建时间升序） */
    private List<ReplyItemVO> getReplies(Long feedbackId) {
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
                    .userType(user != null ? user.getUserType() : null)
                    .content(r.getContent())
                    .createTime(r.getCreateTime() != null ? sdf.format(r.getCreateTime()) : null)
                    .build();
        }).collect(Collectors.toList());
    }
}

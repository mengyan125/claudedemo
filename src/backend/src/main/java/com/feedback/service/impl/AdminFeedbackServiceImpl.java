package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feedback.common.exception.BusinessException;
import com.feedback.common.result.PageResult;
import com.feedback.mapper.*;
import com.feedback.model.dto.CreateReminderDTO;
import com.feedback.model.entity.*;
import com.feedback.model.vo.*;
import com.feedback.security.UserContext;
import com.feedback.service.AdminFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Autowired
    private FbReminderMapper fbReminderMapper;

    @Autowired
    private FbReminderReceiverMapper fbReminderReceiverMapper;

    @Autowired
    private FbCategoryAdminMapper fbCategoryAdminMapper;

    @Autowired
    private FbFeedbackAdminReadMapper fbFeedbackAdminReadMapper;

    @Override
    public PageResult<AdminFeedbackItemVO> getFeedbackList(Long categoryId, String status,
                                                            String keyword, Long gradeId,
                                                            Long classId, Long teacherId,
                                                            String dateStart, String dateEnd,
                                                            String replyStatus, Boolean isFavorited,
                                                            Integer pageNum, Integer pageSize) {
        Long adminId = UserContext.getCurrentUserId();
        // 构建查询条件
        LambdaQueryWrapper<FbFeedback> wrapper = buildListWrapper(categoryId, status, keyword,
                gradeId, classId, teacherId, dateStart, dateEnd, replyStatus, isFavorited, adminId);
        wrapper.orderByDesc(FbFeedback::getCreateTime);
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
        // CATEGORY_ADMIN 校验类别权限
        List<Long> allowedCategoryIds = getAllowedCategoryIds();
        if (allowedCategoryIds != null && !allowedCategoryIds.contains(feedback.getCategoryId())) {
            throw new BusinessException(403, "权限不足");
        }
        // 标记当前管理员已读（按用户隔离）
        markAdminFeedbackRead(adminId, id);
        // 标记该反馈的备注提醒为已读
        markRemindersAsRead(adminId, id);
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
        feedback.setHasUnreadForAdmin(0);
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

    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Long adminId = UserContext.getCurrentUserId();
        UserContext.UserInfo user = UserContext.getCurrentUser();
        if (user == null || user.getRoles() == null || !user.getRoles().contains("SYSTEM_ADMIN")) {
            throw new BusinessException(403, "仅系统管理员可删除反馈");
        }

        FbFeedback feedback = fbFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }

        // CATEGORY_ADMIN 做类别权限校验（理论上SYSTEM_ADMIN不会走到该限制，保留防御）
        List<Long> allowedCategoryIds = getAllowedCategoryIds();
        if (allowedCategoryIds != null && !allowedCategoryIds.contains(feedback.getCategoryId())) {
            throw new BusinessException(403, "权限不足");
        }

        // 删除收藏关系
        LambdaQueryWrapper<FbCollection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(FbCollection::getFeedbackId, feedbackId);
        fbCollectionMapper.delete(collectionWrapper);

        // 删除附件
        LambdaQueryWrapper<FbFeedbackAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
        attachmentWrapper.eq(FbFeedbackAttachment::getFeedbackId, feedbackId);
        fbFeedbackAttachmentMapper.delete(attachmentWrapper);

        // 删除回复
        LambdaQueryWrapper<FbReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(FbReply::getFeedbackId, feedbackId);
        fbReplyMapper.delete(replyWrapper);

        // 删除管理员已读记录
        LambdaQueryWrapper<FbFeedbackAdminRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(FbFeedbackAdminRead::getFeedbackId, feedbackId);
        fbFeedbackAdminReadMapper.delete(readWrapper);

        // 删除提醒接收人及提醒本体
        LambdaQueryWrapper<FbReminder> reminderQuery = new LambdaQueryWrapper<>();
        reminderQuery.eq(FbReminder::getFeedbackId, feedbackId).select(FbReminder::getId);
        List<Long> reminderIds = fbReminderMapper.selectList(reminderQuery).stream()
                .map(FbReminder::getId)
                .collect(Collectors.toList());
        if (!reminderIds.isEmpty()) {
            LambdaQueryWrapper<FbReminderReceiver> receiverWrapper = new LambdaQueryWrapper<>();
            receiverWrapper.in(FbReminderReceiver::getReminderId, reminderIds);
            fbReminderReceiverMapper.delete(receiverWrapper);

            LambdaQueryWrapper<FbReminder> reminderDelete = new LambdaQueryWrapper<>();
            reminderDelete.in(FbReminder::getId, reminderIds);
            fbReminderMapper.delete(reminderDelete);
        }

        // 删除反馈
        fbFeedbackMapper.deleteById(feedbackId);
    }

    // ==================== 状态数量统计 ====================

    @Override
    public FeedbackStatusCountVO getStatusCount(Long categoryId, String keyword, Long gradeId,
                                                  Long classId, Long teacherId,
                                                  String dateStart, String dateEnd) {
        Long adminId = UserContext.getCurrentUserId();
        // 构建基础查询条件（不含 status/replyStatus/isFavorited）
        LambdaQueryWrapper<FbFeedback> baseWrapper = buildListWrapper(categoryId, null, keyword,
                gradeId, classId, teacherId, dateStart, dateEnd, null, null, adminId);
        Long totalCount = fbFeedbackMapper.selectCount(baseWrapper);

        // 已回复
        LambdaQueryWrapper<FbFeedback> repliedWrapper = buildListWrapper(categoryId, null, keyword,
                gradeId, classId, teacherId, dateStart, dateEnd, "replied", null, adminId);
        Long repliedCount = fbFeedbackMapper.selectCount(repliedWrapper);

        // 未回复
        Long unrepliedCount = totalCount - repliedCount;

        return FeedbackStatusCountVO.builder()
                .totalCount(totalCount)
                .repliedCount(repliedCount)
                .unrepliedCount(unrepliedCount)
                .build();
    }

    // ==================== 备注提醒功能 ====================

    @Override
    @Transactional
    public void sendReminder(Long feedbackId, CreateReminderDTO dto) {
        Long senderId = UserContext.getCurrentUserId();
        FbFeedback feedback = fbFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        // 插入提醒记录
        FbReminder reminder = new FbReminder();
        reminder.setFeedbackId(feedbackId);
        reminder.setSenderId(senderId);
        reminder.setContent(dto.getContent());
        reminder.setCreateTime(new Date());
        fbReminderMapper.insert(reminder);
        // 批量插入接收人（至少包含发送人本人，确保当前页可见）
        List<Long> receiverIds = dto.getReceiverIds();
        List<Long> targetReceiverIds = new ArrayList<>();
        if (receiverIds != null && !receiverIds.isEmpty()) {
            targetReceiverIds.addAll(receiverIds);
        }
        if (!targetReceiverIds.contains(senderId)) {
            targetReceiverIds.add(senderId);
        }
        for (Long receiverId : targetReceiverIds) {
            FbReminderReceiver receiver = new FbReminderReceiver();
            receiver.setReminderId(reminder.getId());
            receiver.setReceiverId(receiverId);
            receiver.setIsRead(0);
            fbReminderReceiverMapper.insert(receiver);
        }
    }

    @Override
    public PageResult<ReminderItemVO> getReminderList(Integer pageNum,
                                                       Integer pageSize) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Long currentUserId = UserContext.getCurrentUserId();
        // 查询当前用户收到的提醒
        LambdaQueryWrapper<FbReminderReceiver> rrWrapper =
                new LambdaQueryWrapper<>();
        rrWrapper.eq(FbReminderReceiver::getReceiverId, currentUserId);
        // 分页查询接收记录
        Page<FbReminderReceiver> page = new Page<>(pageNum, pageSize);
        Page<FbReminderReceiver> rrPage =
                fbReminderReceiverMapper.selectPage(page, rrWrapper);
        List<ReminderItemVO> voList = new ArrayList<>();
        for (FbReminderReceiver rr : rrPage.getRecords()) {
            FbReminder reminder =
                    fbReminderMapper.selectById(rr.getReminderId());
            if (reminder == null) {
                continue;
            }
            FbFeedback feedback =
                    fbFeedbackMapper.selectById(reminder.getFeedbackId());
            SysUser sender =
                    sysUserMapper.selectById(reminder.getSenderId());
            voList.add(ReminderItemVO.builder()
                    .id(reminder.getId())
                    .feedbackId(reminder.getFeedbackId())
                    .feedbackTitle(feedback != null
                            ? feedback.getTitle() : null)
                    .senderId(reminder.getSenderId())
                    .senderName(sender != null
                            ? sender.getRealName() : null)
                    .content(reminder.getContent())
                    .isRead(rr.getIsRead() != null
                            && rr.getIsRead() == 1)
                    .createTime(reminder.getCreateTime() != null
                            ? sdf.format(reminder.getCreateTime()) : null)
                    .build());
            // 标记为已读
            if (rr.getIsRead() == null || rr.getIsRead() == 0) {
                rr.setIsRead(1);
                fbReminderReceiverMapper.updateById(rr);
            }
        }
        return PageResult.of(voList, rrPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<UserSearchVO> searchUsers(String keyword, String userType) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 按用户类型过滤
        if (StringUtils.hasText(userType)) {
            wrapper.eq(SysUser::getUserType, userType);
        } else {
            // 默认只搜索非学生用户（管理员和教师）
            wrapper.ne(SysUser::getUserType, "student");
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getUsername, keyword));
        }
        wrapper.last("LIMIT 50");
        List<SysUser> users = sysUserMapper.selectList(wrapper);
        return users.stream().map(u -> UserSearchVO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .realName(u.getRealName())
                .userType(u.getUserType())
                .build()
        ).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /** 构建反馈列表查询条件（不含排序，调用方按需自行添加） */
    private LambdaQueryWrapper<FbFeedback> buildListWrapper(Long categoryId, String status,
                                                             String keyword, Long gradeId,
                                                             Long classId, Long teacherId,
                                                             String dateStart, String dateEnd,
                                                             String replyStatus, Boolean isFavorited,
                                                             Long adminId) {
        LambdaQueryWrapper<FbFeedback> wrapper = new LambdaQueryWrapper<>();
        // 过滤掉草稿
        wrapper.ne(FbFeedback::getStatus, "draft");

        // CATEGORY_ADMIN 类别数据过滤
        List<Long> allowedCategoryIds = getAllowedCategoryIds();
        if (allowedCategoryIds != null) {
            if (allowedCategoryIds.isEmpty()) {
                wrapper.eq(FbFeedback::getId, -1L);
                return wrapper;
            }
            wrapper.in(FbFeedback::getCategoryId, allowedCategoryIds);
        }

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
        // 反馈对象（教师）
        if (teacherId != null) {
            wrapper.eq(FbFeedback::getTeacherId, teacherId);
        }
        // 回复情况
        if (StringUtils.hasText(replyStatus)) {
            wrapper.eq(FbFeedback::getReplyStatus, replyStatus);
        }
        // 时间范围
        if (StringUtils.hasText(dateStart)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date start = sdf.parse(dateStart);
                wrapper.ge(FbFeedback::getCreateTime, start);
            } catch (Exception ignored) { }
        }
        if (StringUtils.hasText(dateEnd)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                // 结束日期取当天最后时刻
                Date end = new Date(sdf.parse(dateEnd).getTime() + 24 * 60 * 60 * 1000 - 1);
                wrapper.le(FbFeedback::getCreateTime, end);
            } catch (Exception ignored) { }
        }
        // 年级/班级筛选：通过 base_student_class + base_class 关联查 studentId 集合
        if (gradeId != null || classId != null) {
            List<Long> studentIds = getStudentIdsByGradeOrClass(gradeId, classId);
            if (studentIds.isEmpty()) {
                // 无匹配学生，直接返回空结果
                wrapper.eq(FbFeedback::getId, -1L);
            } else {
                wrapper.in(FbFeedback::getStudentId, studentIds);
            }
        }
        // 仅查看收藏
        if (isFavorited != null && isFavorited) {
            List<Long> favoriteIds = getFavoriteIds(adminId);
            if (favoriteIds.isEmpty()) {
                wrapper.eq(FbFeedback::getId, -1L);
            } else {
                wrapper.in(FbFeedback::getId, favoriteIds);
            }
        }
        return wrapper;
    }

    /** 根据年级/班级获取学生ID集合 */
    private List<Long> getStudentIdsByGradeOrClass(Long gradeId, Long classId) {
        Long semesterId = getCurrentSemesterId();
        if (semesterId == null) {
            return new ArrayList<>();
        }
        // 如果指定了班级ID，直接查
        if (classId != null) {
            LambdaQueryWrapper<BaseStudentClass> scWrapper = new LambdaQueryWrapper<>();
            scWrapper.eq(BaseStudentClass::getClassId, classId)
                     .eq(BaseStudentClass::getSemesterId, semesterId);
            return baseStudentClassMapper.selectList(scWrapper).stream()
                    .map(BaseStudentClass::getStudentId)
                    .collect(Collectors.toList());
        }
        // 只指定了年级ID，先查该年级下所有班级
        LambdaQueryWrapper<BaseClass> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(BaseClass::getGradeId, gradeId);
        List<Long> classIds = baseClassMapper.selectList(classWrapper).stream()
                .map(BaseClass::getId)
                .collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BaseStudentClass> scWrapper = new LambdaQueryWrapper<>();
        scWrapper.in(BaseStudentClass::getClassId, classIds)
                 .eq(BaseStudentClass::getSemesterId, semesterId);
        return baseStudentClassMapper.selectList(scWrapper).stream()
                .map(BaseStudentClass::getStudentId)
                .collect(Collectors.toList());
    }

    /** 获取当前管理员收藏的反馈ID集合 */
    private List<Long> getFavoriteIds(Long adminId) {
        LambdaQueryWrapper<FbCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCollection::getUserId, adminId);
        return fbCollectionMapper.selectList(wrapper).stream()
                .map(FbCollection::getFeedbackId)
                .collect(Collectors.toList());
    }

    /** 将反馈实体转换为列表项VO */
    private AdminFeedbackItemVO convertToItemVO(FbFeedback feedback, Long adminId) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        String studentName = getStudentName(feedback.getStudentId(), feedback.getIsAnonymous());
        String teacherName = getTeacherName(feedback.getTeacherId());
        String[] gradeAndClass = getGradeAndClassName(feedback.getStudentId());
        boolean isFavorited = checkIsFavorited(adminId, feedback.getId());
        boolean hasUnreadReminder = checkHasUnreadReminder(adminId, feedback.getId());
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
                .hasUnread(checkHasUnreadForAdmin(adminId, feedback))
                .hasUnreadReminder(hasUnreadReminder)
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

    /** 检查管理员列表未读状态（兼容历史字段） */
    private boolean checkHasUnreadForAdmin(Long adminId, FbFeedback feedback) {
        if (feedback.getHasUnreadForAdmin() == null || feedback.getHasUnreadForAdmin() != 1) {
            return false;
        }
        LambdaQueryWrapper<FbFeedbackAdminRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedbackAdminRead::getFeedbackId, feedback.getId())
               .eq(FbFeedbackAdminRead::getUserId, adminId)
               .last("LIMIT 1");
        return fbFeedbackAdminReadMapper.selectOne(wrapper) == null;
    }

    /** 记录管理员已读（幂等） */
    private void markAdminFeedbackRead(Long adminId, Long feedbackId) {
        LambdaQueryWrapper<FbFeedbackAdminRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedbackAdminRead::getFeedbackId, feedbackId)
               .eq(FbFeedbackAdminRead::getUserId, adminId)
               .last("LIMIT 1");
        FbFeedbackAdminRead existing = fbFeedbackAdminReadMapper.selectOne(wrapper);
        if (existing != null) {
            return;
        }
        FbFeedbackAdminRead record = new FbFeedbackAdminRead();
        record.setFeedbackId(feedbackId);
        record.setUserId(adminId);
        record.setReadTime(new Date());
        fbFeedbackAdminReadMapper.insert(record);
    }

    /** 检查指定反馈是否有当前用户未读的备注提醒 */
    private boolean checkHasUnreadReminder(Long userId, Long feedbackId) {
        // 查该反馈下所有提醒ID
        LambdaQueryWrapper<FbReminder> rWrapper = new LambdaQueryWrapper<>();
        rWrapper.eq(FbReminder::getFeedbackId, feedbackId).select(FbReminder::getId);
        List<Long> reminderIds = fbReminderMapper.selectList(rWrapper).stream()
                .map(FbReminder::getId).collect(Collectors.toList());
        if (reminderIds.isEmpty()) {
            return false;
        }
        // 查接收人表中是否有未读记录
        LambdaQueryWrapper<FbReminderReceiver> rrWrapper = new LambdaQueryWrapper<>();
        rrWrapper.in(FbReminderReceiver::getReminderId, reminderIds)
                 .eq(FbReminderReceiver::getReceiverId, userId)
                 .eq(FbReminderReceiver::getIsRead, 0);
        return fbReminderReceiverMapper.selectCount(rrWrapper) > 0;
    }

    /** 标记指定反馈的备注提醒为已读 */
    private void markRemindersAsRead(Long userId, Long feedbackId) {
        LambdaQueryWrapper<FbReminder> rWrapper = new LambdaQueryWrapper<>();
        rWrapper.eq(FbReminder::getFeedbackId, feedbackId).select(FbReminder::getId);
        List<Long> reminderIds = fbReminderMapper.selectList(rWrapper).stream()
                .map(FbReminder::getId).collect(Collectors.toList());
        if (reminderIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<FbReminderReceiver> rrWrapper = new LambdaQueryWrapper<>();
        rrWrapper.in(FbReminderReceiver::getReminderId, reminderIds)
                 .eq(FbReminderReceiver::getReceiverId, userId)
                 .eq(FbReminderReceiver::getIsRead, 0);
        List<FbReminderReceiver> receivers = fbReminderReceiverMapper.selectList(rrWrapper);
        for (FbReminderReceiver receiver : receivers) {
            receiver.setIsRead(1);
            fbReminderReceiverMapper.updateById(receiver);
        }
    }

    /**
     * 获取当前用户允许访问的类别ID列表
     * SYSTEM_ADMIN / ROLE_ADMIN 返回 null（不过滤）
     * CATEGORY_ADMIN 返回 fb_category_admin 中被分配的类别ID
     */
    private List<Long> getAllowedCategoryIds() {
        UserContext.UserInfo user = UserContext.getCurrentUser();
        List<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        // SYSTEM_ADMIN 或 ROLE_ADMIN 不做类别限制
        if (roles.contains("SYSTEM_ADMIN") || roles.contains("ROLE_ADMIN")) {
            return null;
        }
        // CATEGORY_ADMIN 查询被分配的类别
        LambdaQueryWrapper<FbCategoryAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCategoryAdmin::getUserId, user.getId());
        return fbCategoryAdminMapper.selectList(wrapper).stream()
                .map(FbCategoryAdmin::getCategoryId)
                .collect(Collectors.toList());
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

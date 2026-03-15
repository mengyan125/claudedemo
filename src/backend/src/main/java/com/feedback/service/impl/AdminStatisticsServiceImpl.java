package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.*;
import com.feedback.model.entity.*;
import com.feedback.model.vo.AdminFeedbackItemVO;
import com.feedback.model.vo.FeedbackStatisticsVO;
import com.feedback.model.vo.FeedbackStatisticsVO.*;
import com.feedback.model.vo.TeacherFeedbackResultVO;
import com.feedback.security.UserContext;
import com.feedback.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员端-统计服务实现类
 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int CONTENT_TRUNCATE_LENGTH = 30;

    @Autowired
    private FbFeedbackMapper fbFeedbackMapper;

    @Autowired
    private FbCategoryMapper fbCategoryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BaseSemesterMapper baseSemesterMapper;

    @Autowired
    private BaseTeacherClassMapper baseTeacherClassMapper;

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Autowired
    private BaseGradeMapper baseGradeMapper;

    @Autowired
    private BaseStudentClassMapper baseStudentClassMapper;

    @Autowired
    private FbCollectionMapper fbCollectionMapper;

    @Override
    public FeedbackStatisticsVO getStatistics() {
        // 查询所有非草稿反馈
        List<FbFeedback> allFeedbacks = queryNonDraftFeedbacks();
        int totalCount = allFeedbacks.size();

        // 当前学期反馈数
        int semesterCount = countSemesterFeedbacks(allFeedbacks);

        // 本月反馈数
        int monthCount = countMonthFeedbacks(allFeedbacks);

        // 类别分布
        List<CategoryDistributionItem> categoryDistribution =
                buildCategoryDistribution(allFeedbacks, totalCount);

        // 学期趋势
        List<SemesterTrendItem> semesterTrend = buildSemesterTrend(allFeedbacks);

        // 教师Top10
        List<TeacherTop10Item> teacherTop10 = buildTeacherTop10(allFeedbacks);

        return FeedbackStatisticsVO.builder()
                .totalCount(totalCount)
                .semesterCount(semesterCount)
                .monthCount(monthCount)
                .categoryDistribution(categoryDistribution)
                .semesterTrend(semesterTrend)
                .teacherTop10(teacherTop10)
                .build();
    }

    @Override
    public List<TeacherTop10Item> getTeacherTop10(Date startDate, Date endDate) {
        List<FbFeedback> feedbacks = queryNonDraftFeedbacks();
        // 按时间筛选
        if (startDate != null || endDate != null) {
            feedbacks = feedbacks.stream().filter(fb -> {
                if (fb.getCreateTime() == null) return false;
                if (startDate != null && fb.getCreateTime().before(startDate)) return false;
                if (endDate != null && fb.getCreateTime().after(endDate)) return false;
                return true;
            }).collect(Collectors.toList());
        }
        return buildTeacherTop10(feedbacks);
    }

    @Override
    public TeacherFeedbackResultVO getTeacherFeedbackList(Long teacherId) {
        // 查询教师信息
        SysUser teacher = sysUserMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        // 获取教师科目
        String subject = getTeacherSubject(teacherId);

        // 查询该教师的所有非草稿反馈
        List<FbFeedback> feedbacks = queryTeacherFeedbacks(teacherId);

        // 构建教师信息
        TeacherFeedbackResultVO.TeacherInfoItem teacherInfo =
                new TeacherFeedbackResultVO.TeacherInfoItem(
                        teacher.getId(), teacher.getRealName(),
                        subject, feedbacks.size());

        // 转换反馈列表为VO
        Long adminId = UserContext.getCurrentUserId();
        List<AdminFeedbackItemVO> voList = feedbacks.stream()
                .map(fb -> convertToItemVO(fb, adminId))
                .collect(Collectors.toList());

        return TeacherFeedbackResultVO.builder()
                .teacher(teacherInfo)
                .list(voList)
                .build();
    }

    // ==================== 统计私有方法 ====================

    /** 查询所有非草稿反馈 */
    private List<FbFeedback> queryNonDraftFeedbacks() {
        LambdaQueryWrapper<FbFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(FbFeedback::getStatus, "draft");
        return fbFeedbackMapper.selectList(wrapper);
    }

    /** 统计当前学期反馈数 */
    private int countSemesterFeedbacks(List<FbFeedback> allFeedbacks) {
        BaseSemester current = getCurrentSemester();
        if (current == null) {
            return 0;
        }
        Date start = current.getStartDate();
        Date end = current.getEndDate();
        return (int) allFeedbacks.stream()
                .filter(fb -> fb.getCreateTime() != null
                        && !fb.getCreateTime().before(start)
                        && !fb.getCreateTime().after(end))
                .count();
    }

    /** 统计本月反馈数 */
    private int countMonthFeedbacks(List<FbFeedback> allFeedbacks) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        return (int) allFeedbacks.stream()
                .filter(fb -> {
                    if (fb.getCreateTime() == null) {
                        return false;
                    }
                    Calendar fbCal = Calendar.getInstance();
                    fbCal.setTime(fb.getCreateTime());
                    return fbCal.get(Calendar.YEAR) == year
                            && fbCal.get(Calendar.MONTH) == month;
                })
                .count();
    }

    /** 构建类别分布统计 */
    private List<CategoryDistributionItem> buildCategoryDistribution(
            List<FbFeedback> allFeedbacks, int totalCount) {
        // 按类别ID分组统计
        Map<Long, Long> categoryCountMap = allFeedbacks.stream()
                .filter(fb -> fb.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        FbFeedback::getCategoryId, Collectors.counting()));

        // 查询所有类别
        List<FbCategory> categories = fbCategoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(FbCategory::getId, FbCategory::getName));

        return categoryCountMap.entrySet().stream()
                .map(entry -> {
                    String name = categoryNameMap.getOrDefault(entry.getKey(), "未知类别");
                    int count = entry.getValue().intValue();
                    double percentage = totalCount > 0
                            ? Math.round(count * 10000.0 / totalCount) / 100.0
                            : 0.0;
                    return new CategoryDistributionItem(name, count, percentage);
                })
                .sorted((a, b) -> b.getCount() - a.getCount())
                .collect(Collectors.toList());
    }

    /** 构建本学期每月反馈统计 */
    private List<SemesterTrendItem> buildSemesterTrend(
            List<FbFeedback> allFeedbacks) {
        BaseSemester current = getCurrentSemester();
        if (current == null) {
            return new ArrayList<>();
        }
        Date semesterStart = current.getStartDate();
        Date semesterEnd = current.getEndDate();

        // 筛选当前学期内的反馈
        List<FbFeedback> semesterFeedbacks = allFeedbacks.stream()
                .filter(fb -> fb.getCreateTime() != null
                        && !fb.getCreateTime().before(semesterStart)
                        && !fb.getCreateTime().after(semesterEnd))
                .collect(Collectors.toList());

        // 生成学期内每个月的统计
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(semesterStart);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(semesterEnd);

        List<SemesterTrendItem> result = new ArrayList<>();
        Calendar cursor = (Calendar) startCal.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);

        while (!cursor.after(endCal)) {
            int year = cursor.get(Calendar.YEAR);
            int month = cursor.get(Calendar.MONTH);
            String label = (month + 1) + "月";

            int count = (int) semesterFeedbacks.stream()
                    .filter(fb -> {
                        Calendar fbCal = Calendar.getInstance();
                        fbCal.setTime(fb.getCreateTime());
                        return fbCal.get(Calendar.YEAR) == year
                                && fbCal.get(Calendar.MONTH) == month;
                    })
                    .count();

            result.add(new SemesterTrendItem(label, count));
            cursor.add(Calendar.MONTH, 1);
        }
        return result;
    }

    /** 构建教师被反馈Top10（支持时间筛选） */
    private List<TeacherTop10Item> buildTeacherTop10(
            List<FbFeedback> feedbacks) {
        // 按教师ID分组，同时收集反馈列表用于统计类别
        Map<Long, List<FbFeedback>> teacherFeedbackMap = feedbacks.stream()
                .filter(fb -> fb.getTeacherId() != null)
                .collect(Collectors.groupingBy(FbFeedback::getTeacherId));

        // 查询所有类别名称
        List<FbCategory> categories = fbCategoryMapper.selectList(null);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(FbCategory::getId, FbCategory::getName));

        // 按数量排序取前10
        List<Map.Entry<Long, List<FbFeedback>>> top10Entries = teacherFeedbackMap.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(10)
                .collect(Collectors.toList());

        List<TeacherTop10Item> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, List<FbFeedback>> entry : top10Entries) {
            Long teacherId = entry.getKey();
            List<FbFeedback> teacherFbs = entry.getValue();
            int count = teacherFbs.size();
            // 统计该教师被反馈最多的类别
            String topCategoryName = getTopCategoryName(teacherFbs, categoryNameMap);
            TeacherTop10Item item = buildTeacherTop10Item(
                    rank++, teacherId, count, topCategoryName);
            result.add(item);
        }
        return result;
    }

    /** 获取反馈列表中最多的类别名称 */
    private String getTopCategoryName(List<FbFeedback> feedbacks,
                                       Map<Long, String> categoryNameMap) {
        return feedbacks.stream()
                .filter(fb -> fb.getCategoryId() != null)
                .collect(Collectors.groupingBy(FbFeedback::getCategoryId, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> categoryNameMap.getOrDefault(e.getKey(), "未知类别"))
                .orElse("");
    }

    /** 构建单个教师Top10项 */
    private TeacherTop10Item buildTeacherTop10Item(
            int rank, Long teacherId, int count, String categoryName) {
        SysUser teacher = sysUserMapper.selectById(teacherId);
        String teacherName = teacher != null ? teacher.getRealName() : "未知教师";

        // 查询教师的科目、班级、年级
        String subject = "";
        String gradeName = "";
        String className = "";
        LambdaQueryWrapper<BaseTeacherClass> tcWrapper = new LambdaQueryWrapper<>();
        tcWrapper.eq(BaseTeacherClass::getTeacherId, teacherId).last("LIMIT 1");
        BaseTeacherClass tc = baseTeacherClassMapper.selectOne(tcWrapper);
        if (tc != null) {
            subject = tc.getSubject() != null ? tc.getSubject() : "";
            BaseClass baseClass = baseClassMapper.selectById(tc.getClassId());
            if (baseClass != null) {
                className = baseClass.getClassName() != null
                        ? baseClass.getClassName() : "";
                BaseGrade grade = baseGradeMapper.selectById(baseClass.getGradeId());
                if (grade != null) {
                    gradeName = grade.getGradeName() != null
                            ? grade.getGradeName() : "";
                }
            }
        }

        return new TeacherTop10Item(
                rank, teacherId, teacherName, subject, gradeName, className, count, categoryName);
    }

    // ==================== 教师反馈列表私有方法 ====================

    /** 查询教师的非草稿反馈 */
    private List<FbFeedback> queryTeacherFeedbacks(Long teacherId) {
        LambdaQueryWrapper<FbFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbFeedback::getTeacherId, teacherId)
               .ne(FbFeedback::getStatus, "draft")
               .orderByDesc(FbFeedback::getCreateTime);
        return fbFeedbackMapper.selectList(wrapper);
    }

    /** 获取教师科目 */
    private String getTeacherSubject(Long teacherId) {
        LambdaQueryWrapper<BaseTeacherClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseTeacherClass::getTeacherId, teacherId).last("LIMIT 1");
        BaseTeacherClass tc = baseTeacherClassMapper.selectOne(wrapper);
        return tc != null ? tc.getSubject() : null;
    }

    /** 获取当前学期 */
    private BaseSemester getCurrentSemester() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSemester::getIsCurrent, 1).last("LIMIT 1");
        return baseSemesterMapper.selectOne(wrapper);
    }

    /** 将反馈实体转换为列表项VO */
    private AdminFeedbackItemVO convertToItemVO(FbFeedback feedback, Long adminId) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String categoryName = getCategoryName(feedback.getCategoryId());
        String studentName = getStudentDisplayName(
                feedback.getStudentId(), feedback.getIsAnonymous());
        String teacherName = getUserRealName(feedback.getTeacherId());
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
                .isAnonymous(feedback.getIsAnonymous() != null
                        && feedback.getIsAnonymous() == 1)
                .status(feedback.getStatus())
                .hasUnread(feedback.getHasUnreadForAdmin() != null
                        && feedback.getHasUnreadForAdmin() == 1)
                .isFavorited(isFavorited)
                .createTime(feedback.getCreateTime() != null
                        ? sdf.format(feedback.getCreateTime()) : null)
                .build();
    }

    // ==================== 通用私有方法 ====================

    /** 获取类别名称 */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        FbCategory category = fbCategoryMapper.selectById(categoryId);
        return category != null ? category.getName() : null;
    }

    /** 获取学生展示名称（匿名时返回"匿名"） */
    private String getStudentDisplayName(Long studentId, Integer isAnonymous) {
        if (isAnonymous != null && isAnonymous == 1) {
            return "匿名";
        }
        return getUserRealName(studentId);
    }

    /** 获取用户真实姓名 */
    private String getUserRealName(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRealName() : null;
    }

    /** 获取学生的年级和班级名称 */
    private String[] getGradeAndClassName(Long studentId) {
        String[] result = new String[]{"", ""};
        if (studentId == null) {
            return result;
        }
        BaseSemester current = getCurrentSemester();
        if (current == null) {
            return result;
        }
        LambdaQueryWrapper<BaseStudentClass> scWrapper = new LambdaQueryWrapper<>();
        scWrapper.eq(BaseStudentClass::getStudentId, studentId)
                 .eq(BaseStudentClass::getSemesterId, current.getId())
                 .last("LIMIT 1");
        BaseStudentClass sc = baseStudentClassMapper.selectOne(scWrapper);
        if (sc == null) {
            return result;
        }
        BaseClass baseClass = baseClassMapper.selectById(sc.getClassId());
        if (baseClass == null) {
            return result;
        }
        result[1] = baseClass.getClassName() != null ? baseClass.getClassName() : "";
        BaseGrade grade = baseGradeMapper.selectById(baseClass.getGradeId());
        if (grade != null && grade.getGradeName() != null) {
            result[0] = grade.getGradeName();
        }
        return result;
    }

    /** 检查是否已收藏 */
    private boolean checkIsFavorited(Long adminId, Long feedbackId) {
        LambdaQueryWrapper<FbCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FbCollection::getUserId, adminId)
               .eq(FbCollection::getFeedbackId, feedbackId);
        return fbCollectionMapper.selectCount(wrapper) > 0;
    }

    /** 截断内容 */
    private String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= CONTENT_TRUNCATE_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_TRUNCATE_LENGTH) + "...";
    }
}

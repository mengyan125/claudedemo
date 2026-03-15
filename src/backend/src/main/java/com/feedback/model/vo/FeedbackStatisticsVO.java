package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 反馈统计概览VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatisticsVO {

    /** 总反馈数 */
    private Integer totalCount;

    /** 本学期反馈数 */
    private Integer semesterCount;

    /** 本月反馈数 */
    private Integer monthCount;

    /** 类别分布统计 */
    private List<CategoryDistributionItem> categoryDistribution;

    /** 学期趋势统计 */
    private List<SemesterTrendItem> semesterTrend;

    /** 教师被反馈Top10 */
    private List<TeacherTop10Item> teacherTop10;

    /**
     * 类别分布统计项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDistributionItem {

        /** 类别名称 */
        private String name;

        /** 反馈数量 */
        private Integer count;

        /** 占比百分比 */
        private Double percentage;
    }

    /**
     * 学期趋势统计项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterTrendItem {

        /** 学期名称 */
        private String semester;

        /** 反馈数量 */
        private Integer count;
    }

    /**
     * 教师被反馈Top10项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherTop10Item {

        /** 排名 */
        private Integer rank;

        /** 教师ID */
        private Long teacherId;

        /** 教师姓名 */
        private String teacherName;

        /** 科目 */
        private String subject;

        /** 年级名称 */
        private String gradeName;

        /** 班级名称 */
        private String className;

        /** 反馈数量 */
        private Integer count;
    }
}

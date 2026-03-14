package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教师被反馈结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherFeedbackResultVO {

    /** 教师信息 */
    private TeacherInfoItem teacher;

    /** 反馈列表（复用管理员端反馈列表项VO） */
    private List<AdminFeedbackItemVO> list;

    /**
     * 教师信息项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfoItem {

        /** 教师ID */
        private Long id;

        /** 教师姓名 */
        private String name;

        /** 科目 */
        private String subject;

        /** 被反馈总数 */
        private Integer totalCount;
    }
}

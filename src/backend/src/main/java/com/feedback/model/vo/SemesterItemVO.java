package com.feedback.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学期列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterItemVO {

    private Long id;

    private String semesterName;

    private String startDate;

    private String endDate;

    private Boolean isCurrent;
}

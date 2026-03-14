package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.BaseGrade;
import org.apache.ibatis.annotations.Mapper;

/**
 * 年级Mapper接口
 */
@Mapper
public interface BaseGradeMapper extends BaseMapper<BaseGrade> {
}

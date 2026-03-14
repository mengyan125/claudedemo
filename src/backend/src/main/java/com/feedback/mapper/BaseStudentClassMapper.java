package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.BaseStudentClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班级关联Mapper接口
 */
@Mapper
public interface BaseStudentClassMapper extends BaseMapper<BaseStudentClass> {
}

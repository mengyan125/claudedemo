package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.BaseTeacherClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师班级关联Mapper接口
 */
@Mapper
public interface BaseTeacherClassMapper extends BaseMapper<BaseTeacherClass> {
}

package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈Mapper接口
 */
@Mapper
public interface FbFeedbackMapper extends BaseMapper<FbFeedback> {
}

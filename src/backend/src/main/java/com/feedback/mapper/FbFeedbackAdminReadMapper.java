package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbFeedbackAdminRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员反馈已读记录Mapper接口
 */
@Mapper
public interface FbFeedbackAdminReadMapper extends BaseMapper<FbFeedbackAdminRead> {
}

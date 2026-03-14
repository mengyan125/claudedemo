package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbFeedbackAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈附件Mapper接口
 */
@Mapper
public interface FbFeedbackAttachmentMapper extends BaseMapper<FbFeedbackAttachment> {
}

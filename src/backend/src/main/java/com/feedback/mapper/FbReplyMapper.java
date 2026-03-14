package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbReply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈回复Mapper接口
 */
@Mapper
public interface FbReplyMapper extends BaseMapper<FbReply> {
}

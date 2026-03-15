package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbReminderReceiver;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒接收人Mapper接口
 */
@Mapper
public interface FbReminderReceiverMapper extends BaseMapper<FbReminderReceiver> {
}

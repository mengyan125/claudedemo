package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbReminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备注提醒Mapper接口
 */
@Mapper
public interface FbReminderMapper extends BaseMapper<FbReminder> {
}

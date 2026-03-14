package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbCategoryAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 类别管理员关联Mapper
 */
@Mapper
public interface FbCategoryAdminMapper extends BaseMapper<FbCategoryAdmin> {
}

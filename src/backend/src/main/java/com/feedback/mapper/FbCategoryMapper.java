package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.FbCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 反馈类别Mapper接口
 */
@Mapper
public interface FbCategoryMapper extends BaseMapper<FbCategory> {

    /**
     * 根据类别ID统计反馈数量
     *
     * @param categoryId 类别ID
     * @return 反馈数量
     */
    @Select("SELECT COUNT(*) FROM fb_feedback WHERE category_id = #{categoryId}")
    Long countFeedbackByCategoryId(@Param("categoryId") Long categoryId);
}

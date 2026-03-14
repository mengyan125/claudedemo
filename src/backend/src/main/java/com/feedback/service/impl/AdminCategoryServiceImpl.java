package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.FbCategoryMapper;
import com.feedback.model.dto.CreateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryDTO;
import com.feedback.model.dto.UpdateCategoryStatusDTO;
import com.feedback.model.entity.FbCategory;
import com.feedback.model.vo.CategoryItemVO;
import com.feedback.service.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理端-反馈类别服务实现类
 * 处理反馈类别的增删改查业务逻辑
 */
@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    @Autowired
    private FbCategoryMapper fbCategoryMapper;

    @Override
    public List<CategoryItemVO> getCategoryList() {
        LambdaQueryWrapper<FbCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FbCategory::getSortOrder);
        List<FbCategory> categories = fbCategoryMapper.selectList(wrapper);

        List<CategoryItemVO> result = new ArrayList<>();
        for (FbCategory category : categories) {
            Long feedbackCount = fbCategoryMapper.countFeedbackByCategoryId(category.getId());
            CategoryItemVO vo = CategoryItemVO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .isTeachingRelated(category.getIsTeachingRelated() != null && category.getIsTeachingRelated() == 1)
                    .sortOrder(category.getSortOrder())
                    .status(category.getStatus())
                    .feedbackCount(feedbackCount != null ? feedbackCount : 0L)
                    .build();
            result.add(vo);
        }
        return result;
    }

    @Override
    public void createCategory(CreateCategoryDTO dto) {
        FbCategory category = new FbCategory();
        category.setName(dto.getName());
        category.setIsTeachingRelated(Boolean.TRUE.equals(dto.getIsTeachingRelated()) ? 1 : 0);
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus());
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        fbCategoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, UpdateCategoryDTO dto) {
        FbCategory category = fbCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("类别不存在");
        }

        if (StringUtils.hasText(dto.getName())) {
            category.setName(dto.getName());
        }
        if (dto.getIsTeachingRelated() != null) {
            category.setIsTeachingRelated(Boolean.TRUE.equals(dto.getIsTeachingRelated()) ? 1 : 0);
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        category.setUpdateTime(new Date());
        fbCategoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        FbCategory category = fbCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("类别不存在");
        }

        // 检查是否有反馈数据引用此类别
        Long feedbackCount = fbCategoryMapper.countFeedbackByCategoryId(id);
        if (feedbackCount != null && feedbackCount > 0) {
            throw new BusinessException("该类别下存在反馈数据，无法删除");
        }

        fbCategoryMapper.deleteById(id);
    }

    @Override
    public void updateCategoryStatus(Long id, UpdateCategoryStatusDTO dto) {
        FbCategory category = fbCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("类别不存在");
        }

        category.setStatus(dto.getStatus());
        category.setUpdateTime(new Date());
        fbCategoryMapper.updateById(category);
    }
}

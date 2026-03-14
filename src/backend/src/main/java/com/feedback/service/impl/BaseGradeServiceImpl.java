package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.BaseClassMapper;
import com.feedback.mapper.BaseGradeMapper;
import com.feedback.model.dto.CreateGradeDTO;
import com.feedback.model.dto.UpdateGradeDTO;
import com.feedback.model.entity.BaseClass;
import com.feedback.model.entity.BaseGrade;
import com.feedback.service.BaseGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 年级服务实现类
 * 处理年级的增删改查业务逻辑
 */
@Service
public class BaseGradeServiceImpl implements BaseGradeService {

    @Autowired
    private BaseGradeMapper baseGradeMapper;

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Override
    public List<BaseGrade> getGradeList() {
        LambdaQueryWrapper<BaseGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BaseGrade::getSortOrder);
        return baseGradeMapper.selectList(wrapper);
    }

    @Override
    public void createGrade(CreateGradeDTO dto) {
        BaseGrade grade = new BaseGrade();
        grade.setGradeName(dto.getGradeName());
        grade.setSortOrder(dto.getSortOrder());
        grade.setCreateTime(new Date());
        grade.setUpdateTime(new Date());
        baseGradeMapper.insert(grade);
    }

    @Override
    public void updateGrade(Long id, UpdateGradeDTO dto) {
        BaseGrade grade = baseGradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException("年级不存在");
        }

        if (StringUtils.hasText(dto.getGradeName())) {
            grade.setGradeName(dto.getGradeName());
        }
        if (dto.getSortOrder() != null) {
            grade.setSortOrder(dto.getSortOrder());
        }
        grade.setUpdateTime(new Date());
        baseGradeMapper.updateById(grade);
    }

    @Override
    public void deleteGrade(Long id) {
        BaseGrade grade = baseGradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException("年级不存在");
        }

        // 检查是否有班级引用此年级
        LambdaQueryWrapper<BaseClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseClass::getGradeId, id);
        Long count = baseClassMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该年级下存在班级，无法删除");
        }

        baseGradeMapper.deleteById(id);
    }
}

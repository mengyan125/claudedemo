package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.BaseClassMapper;
import com.feedback.mapper.BaseGradeMapper;
import com.feedback.mapper.BaseStudentClassMapper;
import com.feedback.mapper.BaseTeacherClassMapper;
import com.feedback.model.dto.CreateClassDTO;
import com.feedback.model.dto.UpdateClassDTO;
import com.feedback.model.entity.BaseClass;
import com.feedback.model.entity.BaseGrade;
import com.feedback.model.entity.BaseStudentClass;
import com.feedback.model.entity.BaseTeacherClass;
import com.feedback.model.vo.ClassItemVO;
import com.feedback.service.BaseClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 班级服务实现类
 * 处理班级的增删改查业务逻辑
 */
@Service
public class BaseClassServiceImpl implements BaseClassService {

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Autowired
    private BaseGradeMapper baseGradeMapper;

    @Autowired
    private BaseStudentClassMapper baseStudentClassMapper;

    @Autowired
    private BaseTeacherClassMapper baseTeacherClassMapper;

    @Override
    public List<ClassItemVO> getClassList(Long gradeId) {
        // 1. 查询班级列表
        LambdaQueryWrapper<BaseClass> wrapper = new LambdaQueryWrapper<>();
        if (gradeId != null) {
            wrapper.eq(BaseClass::getGradeId, gradeId);
        }
        wrapper.orderByAsc(BaseClass::getSortOrder);
        List<BaseClass> classList = baseClassMapper.selectList(wrapper);

        // 2. 查询所有年级，构建ID->名称映射
        List<BaseGrade> gradeList = baseGradeMapper.selectList(null);
        Map<Long, String> gradeNameMap = gradeList.stream()
                .collect(Collectors.toMap(BaseGrade::getId, BaseGrade::getGradeName));

        // 3. 转换为VO列表
        return classList.stream()
                .map(cls -> ClassItemVO.builder()
                        .id(cls.getId())
                        .className(cls.getClassName())
                        .gradeId(cls.getGradeId())
                        .gradeName(gradeNameMap.getOrDefault(cls.getGradeId(), ""))
                        .sortOrder(cls.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void createClass(CreateClassDTO dto) {
        // 校验年级是否存在
        BaseGrade grade = baseGradeMapper.selectById(dto.getGradeId());
        if (grade == null) {
            throw new BusinessException("年级不存在");
        }

        BaseClass baseClass = new BaseClass();
        baseClass.setClassName(dto.getClassName());
        baseClass.setGradeId(dto.getGradeId());
        baseClass.setSortOrder(dto.getSortOrder());
        baseClass.setCreateTime(new Date());
        baseClass.setUpdateTime(new Date());
        baseClassMapper.insert(baseClass);
    }

    @Override
    public void updateClass(Long id, UpdateClassDTO dto) {
        BaseClass baseClass = baseClassMapper.selectById(id);
        if (baseClass == null) {
            throw new BusinessException("班级不存在");
        }

        if (StringUtils.hasText(dto.getClassName())) {
            baseClass.setClassName(dto.getClassName());
        }
        if (dto.getGradeId() != null) {
            // 校验年级是否存在
            BaseGrade grade = baseGradeMapper.selectById(dto.getGradeId());
            if (grade == null) {
                throw new BusinessException("年级不存在");
            }
            baseClass.setGradeId(dto.getGradeId());
        }
        if (dto.getSortOrder() != null) {
            baseClass.setSortOrder(dto.getSortOrder());
        }
        baseClass.setUpdateTime(new Date());
        baseClassMapper.updateById(baseClass);
    }

    @Override
    public void deleteClass(Long id) {
        BaseClass baseClass = baseClassMapper.selectById(id);
        if (baseClass == null) {
            throw new BusinessException("班级不存在");
        }

        // 检查是否有学生分配
        LambdaQueryWrapper<BaseStudentClass> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BaseStudentClass::getClassId, id);
        Long studentCount = baseStudentClassMapper.selectCount(studentWrapper);
        if (studentCount > 0) {
            throw new BusinessException("该班级下存在学生分配记录，无法删除");
        }

        // 检查是否有教师分配
        LambdaQueryWrapper<BaseTeacherClass> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BaseTeacherClass::getClassId, id);
        Long teacherCount = baseTeacherClassMapper.selectCount(teacherWrapper);
        if (teacherCount > 0) {
            throw new BusinessException("该班级下存在教师分配记录，无法删除");
        }

        baseClassMapper.deleteById(id);
    }
}

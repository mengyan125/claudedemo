package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.BaseSemesterMapper;
import com.feedback.model.dto.CreateSemesterDTO;
import com.feedback.model.dto.UpdateSemesterDTO;
import com.feedback.model.entity.BaseSemester;
import com.feedback.model.vo.SemesterItemVO;
import com.feedback.service.BaseSemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学期服务实现类
 */
@Service
public class BaseSemesterServiceImpl implements BaseSemesterService {

    @Autowired
    private BaseSemesterMapper baseSemesterMapper;

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Override
    public List<SemesterItemVO> getSemesterList() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BaseSemester::getId);
        List<BaseSemester> list = baseSemesterMapper.selectList(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public SemesterItemVO getCurrentSemester() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSemester::getIsCurrent, 1);
        BaseSemester semester = baseSemesterMapper.selectOne(wrapper);
        return semester != null ? toVO(semester) : null;
    }

    @Override
    public void createSemester(CreateSemesterDTO dto) {
        BaseSemester semester = new BaseSemester();
        semester.setSemesterName(dto.getSemesterName());
        semester.setStartDate(parseDate(dto.getStartDate()));
        semester.setEndDate(parseDate(dto.getEndDate()));
        semester.setIsCurrent(0);
        baseSemesterMapper.insert(semester);
    }

    @Override
    public void updateSemester(Long id, UpdateSemesterDTO dto) {
        BaseSemester semester = baseSemesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }
        semester.setSemesterName(dto.getSemesterName());
        semester.setStartDate(parseDate(dto.getStartDate()));
        semester.setEndDate(parseDate(dto.getEndDate()));
        baseSemesterMapper.updateById(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentSemester(Long id) {
        BaseSemester semester = baseSemesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }
        LambdaUpdateWrapper<BaseSemester> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BaseSemester::getIsCurrent, 0);
        baseSemesterMapper.update(null, updateWrapper);

        semester.setIsCurrent(1);
        baseSemesterMapper.updateById(semester);
    }

    @Override
    public void deleteSemester(Long id) {
        BaseSemester semester = baseSemesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }
        baseSemesterMapper.deleteById(id);
    }

    private SemesterItemVO toVO(BaseSemester s) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return SemesterItemVO.builder()
                .id(s.getId())
                .semesterName(s.getSemesterName())
                .startDate(s.getStartDate() != null ? sdf.format(s.getStartDate()) : "")
                .endDate(s.getEndDate() != null ? sdf.format(s.getEndDate()) : "")
                .isCurrent(s.getIsCurrent() != null && s.getIsCurrent() == 1)
                .build();
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new BusinessException("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }
}

package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.BaseClassMapper;
import com.feedback.mapper.BaseSemesterMapper;
import com.feedback.mapper.BaseStudentClassMapper;
import com.feedback.mapper.BaseTeacherClassMapper;
import com.feedback.mapper.SysUserMapper;
import com.feedback.model.dto.AssignStudentsDTO;
import com.feedback.model.dto.AssignTeacherDTO;
import com.feedback.model.entity.BaseClass;
import com.feedback.model.entity.BaseSemester;
import com.feedback.model.entity.BaseStudentClass;
import com.feedback.model.entity.BaseTeacherClass;
import com.feedback.model.entity.SysUser;
import com.feedback.model.vo.StudentAssignmentVO;
import com.feedback.model.vo.TeacherAssignmentVO;
import com.feedback.service.BaseRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 班级关系服务实现类
 * 处理学生/教师与班级分配的业务逻辑
 */
@Service
public class BaseRelationServiceImpl implements BaseRelationService {

    @Autowired
    private BaseStudentClassMapper baseStudentClassMapper;

    @Autowired
    private BaseTeacherClassMapper baseTeacherClassMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Autowired
    private BaseSemesterMapper baseSemesterMapper;

    /** 日期时间格式 */
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<StudentAssignmentVO> getClassStudents(Long classId) {
        Long semesterId = getCurrentSemesterId();

        // 查询当前学期该班级的学生分配记录
        LambdaQueryWrapper<BaseStudentClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseStudentClass::getClassId, classId)
                .eq(BaseStudentClass::getSemesterId, semesterId);
        List<BaseStudentClass> records = baseStudentClassMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询学生信息
        List<Long> studentIds = records.stream()
                .map(BaseStudentClass::getStudentId)
                .collect(Collectors.toList());
        Map<Long, SysUser> userMap = getUserMap(studentIds);

        // 查询班级信息
        BaseClass baseClass = baseClassMapper.selectById(classId);
        String className = baseClass != null ? baseClass.getClassName() : "";

        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
        return records.stream()
                .map(record -> {
                    SysUser user = userMap.get(record.getStudentId());
                    return StudentAssignmentVO.builder()
                            .id(record.getId())
                            .studentId(record.getStudentId())
                            .studentName(user != null ? user.getRealName() : "")
                            .username(user != null ? user.getUsername() : "")
                            .classId(classId)
                            .className(className)
                            .assignTime(record.getCreateTime() != null
                                    ? sdf.format(record.getCreateTime()) : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentAssignmentVO> getUnassignedStudents() {
        Long semesterId = getCurrentSemesterId();

        // 查询当前学期已分配的学生ID
        LambdaQueryWrapper<BaseStudentClass> scWrapper = new LambdaQueryWrapper<>();
        scWrapper.eq(BaseStudentClass::getSemesterId, semesterId);
        List<BaseStudentClass> assigned = baseStudentClassMapper.selectList(scWrapper);
        Set<Long> assignedIds = assigned.stream()
                .map(BaseStudentClass::getStudentId)
                .collect(Collectors.toSet());

        // 查询所有学生类型的用户
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUserType, "student");
        List<SysUser> students = sysUserMapper.selectList(userWrapper);

        // 过滤未分配的学生
        return students.stream()
                .filter(s -> !assignedIds.contains(s.getId()))
                .map(s -> StudentAssignmentVO.builder()
                        .id(0L)
                        .studentId(s.getId())
                        .studentName(s.getRealName())
                        .username(s.getUsername())
                        .classId(0L)
                        .className("")
                        .assignTime("")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignStudentsToClass(Long classId, AssignStudentsDTO dto) {
        Long semesterId = getCurrentSemesterId();
        Date now = new Date();

        for (Long studentId : dto.getStudentIds()) {
            BaseStudentClass record = new BaseStudentClass();
            record.setStudentId(studentId);
            record.setClassId(classId);
            record.setSemesterId(semesterId);
            record.setCreateTime(now);
            baseStudentClassMapper.insert(record);
        }
    }

    @Override
    public void removeStudentAssignment(Long id) {
        BaseStudentClass record = baseStudentClassMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("分配记录不存在");
        }
        // 物理删除
        baseStudentClassMapper.deleteById(id);
    }

    @Override
    public List<TeacherAssignmentVO> getClassTeachers(Long classId) {
        Long semesterId = getCurrentSemesterId();

        // 查询当前学期该班级的教师分配记录
        LambdaQueryWrapper<BaseTeacherClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseTeacherClass::getClassId, classId)
                .eq(BaseTeacherClass::getSemesterId, semesterId);
        List<BaseTeacherClass> records = baseTeacherClassMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询教师信息
        List<Long> teacherIds = records.stream()
                .map(BaseTeacherClass::getTeacherId)
                .collect(Collectors.toList());
        Map<Long, SysUser> userMap = getUserMap(teacherIds);

        // 查询班级信息
        BaseClass baseClass = baseClassMapper.selectById(classId);
        String className = baseClass != null ? baseClass.getClassName() : "";

        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
        return records.stream()
                .map(record -> {
                    SysUser user = userMap.get(record.getTeacherId());
                    return TeacherAssignmentVO.builder()
                            .id(record.getId())
                            .teacherId(record.getTeacherId())
                            .teacherName(user != null ? user.getRealName() : "")
                            .username(user != null ? user.getUsername() : "")
                            .classId(classId)
                            .className(className)
                            .subject(record.getSubject())
                            .assignTime(record.getCreateTime() != null
                                    ? sdf.format(record.getCreateTime()) : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTeacherToClass(Long classId, AssignTeacherDTO dto) {
        Long semesterId = getCurrentSemesterId();

        BaseTeacherClass record = new BaseTeacherClass();
        record.setTeacherId(dto.getTeacherId());
        record.setClassId(classId);
        record.setSemesterId(semesterId);
        record.setSubject(dto.getSubject());
        record.setCreateTime(new Date());
        baseTeacherClassMapper.insert(record);
    }

    @Override
    public void removeTeacherAssignment(Long id) {
        BaseTeacherClass record = baseTeacherClassMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("分配记录不存在");
        }
        // 物理删除
        baseTeacherClassMapper.deleteById(id);
    }

    /**
     * 获取当前学期ID
     *
     * @return 当前学期ID
     * @throws BusinessException 未设置当前学期时抛出
     */
    private Long getCurrentSemesterId() {
        LambdaQueryWrapper<BaseSemester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseSemester::getIsCurrent, 1);
        BaseSemester semester = baseSemesterMapper.selectOne(wrapper);
        if (semester == null) {
            throw new BusinessException("未设置当前学期");
        }
        return semester.getId();
    }

    /**
     * 批量查询用户信息并构建ID->用户映射
     *
     * @param userIds 用户ID列表
     * @return 用户ID到用户实体的映射
     */
    private Map<Long, SysUser> getUserMap(List<Long> userIds) {
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }
}

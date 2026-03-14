package com.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feedback.common.exception.BusinessException;
import com.feedback.mapper.FbQuickReplyMapper;
import com.feedback.mapper.SysUserMapper;
import com.feedback.model.dto.CreateQuickReplyDTO;
import com.feedback.model.dto.UpdateQuickReplyDTO;
import com.feedback.model.entity.FbQuickReply;
import com.feedback.model.entity.SysUser;
import com.feedback.model.vo.QuickReplyItemVO;
import com.feedback.security.UserContext;
import com.feedback.service.AdminQuickReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理端-快捷回复服务实现类
 * 处理快捷回复的增删改查业务逻辑
 */
@Service
public class AdminQuickReplyServiceImpl implements AdminQuickReplyService {

    @Autowired
    private FbQuickReplyMapper fbQuickReplyMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<QuickReplyItemVO> getQuickReplyList() {
        LambdaQueryWrapper<FbQuickReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FbQuickReply::getSortOrder);
        List<FbQuickReply> quickReplies = fbQuickReplyMapper.selectList(wrapper);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<QuickReplyItemVO> result = new ArrayList<>();
        for (FbQuickReply quickReply : quickReplies) {
            // 联查sys_user表获取创建人姓名
            String createUserName = "";
            if (quickReply.getCreateUserId() != null) {
                SysUser user = sysUserMapper.selectById(quickReply.getCreateUserId());
                if (user != null) {
                    createUserName = user.getRealName();
                }
            }

            QuickReplyItemVO vo = QuickReplyItemVO.builder()
                    .id(quickReply.getId())
                    .content(quickReply.getContent())
                    .sortOrder(quickReply.getSortOrder())
                    .createUserName(createUserName)
                    .createTime(quickReply.getCreateTime() != null ? sdf.format(quickReply.getCreateTime()) : "")
                    .build();
            result.add(vo);
        }
        return result;
    }

    @Override
    public void createQuickReply(CreateQuickReplyDTO dto) {
        FbQuickReply quickReply = new FbQuickReply();
        quickReply.setContent(dto.getContent());
        quickReply.setSortOrder(dto.getSortOrder());
        quickReply.setCreateUserId(UserContext.getCurrentUserId());
        quickReply.setCreateTime(new Date());
        quickReply.setUpdateTime(new Date());
        fbQuickReplyMapper.insert(quickReply);
    }

    @Override
    public void updateQuickReply(Long id, UpdateQuickReplyDTO dto) {
        FbQuickReply quickReply = fbQuickReplyMapper.selectById(id);
        if (quickReply == null) {
            throw new BusinessException("快捷回复不存在");
        }

        if (StringUtils.hasText(dto.getContent())) {
            quickReply.setContent(dto.getContent());
        }
        if (dto.getSortOrder() != null) {
            quickReply.setSortOrder(dto.getSortOrder());
        }
        quickReply.setUpdateTime(new Date());
        fbQuickReplyMapper.updateById(quickReply);
    }

    @Override
    public void deleteQuickReply(Long id) {
        FbQuickReply quickReply = fbQuickReplyMapper.selectById(id);
        if (quickReply == null) {
            throw new BusinessException("快捷回复不存在");
        }

        fbQuickReplyMapper.deleteById(id);
    }
}

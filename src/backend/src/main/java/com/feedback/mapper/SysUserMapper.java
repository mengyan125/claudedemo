package com.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feedback.model.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户Mapper接口
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户名数量（包含逻辑删除的记录），用于唯一性校验
     * 绕过 @TableLogic 的自动 deleted=0 过滤
     */
    @Select({"<script>",
            "SELECT COUNT(*) FROM sys_user WHERE username = #{username}",
            "<if test='excludeId != null'> AND id != #{excludeId}</if>",
            "</script>"})
    Long countByUsernameIncludeDeleted(@Param("username") String username,
                                       @Param("excludeId") Long excludeId);
}

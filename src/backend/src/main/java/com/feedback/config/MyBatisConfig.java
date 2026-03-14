package com.feedback.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件等 MyBatis-Plus 扩展功能
 */
@Configuration
public class MyBatisConfig {

    /**
     * 配置 MyBatis-Plus 分页拦截器
     * 使用 MySQL 数据库类型（兼容 TiDB）
     *
     * @return MybatisPlusInterceptor 分页拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页内置拦截器，数据库类型设置为 MySQL（TiDB 兼容 MySQL 协议）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

package com.feedback.config;

import com.feedback.security.JwtInterceptor;
import com.feedback.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类
 * 配置拦截器和静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private RoleInterceptor roleInterceptor;

    /**
     * 文件上传目录，默认为当前目录下的 uploads 文件夹
     */
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 添加拦截器配置
     * JWT 拦截器拦截所有 /api/** 路径，排除登录接口和文件访问接口
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/files/**")
                .order(1);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/files/**")
                .order(2);
    }

    /**
     * 添加静态资源映射
     * 将文件上传目录映射到 /api/files/** 路径
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 文件上传静态资源映射（使用绝对路径）
        String absoluteUploadDir = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + absoluteUploadDir + "/");
    }
}

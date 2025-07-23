package com.teamProject2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final LoginInterceptor loginInterceptor;
	private final DeptAuthInterceptor  deptAuthInterceptor; 

    @Autowired
    public WebConfig(LoginInterceptor loginInterceptor,
    				 DeptAuthInterceptor  deptAuthInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.deptAuthInterceptor = deptAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	
    	// 1) 로그인 체크
    	registry.addInterceptor(loginInterceptor)
            .order(1)
            .addPathPatterns("/**")                         // 모든 요청
            .excludePathPatterns(                          // 단, 이 경로들은 예외
                "/login/**",      // /login, /login/logout
                "/css/**",
                "/js/**",
                "/images/**"
            );
    	// 2) 부서코드 기반 권한 체크
    	registry.addInterceptor(deptAuthInterceptor)
                .order(2)
                .addPathPatterns(
                	"/order/**", 
                    "/confirm/**", 
                    "/stockIn/**"
                );
    }
}

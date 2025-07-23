package com.teamProject2.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// 요청 전에 세션 체크하여 로그인 여부 확인
@Component
public class LoginInterceptor implements HandlerInterceptor {

	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1) 세션이 없거나 LOGIN_ECODE 속성이 없으면
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("LOGIN_ECODE") != null);

        // 2) 로그인 페이지(/login)나 로그아웃(/login/logout), 정적 리소스는 예외
        String uri = request.getRequestURI();
        if (!loggedIn
            && !uri.startsWith("/login")
            && !uri.startsWith("/css")
            && !uri.startsWith("/js")
            && !uri.startsWith("/images")) {

            // 3) 세션 없으면 로그인 페이지로 redirect
            response.sendRedirect("/login");
            return false;
        }
        // 4) 이미 로그인했거나 예외 URL은 그냥 요청 진행
        return true;
    }
}

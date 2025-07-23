package com.teamProject2.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class DeptAuthInterceptor implements HandlerInterceptor {

	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGIN_DCODE") == null) {
        	// 로그인 정보 없으면 무조건 로그인 페이지로
            response.sendRedirect("/login");
            return false;
        }

        Integer dcode = (Integer) session.getAttribute("LOGIN_DCODE");
        String uri  = request.getRequestURI();

        // 구매팀(DCODE=10001)만 /order/**
        if (uri.startsWith("/order") && !Integer.valueOf(10001).equals(dcode)) {
            response.sendRedirect("/login");
            return false;
        }
        // 관리팀(DCODE=10002)만 /confirm/**
        if (uri.startsWith("/confirm") && !Integer.valueOf(10002).equals(dcode)) {
            response.sendRedirect("/login");
            return false;
        }
        // 자재팀(DCODE=10003)만 /stockIn/**
        if (uri.startsWith("/stockIn") && !Integer.valueOf(10003).equals(dcode)) {
            response.sendRedirect("/login");
            return false;
        }

        // 그 외 요청은 그대로 진행
        return true;
    }
}

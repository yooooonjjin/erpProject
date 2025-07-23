package com.teamProject2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.teamProject2.service.LoginService;

@Controller
@RequestMapping("/login")
public class LoginController {

	private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    
    /**
     * 로그인 화면
     */
    @GetMapping
    public String loginPage() {
        return "login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping
    public String login(@RequestParam int ecode,
            			@RequestParam String ename,
            			Model model) {
    	
        // 서비스에 인증 요청 → 에러 코드 반환 (null 이면 성공)
        String error = loginService.login(ecode, ename);
        
        if (error != null) {
            // 에러가 있으면 뷰에 error 속성으로 전달
            model.addAttribute("error", error);
            return "login";
        }
        
        // 로그인 성공: 서비스가 계산한 URL로 리다이렉트
        return "redirect:" + loginService.getRedirectUrl();
    }
    
    /**
     * 로그아웃
     * 세션 무효화 후 로그인 화면으로 이동
     */
    @GetMapping("/logout")
    public String logout() {
        loginService.logout();
        return "redirect:/login";
    }
    
    
    
}

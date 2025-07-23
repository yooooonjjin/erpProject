package com.teamProject2.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.teamProject2.entity.DeptDto;
import com.teamProject2.entity.EmpDto;
import com.teamProject2.repository.DeptRepository;
import com.teamProject2.repository.EmpRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class LoginService {

	// 사원 조회용
	private final EmpRepository empRepository;
	// 부서 조회용
	private final DeptRepository deptRepository;
	// 세션 저장용
	private final HttpSession session;
	
    public LoginService(EmpRepository empRepository,
    					DeptRepository deptRepository,
    					HttpSession session) {
        this.empRepository = empRepository;
        this.deptRepository = deptRepository;
        this.session = session;
    }
    
    /**
     * 로그인 처리
     */
    public String login(int ecode, String ename) {
    	
    	// 1) 입력값 검증
    	// 사원 번호·이름 모두 미입력
    	if (ecode == 0 && (ename == null || ename.isBlank())) {
            return "emptyBoth";   
        }
    	// 사원 번호 미입력
    	if (ecode == 0) {
            return "emptyCode";   
        }
    	// 사원 번호 미입력
    	if (ecode == 0) {
            return "emptyCode";   
        }
    	
    	// 2) 사원 인증 (사원번호와 이름 매칭)
        Optional<EmpDto> optEmp = empRepository.findByEcodeAndEname(ecode, ename);
        if (optEmp.isEmpty()) {
            return "mismatch";    // 인증 실패
        }
        EmpDto emp = optEmp.get();
    	
        // 3) 부서 조회 (DGUBUN='DPT'인 경우)
        DeptDto dept = deptRepository.findByDgubunAndDcode("dpt", emp.getDptcd())
                					 .orElseThrow(() -> new IllegalStateException("부서 정보가 없습니다."));

        // 4) 세션에 로그인 정보 저장
        session.setAttribute("LOGIN_ECODE", emp.getEcode());
        session.setAttribute("LOGIN_ENAME", emp.getEname());
        session.setAttribute("LOGIN_EPHONE", emp.getEphone());
        session.setAttribute("LOGIN_DEPT", dept);
        session.setAttribute("LOGIN_DCODE", dept.getDcode());
        session.setAttribute("LOGIN_DNAME", dept.getDname());

        return null;  // 로그인 성공
        
    }
    
    /**
     * 세션에 저장된 부서명에 따라 리다이렉트할 URL 반환
     */
    public String getRedirectUrl() {
        DeptDto dept = (DeptDto) session.getAttribute("LOGIN_DEPT");
        String dname = dept.getDname();
        return switch (dname) {
            case "구매팀"  -> "/order";
            case "관리팀"  -> "/confirm";
            case "자재팀"  -> "/stockIn";
            default        -> "/";
        };
    }
    
    
    /**
     * 로그아웃 처리 (세션 무효화)
     */
    public void logout() {
        session.invalidate();
    }

    
}

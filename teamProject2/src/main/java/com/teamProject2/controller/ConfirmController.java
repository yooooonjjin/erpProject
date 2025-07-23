package com.teamProject2.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.service.ConfirmService;
import com.teamProject2.service.OrdersService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("confirm")
public class ConfirmController {

	public final OrdersService ordersService;
	public final ConfirmService confirmService;
	
	public ConfirmController(OrdersService ordersService, ConfirmService confirmService) {
		this.ordersService = ordersService;
		this.confirmService = confirmService;
	}
	
	/**
	 * 모든 결재건에 대한 리스트 (결재 대기, 승인, 반려)
	 */
	@GetMapping
	public ModelAndView allList( @RequestParam(defaultValue = "1") int indexpage,
	        					 @RequestParam(defaultValue = "ORD") String ogubun,
	        					 @RequestParam(name="ordate", required=false)
	        					 @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate ordate,
	        					 @RequestParam(name="ostate", required=false) String ostate,     // 상태 필터
	        				     @RequestParam(name="supnm",  required=false) String supnm,      // 공급처명 키워드 필터
	        				     @RequestParam(name="mgrnm",  required=false) String mgrnm,      // 담당자명 키워드 필터
	        				     @RequestParam(name="word",   required=false) String word,
	        				     HttpSession session) {
		
		ModelAndView model = new ModelAndView();
	    
		// 1) ogubun 기본값 보정
	    if (ogubun == null || ogubun.isBlank()) {
	        ogubun = "ORD";
	    }
		
		// 2) 전체 주문 건수
		Long total = ordersService.count(ogubun);
		
		// 3) 실제 페이징 된 목록
		int pageSize = 10;	// 한 페이지에 10개씩
		Page<OrdersDto> page = ordersService.allList(
				indexpage - 1,
				pageSize,
				ogubun,
	            ordate,          // 신청일자
	            ostate,          // 상태
	            supnm,           // 공급처명 키워드
	            mgrnm,           // 담당자명 키워드
	            word             // 특이사항 키워드
		);
		
		// 4) 화면에 보여줄 리스트와 페이징 정보
		List<OrdersDto> list = page.getContent();
		int startPageRownum = (int)(total - (indexpage - 1) * pageSize);
		
		// 5) 공급처명 맵으로 받아오기
		Map<Integer, String> supMap  = ordersService.getSupplierMap();

		// 6) 총 발주 건수 불러오기
		Map<Integer, Long> itemCountMap = ordersService.getItemCountMap(ogubun);
		 
		// 7) 페이징 검색 결과 없을 경우
		int totalPages = page.getTotalPages();
		if (totalPages == 0) {
		    totalPages = 1;
		}
		
		// 8) 모델에 필요한 값들 등록
		model.addObject("list", list);							// 화면에 보여줄 리스트
		model.addObject("startPageRownum", startPageRownum);    // 번호 컬럼 시작값
        model.addObject("ptotal", total);              			// 전체 주문 건수
        model.addObject("ptotalPage", page.getTotalPages()); 	// 전체 페이지 수
        model.addObject("currentPage", indexpage);
        
        model.addObject("ordate", ordate);       	// 신청일자
        model.addObject("ostate", ostate);       	// 상태
        model.addObject("supnm", supnm);        	// 공급처명 키워드
        model.addObject("mgrnm", mgrnm);        	// 담당자명 키워드
        model.addObject("word", word);         		// 특이사항 키워드
        
        model.addObject("supMap", supMap);						// 공급처명
        model.addObject("itemCountMap", itemCountMap);			// 총 발주건수
        model.addObject("statusList", List.of("결재 대기","결재 승인","결재 반려"));				// 상태 드롭다운 데이터
        
        // 로그인 한 사원명
     	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));
     	
        
        model.setViewName("confirm/confirmList");
		
		return model;
	}
	
	/**
	 * 결재대기함
	 */
	@GetMapping("/pending")
	public ModelAndView confirmPendingList(@RequestParam(defaultValue = "1") int indexpage,
	                                       @RequestParam(defaultValue = "ORD") String ogubun,
	                                       @RequestParam(name = "ordate", required = false)
	                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate ordate,
	                                       @RequestParam(name = "supnm", required = false) String supnm,
	                                       @RequestParam(name = "mgrnm", required = false) String mgrnm,
	                                       @RequestParam(name = "word", required = false) String word,
	                                       HttpSession session) {

	    ModelAndView model = new ModelAndView();

	    // 0) ogubun 기본값 보정
	    if (ogubun == null || ogubun.isBlank()) {
	        ogubun = "ORD";
	    }
	    
	    // 1) 상태값 고정: 결재 대기
	    String ostate = "결재 대기";

	    // 2) 전체 데이터 수 조회 (페이징 계산용)
	    Long total = ordersService.countPending(ogubun, ostate);

	    // 3) 현재 페이지 데이터 목록 조회
	    int pageSize = 10;
	    Page<OrdersDto> page = ordersService.getPendingList(
	            indexpage - 1,
	            pageSize,
	            ogubun,
	            ordate,
	            ostate,
	            supnm,
	            mgrnm,
	            word
	    );

	    // 4) 실제 보여줄 데이터 리스트
	    List<OrdersDto> list = page.getContent();
	    int startPageRownum = (int)(total - (indexpage - 1) * pageSize);
	    
	    // 5) 기타 부가 정보들 조회
	    Map<Integer, String> supMap = ordersService.getSupplierMap();
	    Map<Integer, Long> itemCountMap = ordersService.getItemCountMap(ogubun);

	    // 6) 모델에 전달할 값들 설정
	    model.addObject("list", list);							// 화면에 보여줄 리스트
		model.addObject("startPageRownum", startPageRownum);    // 번호 컬럼 시작값
        model.addObject("ptotal", total);              			// 전체 주문 건수
        model.addObject("ptotalPage", page.getTotalPages()); 	// 전체 페이지 수
        model.addObject("currentPage", indexpage);
       
        // 7) 검색 조건 유지용
        model.addObject("ordate", ordate);       // 신청일자
        model.addObject("ostate", ostate);       // 상태
        model.addObject("supnm", supnm);        // 공급처명 키워드
        model.addObject("mgrnm", mgrnm);        // 담당자명 키워드
        model.addObject("word", word);         // 특이사항 키워드
        
        // 8) 추가 데이터 전달
        model.addObject("supMap", supMap);						// 공급처명
        model.addObject("itemCountMap", itemCountMap);			// 총 발주건수
        model.addObject("statusList", List.of("결재 대기"));

        // 9) 로그인정보
	    model.addObject("loginEname", session.getAttribute("LOGIN_ENAME"));
	    
	    // 10) 뷰 설정
	    model.setViewName("confirm/confirmPendingList");

	    return model;
	}
	
	/**
	 * 결재 이력 (승인 + 반려)
	 */
	@GetMapping("/history")
	public ModelAndView confirmHistoryList(@RequestParam(defaultValue = "1") int indexpage,
	        							   @RequestParam(defaultValue = "ORD") String ogubun,
	        							   @RequestParam(name="ordate", required=false)
	        							   @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate ordate,
	        							   @RequestParam(name="ostate", required=false) String ostate,
	        							   @RequestParam(name="supnm", required=false) String supnm,
	        							   @RequestParam(name="mgrnm", required=false) String mgrnm,
	        							   @RequestParam(name="word", required=false) String word,
	        							   HttpSession session) {

	    ModelAndView model = new ModelAndView();

	    // 0) ogubun 기본값 보정
	    if (ogubun == null || ogubun.isBlank()) {
	        ogubun = "ORD";
	    }

	    // 1) 상태값 고정: 승인 또는 반려
	    List<String> historyStatusList = List.of("결재 승인", "결재 반려");
	    List<String> filterList = (ostate == null || ostate.isBlank())
	    	    	? historyStatusList : List.of(ostate);

	    // 2) 전체 데이터 수 조회
	    Long total = ordersService.countHistory(ogubun);

	    // 3) 페이징된 데이터 조회
	    int pageSize = 10;
	    Page<OrdersDto> page = ordersService.getHistoryList(
	            indexpage - 1,
	            pageSize,
	            ogubun,
	            ordate,
	            filterList,
	            supnm,
	            mgrnm,
	            word
	    );

	    // 4) 모델 세팅 (pending과 동일)
	    List<OrdersDto> list = page.getContent();
	    int startPageRownum = (int)(total - (indexpage - 1) * pageSize);

	    model.addObject("list", list);
	    model.addObject("startPageRownum", startPageRownum);
	    model.addObject("ptotal", total);
	    model.addObject("ptotalPage", page.getTotalPages());
	    model.addObject("currentPage", indexpage);

	    model.addObject("ordate", ordate);
	    model.addObject("supnm", supnm);
	    model.addObject("mgrnm", mgrnm);
	    model.addObject("word", word);

	    model.addObject("supMap", ordersService.getSupplierMap());
	    model.addObject("itemCountMap", ordersService.getItemCountMap(ogubun));
	    model.addObject("statusList", historyStatusList);
	    model.addObject("ostate", ostate);

	    model.addObject("loginEname", session.getAttribute("LOGIN_ENAME"));

	    model.setViewName("confirm/confirmHistoryList");
	    return model;
	}
	
	/**
	 * 내 결재 이력 (승인과 반려만, 본인이 결재자)
	 */
	@GetMapping("/my")
    public ModelAndView myList(@RequestParam(defaultValue = "1") int indexpage,
            				   @RequestParam(defaultValue = "ORD") String ogubun,
            				   @RequestParam(name = "ordate", required = false)
            				   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate ordate,
            				   @RequestParam(name = "ostate", required = false) String ostate,
            				   @RequestParam(name = "supnm",  required = false) String supnm,
            				   @RequestParam(name = "mgrnm",  required = false) String mgrnm,
            				   @RequestParam(name = "word",   required = false) String word,
            				   HttpSession session) {

        Integer loginEmpCd = (Integer) session.getAttribute("LOGIN_ECODE");
        String loginEname  = (String) session.getAttribute("LOGIN_ENAME");

        // (1) 전체 본인 이력(승인+반려) 조회
        List<String> historyStatuses = List.of("결재 승인", "결재 반려");
        Page<OrdersDto> histPage = ordersService.getHistoryList(
                indexpage - 1,
                10,
                ogubun,
                ordate,
                historyStatuses,
                supnm,
                mgrnm,
                word
        );

        // (2) “내가 결재한” 필터
        List<OrdersDto> myContent = histPage.getContent().stream()
            .filter(o -> loginEmpCd.equals(o.getCfmemp()))
            .toList();

        Page<OrdersDto> page = new PageImpl<>(
            myContent,
            histPage.getPageable(),
            myContent.size()
        );

        // (3) 모델 세팅
        ModelAndView mv = new ModelAndView("confirm/confirmMyList");
        mv.addObject("list",           page.getContent());
        mv.addObject("currentPage",    indexpage);
        mv.addObject("ptotalPage",     page.getTotalPages());
        mv.addObject("startPageRownum",
                     (int)(page.getTotalElements() - (indexpage - 1) * 10));
        mv.addObject("ordate",         ordate);
        mv.addObject("ostate",         ostate);
        mv.addObject("supnm",          supnm);
        mv.addObject("mgrnm",          mgrnm);
        mv.addObject("word",           word);
        mv.addObject("supMap",         ordersService.getSupplierMap());
        mv.addObject("itemCountMap",   ordersService.getItemCountMap(ogubun));
        mv.addObject("statusList",     historyStatuses);  // 승인/반려만
        mv.addObject("loginEname",     loginEname);
        mv.addObject("view",           "my");            // 상세→목록용 플래그

        return mv;
    }
	
	/**
	 * 발주서 상세보기
	 */
	@GetMapping({"/{ono}", "/pending/{ono}", "/history/{ono}", "/my/{ono}"})
	public ModelAndView detail( @PathVariable int ono,
								@RequestParam(defaultValue="all") String view,
								@RequestParam(name="ogubun", defaultValue="ORD") String ogubun,
								@RequestParam(defaultValue = "1") int indexpage,
								@RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate ordate,
								@RequestParam(required = false) String ostate,
								@RequestParam(required = false) String supnm,
								@RequestParam(required = false) String mgrnm,
								@RequestParam(required = false) String word,
								HttpSession session) {
	
		ModelAndView model = new ModelAndView();	
		
		List<Map<String, Object>> ordDetail = ordersService.detail(ono);
		Map<String, Object> receiver = ordersService.getReceiverInfo(ono);
		if (receiver == null) {
		    receiver = new HashMap<>();
		    receiver.put("ENAME", "-");  // 기본값 설정
		}
		Map<String, Object> supplier = ordersService.getSupplierInfo(ono);
		Map<String, Object> first = ordDetail.get(0);

		OrdersDto summary = ordersService.getOrderSummary(ono);
		model.addObject("summary", summary);
		
		// 상세 페이지 데이터
		model.addObject("ordDetail", ordDetail);	// 자재 라인 리스트
		model.addObject("receiver", receiver);		// 수신처
		model.addObject("supplier", supplier);		// 공급처
		model.addObject("ono", ono); 
        model.addObject("ogubun", ogubun);
        model.addObject("onote", first.get("ONOTE"));
        model.addObject("ordate_detail", first.get("ORDATE"));
        model.addObject("ostate_detail", first.get("OSTATE"));
        
        
        // 검색 조건 유지 데이터
        model.addObject("ogubun", ogubun);
        model.addObject("indexpage", indexpage);
        model.addObject("ordate", ordate);        
        model.addObject("ostate", ostate);        
        model.addObject("supnm", supnm);
        model.addObject("mgrnm", mgrnm);
        model.addObject("word", word);

        model.addObject("view", view);
        
        // 로그인 한 사원명 + 사원번호
        model.addObject("loginEcode",  session.getAttribute("LOGIN_ECODE"));
        model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));

        
		model.setViewName("confirm/confirmDetail");
		
		return model;
	}
	
	
	/**
	 * 결재하기 (승인 / 반려)
	 */
	@PostMapping("/confirmAction")
	public String confirmAction(@RequestParam int ono,
				            	@RequestParam String type,      // 1: 승인, 2: 반려
				            	@RequestParam String rnote,     // 선택 사유
				            	@RequestParam String rdetail,	// 상세 사유
				            	HttpSession session) {	
		
		// 세션에서 로그인 사원코드·사원이름 꺼내기
		Integer loginEmpCd = (Integer) session.getAttribute("LOGIN_ECODE");
	    String  loginEname = (String)  session.getAttribute("LOGIN_ENAME");

	    
		if ("1".equals(type)) {
	        confirmService.approve(ono, rnote, rdetail, loginEmpCd, loginEname);  // 승인 처리
	    } else if ("2".equals(type)) {
	        confirmService.reject(ono, rnote, rdetail, loginEmpCd, loginEname);   // 반려 처리
	    }
		
		// 결재 후 목록 화면으로 이동
		return "redirect:/confirm";
	}
	
	
	
	
	

}

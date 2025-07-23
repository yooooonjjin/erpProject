package com.teamProject2.controller;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.OrdersDto;
import com.teamProject2.service.OrdersService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/order")
public class OrdersController {
	
	public final OrdersService orderService;
	public OrdersController(OrdersService orderService) {
		this.orderService = orderService;
	}
	
	/**
	 * 발주목록화면
	 */
	@GetMapping
	public ModelAndView list(@RequestParam(defaultValue = "1") int indexpage,
	                         @RequestParam(required = false) String ordate,
	                         @RequestParam(required = false) String ostate,
	                         @RequestParam(required = false) String supcd,
	                         @RequestParam(required = false) String omgr,
	                         @RequestParam(required = false) String onote,
	                         @RequestParam(defaultValue = "ORD") String ogubun,
	                         HttpSession session) {

		ModelAndView model = new ModelAndView();

	    Page<OrdersDto> page;

	    if ((ordate != null && !ordate.isBlank()) ||
	        (ostate != null && !ostate.isBlank()) ||
	        (supcd != null && !supcd.isBlank()) ||
	        (omgr != null && !omgr.isBlank()) ||
	        (onote != null && !onote.isBlank())) {
	        
	        page = orderService.searchOrders(indexpage - 1, 10, ordate, ostate, supcd, omgr, onote, ogubun);
	    } else {
	        page = orderService.listByOgubun(indexpage - 1, 10, ogubun);
	    }

	    int startPageRownum = (int)(page.getTotalElements() - page.getNumber() * 10);

	    model.addObject("ordList", page.getContent());
	    model.addObject("currentPage", page.getNumber()); // 0-based
	    model.addObject("ptotal", page.getTotalElements());

	    int totalPages = page.getTotalPages();
	    if (totalPages == 0) totalPages = 1;
	    model.addObject("ptotalPage", totalPages);
	    model.addObject("startPageRownum", startPageRownum);

	    // 페이지 단위
	    int pageGroupSize = 10; // 한 번에 보여줄 페이지 수
	    int startPage = (page.getNumber() / pageGroupSize) * pageGroupSize + 1;
	    int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);
	    model.addObject("startPage", startPage);
	    model.addObject("endPage", endPage);

	    model.addObject("orderCountMap", orderService.countByOnoGroup(ogubun));

	    List<Integer> onoList = page.getContent().stream().map(OrdersDto::getOno).toList();
	    model.addObject("supplierNameMap", orderService.getSupplierNamesForOrders(onoList));

	    model.addObject("ordate", ordate);
	    model.addObject("ostate", ostate);
	    model.addObject("supcd", supcd);
	    model.addObject("omgr", omgr);
	    model.addObject("onote", onote);
	    
	    // 로그인 정보
     	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));

	    model.setViewName("order/orderList");
	    return model;
	}

	
	/**
	 * 발주신청서화면
	 */
	@GetMapping("/write")
	public ModelAndView write(HttpSession session) {
		
		ModelAndView model = new ModelAndView();
		
		// 로그인 정보
     	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));
	    model.addObject("loginEcode",  session.getAttribute("LOGIN_ECODE"));
	    model.addObject("loginDptcd",  session.getAttribute("LOGIN_DPTCD"));
	    model.addObject("loginDname",  session.getAttribute("LOGIN_DNAME"));
	    model.addObject("loginEphone",  session.getAttribute("LOGIN_EPHONE"));
		
		// 신청서 현재날짜 표시
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1;
		int d = cal.get(Calendar.DATE);
		model.addObject("ymd",y+"년 "+m+"월 "+d+"일");
		
		model.setViewName("order/orderWrite");
		
		return model;
		
	}
	
	/**
	 * 공급처관련 자동완성
	 */
	@GetMapping("/suppliers")
    public List<ClientDto> getSuppliers(@RequestParam("keyword") String keyword) {
        return orderService.findSuppliersByName(keyword);
    }

    @GetMapping("/supplier/{ccode}")
    public ClientDto getSupplierByCode(@PathVariable int ccode) {
        return orderService.findSupplierByCcode(ccode);
    }
    
    /**
     * 자재관련 자동완성
     */
    @GetMapping("/items")
    public List<Map<String, Object>> getItems(@RequestParam("keyword") String keyword) {
        return orderService.findItemsByKeyword(keyword.trim());
    }
    
    @PostMapping
    public String save(@RequestParam(required = false) Integer ono,  // 수정 시 hidden 값 넘어옴
                       @RequestParam int supcd,
                       @RequestParam String omgr,
                       @RequestParam int empcd,
                       @RequestParam String ogubun,
                       @RequestParam String ostate,
                       @RequestParam String onote,
                       @RequestParam int oaqty,
                       @RequestParam int oasuprc,
                       @RequestParam int oatax,
                       @RequestParam int oatprc,
                       @RequestParam List<String> odate,
                       @RequestParam List<Integer> oqty,
                       @RequestParam List<Integer> ouprc,
                       @RequestParam List<Integer> osuprc,
                       @RequestParam List<Integer> otax,
                       @RequestParam List<Integer> otprc,
                       @RequestParam List<String> igubun,
                       @RequestParam List<Integer> icode) {

        int result;
        if (ono == null) {
            result = orderService.saveOrderBatch(
                supcd, omgr, empcd, ogubun, ostate, onote,
                oaqty, oasuprc, oatax, oatprc,
                odate, oqty, ouprc, osuprc, otax, otprc, igubun, icode
            );
        } else {
            result = orderService.updateOrderBatch(
                ono, ogubun, supcd, omgr, empcd, ostate, onote,
                oaqty, oasuprc, oatax, oatprc,
                odate, oqty, ouprc, osuprc, otax, otprc, igubun, icode
            );
        }

        return (result == 1) ? "ok" : "fail";
    }

    
    /**
     * 발주 상세화면
     */
    @GetMapping("/detail")
    public ModelAndView detail(@RequestParam int ono,
    						   @RequestParam String ostate,
    						   @RequestParam String ogubun,
    						   HttpSession session) {
    	
    	
    	ModelAndView model = new ModelAndView();
    	
    	// 로그인 정보
     	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));
	    model.addObject("loginEcode",  session.getAttribute("LOGIN_ECODE"));
	    

    	List<OrdersDto> list = orderService.detail(ono,ogubun);
    	model.addObject("orderList", list);
    	model.addObject("ostate", ostate);
    	model.addObject("ono", ono);
    	model.addObject("ordate", list.get(0).getOrdate());
    	model.addObject("onote", list.get(0).getOnote());
    	model.addObject("ogubun", list.get(0).getOgubun());
    	model.addObject("ocode",list.get(0).getOcode());
    	model.addObject("oaqty", list.get(0).getOaqty());
    	model.addObject("oasuprc", list.get(0).getOasuprc());
    	model.addObject("oatax", list.get(0).getOatax());
    	model.addObject("oatprc", list.get(0).getOatprc());
    	model.addObject("oudate", list.get(0).getOudate());
    	
    	// 조인용
        List<OrdersDto> orderList = orderService.getOrderDetail(ono, ogubun);
        model.addObject("orderList", orderList);

        if (!orderList.isEmpty()) {
            OrdersDto first = orderList.get(0);
            model.addObject("ogubuncode", ogubun+ ono);
            model.addObject("ordate", first.getOrdate());
            model.addObject("onote", first.getOnote());
            model.addObject("cname", first.getCname());
            model.addObject("cowner", first.getCowner());
            model.addObject("cphone", first.getCphone());
            model.addObject("ename", first.getEname());
            model.addObject("ephone", first.getEphone());
            model.addObject("dname", first.getDname());
            model.addObject("omgr", first.getOmgr());
            model.addObject("empcd", first.getEmpcd());
        }

    	model.setViewName("order/orderDetail");
    	
    	return model;
    	
    	
    }
    
    /**
     * delst n처리 삭제처리
     */
    @PostMapping("/delete")
    public String delete(OrdersDto dto) {
    	
    	String msg = "1";
    	
    	int result = orderService.updateDelst(dto.getOno(), dto.getOgubun());
    	
    	if(result<1) msg = "2";
    	
    	
    	return msg;
    }


    /**
     * 발주 수정처리 화면
     */
    @GetMapping("/modify")
    public ModelAndView modify(@RequestParam int ono,
    						   @RequestParam String ogubun,
    						   HttpSession session) {
    	
    	//날짜수정처리 dto.setOudate(new Timestamp(System.currentTimeMillis()));

    	
    	ModelAndView model = new ModelAndView();
    	
    	// 로그인 정보
     	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));
    	
    	List<OrdersDto> list = orderService.detail(ono,ogubun);
    	model.addObject("orderList", list);
    	model.addObject("ono", ono);
    	model.addObject("ordate", list.get(0).getOrdate());
    	model.addObject("onote", list.get(0).getOnote());
    	model.addObject("ogubun", list.get(0).getOgubun());
    	model.addObject("ocode",list.get(0).getOcode());
    	model.addObject("oaqty", list.get(0).getOaqty());
    	model.addObject("oasuprc", list.get(0).getOasuprc());
    	model.addObject("oatax", list.get(0).getOatax());
    	model.addObject("oatprc", list.get(0).getOatprc());
    	model.addObject("supcd", list.get(0).getSupcd());
    	
    	// 조인용
        List<OrdersDto> orderList = orderService.getOrderDetail(ono, ogubun);
        model.addObject("orderList", orderList);

        if (!orderList.isEmpty()) {
            OrdersDto first = orderList.get(0);
            model.addObject("ogubuncode", ogubun+ ono);
            model.addObject("ordate", first.getOrdate());
            model.addObject("onote", first.getOnote());
            model.addObject("cname", first.getCname());
            model.addObject("cowner", first.getCowner());
            model.addObject("cphone", first.getCphone());
            model.addObject("ename", first.getEname());
            model.addObject("ephone", first.getEphone());
            model.addObject("dname", first.getDname());
            model.addObject("icode", first.getIcode());
            model.addObject("igubun", first.getIgubun());
        }
    	model.setViewName("order/orderModify");
    	
    	return model;
    	
    }
    
    /**
     * 로그인 한 사람의 발주목록화면
     */
     @GetMapping("/my")
     public ModelAndView myList(@RequestParam(defaultValue = "1") int indexpage,
                                @RequestParam(required = false) String ordate,
                                @RequestParam(required = false) String ostate,
                                @RequestParam(required = false) String supcd,
                                @RequestParam(required = false) String onote,
                                @RequestParam(defaultValue = "ORD") String ogubun,
                                HttpSession session) {

    	 ModelAndView model = new ModelAndView();
    	 
    	 int empcd = (int) session.getAttribute("LOGIN_ECODE");
    	 String omgr = (String) session.getAttribute("LOGIN_ENAME");
    	 String empcdStr = String.valueOf(empcd); 
    	 
    	 Page<OrdersDto> page;

    	 if ((ordate != null && !ordate.isBlank()) ||
    			 (ostate != null && !ostate.isBlank()) ||
    			 (supcd != null && !supcd.isBlank()) ||
    			 (onote != null && !onote.isBlank())) {

    		 	page = orderService.searchOrdersByLoginEmp(indexpage - 1, 10, ordate, ostate, supcd, onote, ogubun, empcdStr, omgr);

           } else {
               	page = orderService.listByOgubunAndLoginEmp(indexpage - 1, 10, ogubun, empcdStr, omgr);
           }

    	 int startPageRownum = (int)(page.getTotalElements() - page.getNumber() * 10);

    	 model.addObject("ordList", page.getContent());
    	 model.addObject("currentPage", page.getNumber()); // 0-based
    	 model.addObject("ptotal", page.getTotalElements());

    	 int totalPages = page.getTotalPages();	
    	 if (totalPages == 0) totalPages = 1;
    	 model.addObject("ptotalPage", totalPages);
    	 model.addObject("startPageRownum", startPageRownum);

    	 // 페이지 단위
    	 int pageGroupSize = 10; // 한 번에 보여줄 페이지 수
    	 int startPage = (page.getNumber() / pageGroupSize) * pageGroupSize + 1;
    	 int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);
    	 model.addObject("startPage", startPage);
    	 model.addObject("endPage", endPage);

    	 model.addObject("orderCountMap", orderService.countByOnoGroup(ogubun));

    	 List<Integer> onoList = page.getContent().stream().map(OrdersDto::getOno).toList();
    	 model.addObject("supplierNameMap", orderService.getSupplierNamesForOrders(onoList));

    	 model.addObject("ordate", ordate);
    	 model.addObject("ostate", ostate);
    	 model.addObject("supcd", supcd);
    	 model.addObject("omgr", omgr);
    	 model.addObject("onote", onote);
    	 
    	// 로그인 정보
      	model.addObject("loginEname",  session.getAttribute("LOGIN_ENAME"));

    	 model.setViewName("/order/orderMyList");
    	 return model;
	}


}

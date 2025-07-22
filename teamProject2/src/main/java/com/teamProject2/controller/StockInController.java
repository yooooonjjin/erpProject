package com.teamProject2.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.teamProject2.entity.ReasonDto;
import com.teamProject2.repository.ClientRepository;
import com.teamProject2.repository.InventoryRepository;
import com.teamProject2.repository.OrdersRepository;
import com.teamProject2.repository.ReasonRepository;
import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.InventoryDto;
import com.teamProject2.entity.OrdersDto;
import com.teamProject2.entity.OrdersId;
import com.teamProject2.service.ClientService;
import com.teamProject2.service.InventoryService;
import com.teamProject2.service.OrdersService;
import com.teamProject2.service.ReasonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/stockIn")
public class StockInController {

    private final ClientRepository clientRepository;
    private final InventoryRepository inventoryRepository;
    private final OrdersRepository ordersRepository;
    private final ReasonRepository reasonRepository;
    
	private final ClientService clientService;
    private final InventoryService inventoryService;
    private final OrdersService ordersService;
    private final ReasonService reasonService;

    public StockInController( ClientService clientService, 
						      InventoryService inventoryService,
						      OrdersService ordersService, 
						      ReasonService reasonService, 
						      ClientRepository clientRepository,
						      InventoryRepository inventoryRepository,
						      OrdersRepository ordersRepository,
						      ReasonRepository reasonRepository) {
    	this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.ordersService = ordersService;
        this.reasonService = reasonService;
        
        this.clientRepository = clientRepository;
        this.inventoryRepository = inventoryRepository;
        this.ordersRepository = ordersRepository;
        this.reasonRepository = reasonRepository;
    }
	
    @GetMapping({"", "/", "/stockInList"})
    public String showStockInList(
            @RequestParam(defaultValue = "1") Integer indexPage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) String manager,
            @RequestParam(required = false) String date,
            HttpServletRequest request,
            HttpSession session,
            Model model) {
    	
        // 기존 null/빈 문자열 처리 코드
        status = (status == null || status.isEmpty()) ? null : status;
        if (status == null) {
            status = "입고";
        }
        client = (client == null || client.isEmpty()) ? null : client;
        manager = (manager == null || manager.isEmpty()) ? null : manager;
        date = (date == null || date.isEmpty()) ? null : date;

        int pageSize = 10;
        Pageable pageable = PageRequest.of(indexPage - 1, pageSize);

        // 서비스 호출(레포지토리에서 페이징 처리된 데이터를 받아옴)
        Page<Object[]> pagedOrders = ordersService.getFilteredOrders(status, client, manager, date, pageable);

        List<Map<String, Object>> mappedList = new ArrayList<>();
        for (Object[] row : pagedOrders.getContent()) {

            Map<String, Object> map = new HashMap<>();

            // row[0]이 `BigDecimal`로 `ono` 값을 나타낸다고 가정
            if (row[0] instanceof Integer) {
                Integer ono = (Integer) row[0];  // Integer 타입으로 처리

                // ono 값의 뒤 3자리만 추출
                String formattedOno = String.format("%03d", ono % 1000);  // %1000은 뒤 3자리만 추출

                String ogubun = "STI";  // ogubun은 STI로 고정

                // 입고 코드 생성
                String reasonCode = ogubun + formattedOno;  // STI + 001
                map.put("reasonCode", reasonCode);
            } else {
                map.put("reasonCode", "Unknown ono");
            }
            
         // 자재 코드 생성 (MAT, PRD, FAC + icode 형태)
            String matCode = (String) row[3];
            map.put("matCode", matCode != null ? matCode : "Unknown matCode");

            map.put("stateText", row[1]);
            map.put("qtyWithUnit", row[2] + "개");
            map.put("matCode", row[3]);
            map.put("matName", row[4]);
            map.put("clientName", row[5]);
            map.put("manager", row[6]);
            map.put("odate", row[7]);

            mappedList.add(map);
        }

        model.addAttribute("orderList", mappedList);
        model.addAttribute("page", indexPage);
        model.addAttribute("totalPages", pagedOrders.getTotalPages());
        model.addAttribute("startPageRownum", pagedOrders.getTotalElements() - (indexPage - 1) * pageSize);
        model.addAttribute("status", status);
        model.addAttribute("client", client);
        model.addAttribute("manager", manager);
        model.addAttribute("date", date);

        // 로그인 정보
        model.addAttribute("loginEname", session.getAttribute("LOGIN_ENAME"));

        return "stockIn/stockInList";
    }
    
    @GetMapping("/stockInDetail/{ono}")
    public String showStockInDetail(@PathVariable int ono, Model model) {
        
        // 현재 날짜를 포맷하여 모델에 추가
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        model.addAttribute("todayDate", formattedDate);
        
        // 입고 코드 자동 생성
        String reasonCode = ordersService.generateNextReasonCode(ono);
        System.out.println("Generated reason code: " + reasonCode);  // 생성된 입고 코드 로그 확인
        model.addAttribute("reasonCode", reasonCode);
        
       
        System.out.println("reasonCode in model: " + model.getAttribute("reasonCode"));

        // OrdersDto를 통해 matcd 가져오기
        ono = Integer.parseInt("10" + ono); 
        
        // ogubun='STI'이고 ocode=xxx인 주문 1건을 조회
        List<OrdersDto> orders = ordersRepository.findAllByOnoAndOgubun(ono, "STI");
        if (!orders.isEmpty()) {
            OrdersDto order = orders.get(0);  // ✅ 리스트에서 하나 꺼냄
            System.out.println("=========="+order);

            int matcd = order.getMatcd();  // ✅ 리스트 전체가 아니라 하나 꺼낸 객체에서 호출
            System.out.println("=========matcd 이름: " + matcd);

            // matcd를 통해 Inventory에서 자재명(iname) 조회
            Optional<InventoryDto> inventory = inventoryRepository.findByMatcd(matcd);

            // inventory가 존재하면 자재명을 가져오고, 없으면 "Unknown"으로 설정
            String itemName = inventory.map(InventoryDto::getIname).orElse("Unknown");
            System.out.println("itemName: " + itemName);  // itemName 값 로그 출력

            // 자재명 모델에 추가
            model.addAttribute("itemName", itemName);  // 자재명 전달
        }

        // 입고 상세 항목 데이터 조회
        List<Object[]> detailList = ordersRepository.findOrderDetailsByOcode(ono);
        model.addAttribute("detailList", detailList);

        // 담당자 코드로 사원 정보 조회
        if (!detailList.isEmpty()) {
            Object[] first = detailList.get(0);
            OrdersDto order2 = (OrdersDto) first[0];
            Integer empCd = order2.getEmpcd();
            
            List<Object[]> results = ordersRepository.findEmployeeWithDept(empCd);
            if (!results.isEmpty()) {
                Object[] empInfo = results.get(0);
                String receiverName = (String) empInfo[0];
                model.addAttribute("receiverName", receiverName);
                model.addAttribute("receiverDept", empInfo[1]);
                model.addAttribute("receiverPhone", empInfo[2]);

                // 🔍 수신처 로그 출력
                System.out.println("수신처 이름: " + receiverName);
                }
            }
        
	    
        
        if (!orders.isEmpty()) {
        	
        	System.out.println("📦 detailList size: " + detailList.size());
            OrdersDto order = orders.get(0);  // ✅ 이미 꺼낸 주문 객체

            Integer supplierCode = order.getSupcd();  // 🔄 여기 수정됨

            // ✅ 공급처 정보 1개 조회
            ClientDto supplier = clientRepository.findSupplierByCode(supplierCode);

            System.out.println("공급처 코드: " + supplierCode);
            System.out.println("공급처 이름: " + (supplier != null ? supplier.getCname() : "없음"));

            model.addAttribute("supplier", supplier);
        }

	    // ✅ 입고 상세 리스트
	    List<Map<String, Object>> stockList = ordersService.getStockInData();
	    model.addAttribute("stockList", stockList);
	      
        // ✅ 정확한 1건 입고 행 조회
        OrdersDto order1 = ordersRepository.findByOcode(ono);		// ocode(입고서 행번호)에 해당하는 OrdersDto 객체 한 건을 DB에서 찾아오는 코드 		// 또는 findById()로 대체 가능
        if (order1 != null) {
            model.addAttribute("order1", order1);
            
            // ✅ 공급처 정보 가져오기
            ClientDto supplier = clientRepository.findByCgubunAndCcode("sup", order1.getSupcd());
            model.addAttribute("supplier", supplier);
            
        }
        
        
		// 불용 사유 입력 화면	-> 예시: ReasonDto를 가져옴 (DB에서 가져오거나 임의 생성)
        // ocode에 해당하는 불용 사유 조회
        Optional<ReasonDto> reasonOpt = reasonService.getReasonByOrderCode(ono);
        if (reasonOpt.isPresent()) {
            model.addAttribute("reason", reasonOpt.get());
        } else {
            model.addAttribute("reason", "불용 사유가 없습니다.");
        }
		
	    return "stockIn/stockInDetail";
	}
    
    // 불용 사유 저장 후 상태 변경
    @PostMapping("/insertReason")
    @ResponseBody
    public ResponseEntity<Void> insertReason(@RequestBody ReasonDto reasonDto) {
        Integer reasonCode = reasonDto.getSticd();  // ReasonDto에서 ocode (sticd) 값 받기
        try {
	        // 1. 불용 사유 저장 후 상태 변경
	        ordersService.insertReasonAndUpdateState(reasonDto, reasonCode);
	        
	        // 2. 주문 상태를 "입고 완료"로 업데이트
	        ordersService.updateOrderState(reasonCode);	// 
	        
	        // 3. 성공적인 응답
	        return ResponseEntity.ok().build();  // 성공 시 200 응답
	    } catch (Exception e) {
	        // 예외 처리: 실패한 경우 적절한 에러 응답 반환
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500 응답
	    }
    }
  }  

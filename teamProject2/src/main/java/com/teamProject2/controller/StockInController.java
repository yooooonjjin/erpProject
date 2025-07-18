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

        return "stockIn/stockInList";
    }
    
    @GetMapping("/stockInDetail/{ocode}")
    public String showStockInDetail(@PathVariable int ocode, Model model) {
        
        // 현재 날짜를 포맷하여 모델에 추가
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        model.addAttribute("todayDate", formattedDate);
        
        // 입고 코드 자동 생성
        String reasonCode = ordersService.generateNextReasonCode(ocode);
        System.out.println("Generated reason code: " + reasonCode);  // 생성된 입고 코드 로그 확인
        model.addAttribute("reasonCode", reasonCode);
        
        System.out.println("reasonCode in model: " + model.getAttribute("reasonCode"));

        // OrdersDto를 통해 matcd 가져오기
        OrdersDto order = ordersRepository.findById(new OrdersId(null, "STI", ocode)).orElse(null);

        if (order != null) {
            int matcd = order.getMatcd();  // matcd 값을 가져옴

         // matcd를 통해 Inventory에서 자재명(iname) 조회
            Optional<InventoryDto> inventory = inventoryRepository.findByMatcd(matcd);

            // inventory가 존재하면 자재명을 가져오고, 없으면 "Unknown"으로 설정
            String itemName = inventory.map(InventoryDto::getIname).orElse("Unknown");
            System.out.println("itemName: " + itemName);  // itemName 값 로그 출력
            // 자재명 모델에 추가
            model.addAttribute("itemName", itemName);  // 자재명 전달
        }

        // 입고 상세 항목 데이터 조회
        List<Object[]> detailList = ordersRepository.findOrderDetailsByOcode(ocode);
        model.addAttribute("detailList", detailList);

        // 담당자 코드로 사원 정보 조회
        if (!detailList.isEmpty()) {
            Object[] first = detailList.get(0);
            
            // 배열 안에 들어 있는 타입 확인 (디버깅 용)
            for (int i = 0; i < first.length; i++) {
                System.out.println("🔎 index " + i + " = " + first[i] + " | type = " + first[i].getClass().getName());
            }

            // 예: OrdersDto가 0번째
            OrdersDto order2 = (OrdersDto) first[0];
            Integer empCd = order2.getEmpcd();  // 담당자 코드

            if (empCd == null) {
                // null 처리
                model.addAttribute("receiverName", "미지정");
            } else {
                // null이 아닐 때만 조회 수행
                List<Object[]> results = ordersRepository.findEmployeeWithDept(empCd);

                if (!results.isEmpty()) {
                    Object[] empInfo = results.get(0);
                    model.addAttribute("receiverName", empInfo[0]);  // 사원 이름
                    model.addAttribute("receiverDept", empInfo[1]);  // 사원 부서
                    model.addAttribute("receiverPhone", empInfo[2]);  // 사원 전화번호
                }
            }
        }
	    
        
	    if (order != null) {
	    	model.addAttribute("order", order); 
	        Integer supplierCode = order.getSupcd();

	        // ✅ 공급처 정보 1개 조회
	        ClientDto supplier = clientRepository.findSupplierByCode(supplierCode);
	        model.addAttribute("supplier", supplier);
	    }

	    // ✅ 입고 상세 리스트
	    List<Map<String, Object>> stockList = ordersService.getStockInData();
	    model.addAttribute("stockList", stockList);
	      
        // ✅ 정확한 1건 입고 행 조회
        OrdersDto order1 = ordersRepository.findByOcode(ocode);		// ocode(입고서 행번호)에 해당하는 OrdersDto 객체 한 건을 DB에서 찾아오는 코드 		// 또는 findById()로 대체 가능
        if (order1 != null) {
            model.addAttribute("order1", order1);
            
            // ✅ 공급처 정보 가져오기
            ClientDto supplier = clientRepository.findByCgubunAndCcode("sup", order1.getSupcd());
            model.addAttribute("supplier", supplier);
            
        }
        
        
		
	    return "stockIn/stockInDetail";
	}
    
    

}

	/*
	 
	// 입고 저장(불용 없을 때 전체 저장)
	 @PostMapping("/insertOrders")
	    public ResponseEntity<Void> insertStockIn(@RequestBody List<OrdersDto> data) {
		 System.out.println("🔥 [컨트롤러 진입 성공] dtoList size: " + data.size());
		 for (OrdersDto dto : data) {
		        System.out.println("➡️ 입고 행: ono={}, ocode={}, oqty={}, ogubun={}, matcd={}" +
		                 dto.getOno()+ ","+dto.getOcode()+ ","+ dto.getOqty()+  ","+dto.getOgubun()+  ","+dto.getMatcd() );
		    }
	        ordersService.saveAll(data);
	        return ResponseEntity.ok().build();
	    }
	 
	 
	*/
	
	/*
	@PostMapping("/reason/insert")
	@ResponseBody
	public ResponseEntity<?> insertReason(@RequestBody ReasonDto reasonDto) {
	    reasonService.save(reasonDto); // DB 저장
	    return ResponseEntity.ok().build();
	}
	
	*/
	
	
	// ✅입고 목록에서 입고 버튼 누르면 입고 명세서 화면으로 이동 / 입고 명세서에서 입고 눌렀을때 불용이 하나라도 있으면 불용 사유 입력 창으로
	// ✅입고 수량 : input /  입고명세서 공급처 부분 : 발주테이블  데이터를 보내주면 그걸 받아서 supcd 로 불러오기
	// ✅수신처 연동
	// ✅입고코드(입고 명세서) = (입고 목록) 입고 코드
	// ✅입고 일자 = 입고 명세서 작성한 날짜 (오늘로 찍히게)
	// ✅자재 코드 : MATCD, PRDCD, FACCD 중 코드가 null 값이 아닌 코드를 자재 테이블 코드랑 똑같은 코드+숫자
	
	// 입고 버튼 : "가용"일 때 입고 목록에 추가되게 하기("입고 완료")
	// 불용 사유 테이블 : 입고 코드 & 자재명 가져오기 / 신청 누르면 입고 목록에 추가되게 하기("입고 완료")
	
	// 자재 리스트 : 발주 테이블에서 입고 코드 기준 MATCD, PRDCD, FACCD 중 코드가 null 값이 아닌 코드를 자재 테이블 코드랑 똑같은 코드의 자재명
	// 모달창 데이터 불러오기
	    
	    
	// 입고 완료 상태 : 조건
	
	// http://localhost:8022/stockIn 으로 입고 목록 뜨게
	
	


package com.teamProject2.controller;

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
    	
    	// 로그인 세션에서 꺼내기
    	Integer loginEcode = (Integer) session.getAttribute("LOGIN_ECODE");
    	String  loginEname = (String)  session.getAttribute("LOGIN_ENAME");
    	model.addAttribute("loginEcode",  loginEcode);
    	model.addAttribute("loginEname",  loginEname);

        // 페이징을 위해 현재 페이지를 설정하고, 기본값 1을 설정
        int pageSize = 10;  // 페이지 크기 설정
        Pageable pageable = PageRequest.of(indexPage - 1, pageSize);

        // 기존 null/빈 문자열 처리 코드
        status = (status == null || status.isEmpty()) ? null : status;
        if (status == null) {
            status = "입고";
        }
        client = (client == null || client.isEmpty()) ? null : client;
        manager = (manager == null || manager.isEmpty()) ? null : manager;
        date = (date == null || date.isEmpty()) ? null : date;
        
        // 서비스 호출(레포지토리에서 페이징 처리된 데이터를 받아옴)
        Page<Object[]> pagedOrders = ordersService.getFilteredOrders(status, client, manager, date, pageable);

        // 페이지 네비게이션을 위한 데이터 계산
        List<Map<String, Object>> mappedList = new ArrayList<>();
        for (Object[] row : pagedOrders.getContent()) {

            Map<String, Object> map = new HashMap<>();

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

            // 상태
            String state = (String) row[1];
            map.put("stateText", state);

            map.put("qtyWithUnit", row[2] + "개");
            map.put("matName", row[4]);
            map.put("clientName", row[5]);

            // 상태가 입고 완료면 담당자 → 최길동
            if ("입고 완료".equals(state)) {
                map.put("manager", "최길동");
            } else {
                map.put("manager", row[6]);
            }

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
    
    @GetMapping("/stockInDetail/{ono}")
    public String showStockInDetail(@PathVariable int ono,
    								HttpSession session,
    								Model model) {
        
    	// 로그인 정보 모델에 담기
    	model.addAttribute("loginEcode", session.getAttribute("LOGIN_ECODE"));
    	model.addAttribute("loginEname", session.getAttribute("LOGIN_ENAME"));
        
    	// 현재 날짜를 포맷하여 모델에 추가
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        model.addAttribute("todayDate", formattedDate);
        
        // 입고 코드 자동 생성
        String reasonCode = ordersService.generateNextReasonCode(ono);
        model.addAttribute("reasonCode", reasonCode);
        
        // OrdersDto를 통해 matcd 가져오기
        String pre = (ono < 100) ? "10" : "1";
        ono = Integer.parseInt(pre + ono); 
        
        List<OrdersDto> orders = ordersRepository.findByOnoAndOgubun(ono, "STI");

        // ogubun='STI'이고 ono=xxx인 주문 1건을 조회
        List<Map<String, Object>> matList = new ArrayList<>();

        for (OrdersDto order : orders) {
            Integer code = null;
            String prefix = "";

            if (order.getMatcd() != null) {
                code = order.getMatcd();
                prefix = "MAT";
            } else if (order.getPrdcd() != null) {
                code = order.getPrdcd();
                prefix = "PRD";
            } else if (order.getFaccd() != null) {
                code = order.getFaccd();
                prefix = "FAC";
            } else {
                throw new IllegalStateException("matcd, prdcd, faccd 모두 null입니다.");
            }

            String matCode = prefix + code;

            String itemName = inventoryService.getMaterialName(code);

            Map<String, Object> map = new HashMap<>();
            map.put("matCode", matCode);
            map.put("itemName", itemName);
            map.put("order", order);  // 필요하다면 주문 정보도 같이 담기

            matList.add(map);
        }

        model.addAttribute("matList", matList);

        // 입고 상세 항목 데이터 조회
        List<OrdersDto> orderList = ordersRepository.findByOno(ono);
        List<Map<String, Object>> detailList = new ArrayList<>();

        for (OrdersDto dto : orderList) {
            Map<String, Object> row = new HashMap<>();
            row.put("order", dto);

            // 자재명 가져오기
            if (dto.getMatcd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getMatcd(), "MAT");
                row.put("matName", inv.getIname());
            } else if (dto.getPrdcd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getPrdcd(), "PRD");
                row.put("matName", inv.getIname());
            } else if (dto.getFaccd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getFaccd(), "FAC");
                row.put("matName", inv.getIname());
            }

            detailList.add(row);
        }

        model.addAttribute("detailList", detailList);

        if (!detailList.isEmpty()) {
            Map<String, Object> firstRow = detailList.get(0);
            OrdersDto order2 = (OrdersDto) firstRow.get("order");
            Integer empCd = order2.getEmpcd();
            
            
            
            List<Object[]> results = ordersRepository.findEmployeeWithDept(empCd);
            if (!results.isEmpty()) {
                Object[] empInfo = results.get(0);
                String receiverName = (String) empInfo[0];
                model.addAttribute("receiverName", receiverName);
                model.addAttribute("receiverDept", empInfo[1]);
                model.addAttribute("receiverPhone", empInfo[2]);
                
                }
            }
        
	    
        
        if (!orders.isEmpty()) {
        	
            OrdersDto order = orders.get(0);  // ✅ 이미 꺼낸 주문 객체

            Integer supplierCode = order.getSupcd();  // 🔄 여기 수정됨

            // ✅ 공급처 정보 1개 조회
            ClientDto supplier = clientRepository.findByCgubunAndCcode("sup", supplierCode);

            model.addAttribute("supplier", supplier);
        }

     // ✅ 입고 상세 리스트
        List<Map<String, Object>> stockList = ordersService.getStockInData(ono);
        
        // stockList가 5개일 경우, 5개 항목을 채우고 나머지는 빈 객체로 추가
        while (stockList.size() < 10) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("iname", "");
            emptyItem.put("status", "");
            emptyItem.put("ownm", "");
            emptyItem.put("ouprc", 0);
            emptyItem.put("oqty", 0);
            emptyItem.put("supply", 0);
            emptyItem.put("tax", 0);
            stockList.add(emptyItem);
        }

        model.addAttribute("stockList", stockList);
        
        // ✅ 정확한 1건 입고 행 조회
        List<OrdersDto> order1 = ordersRepository.findByOno(ono);		// ocode(입고서 행번호)에 해당하는 OrdersDto 객체 한 건을 DB에서 찾아오는 코드 		// 또는 findById()로 대체 가능
        if (order1 != null) {
            model.addAttribute("order1", order1);
            
            // ✅ 공급처 정보 가져오기
         // 리스트에서 첫 번째 OrdersDto 객체 꺼내기
            OrdersDto firstOrder = order1.get(0);

            ClientDto supplier = clientRepository.findByCgubunAndCcode("sup", firstOrder.getSupcd());
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
	        ordersService.updateOrderState(reasonCode);
	        
	        // 3. 성공적인 응답
	        return ResponseEntity.ok().build();  // 성공 시 200 응답
	    } catch (Exception e) {
	        // 예외 처리: 실패한 경우 적절한 에러 응답 반환
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500 응답
	    }
    }
    
    
    @GetMapping("/list/{ono}")
    public String showDetailList(@PathVariable int ono,
    							 HttpSession session,
    							 Model model) {
    	
    	// 로그인 정보 모델에 담기
    	model.addAttribute("loginEcode", session.getAttribute("LOGIN_ECODE"));
    	model.addAttribute("loginEname", session.getAttribute("LOGIN_ENAME"));
    	
        // 현재 날짜를 포맷하여 모델에 추가
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        model.addAttribute("todayDate", formattedDate);
        
        // 입고 코드 자동 생성
        String reasonCode = ordersService.generateNextReasonCode(ono);
        model.addAttribute("reasonCode", reasonCode);

        // OrdersDto를 통해 matcd 가져오기
        String pre = (ono < 100) ? "10" : "1";
        ono = Integer.parseInt(pre + ono);
        
        List<OrdersDto> orders = ordersRepository.findByOnoAndOgubun(ono, "STI");

        // 입고 상세 항목 데이터 조회
        List<OrdersDto> orderList = ordersRepository.findByOno(ono);
        List<Map<String, Object>> detailList = new ArrayList<>();

        for (OrdersDto dto : orderList) {
            Map<String, Object> row = new HashMap<>();
            row.put("order", dto);

            // 자재명 가져오기
            if (dto.getMatcd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getMatcd(), "MAT");
                row.put("matName", inv.getIname());
            } else if (dto.getPrdcd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getPrdcd(), "PRD");
                row.put("matName", inv.getIname());
            } else if (dto.getFaccd() != null) {
                InventoryDto inv = inventoryRepository.findByIcodeAndIgubun(dto.getFaccd(), "FAC");
                row.put("matName", inv.getIname());
            }

            detailList.add(row);
        }

        model.addAttribute("detailList", detailList);

        if (!detailList.isEmpty()) {
            Map<String, Object> firstRow = detailList.get(0); // ✅ 올바른 방식
            OrdersDto order2 = (OrdersDto) firstRow.get("order"); // ✅ 여기서 캐스팅
            Integer empCd = order2.getEmpcd();
            
            List<Object[]> results = ordersRepository.findEmployeeWithDept(empCd);
            if (!results.isEmpty()) {
                Object[] empInfo = results.get(0);
                String receiverName = (String) empInfo[0];
                model.addAttribute("receiverName", receiverName);
                model.addAttribute("receiverDept", empInfo[1]);
                model.addAttribute("receiverPhone", empInfo[2]);
            }
        }
        
        if (!orders.isEmpty()) {
            OrdersDto order = orders.get(0);  // ✅ 이미 꺼낸 주문 객체

            Integer supplierCode = order.getSupcd();  // 🔄 여기 수정됨

            // ✅ 공급처 정보 1개 조회
            ClientDto supplier = clientRepository.findSupplierByCode(supplierCode);
            model.addAttribute("supplier", supplier);
        }

        // ✅ 입고 상세 리스트
        List<Map<String, Object>> stockList = ordersService.getStockInData(ono);
        
        // stockList가 5개일 경우, 5개 항목을 채우고 나머지는 빈 객체로 추가
        while (stockList.size() < 10) {
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("iname", "");
            emptyItem.put("status", "");
            emptyItem.put("ownm", "");
            emptyItem.put("ouprc", 0);
            emptyItem.put("oqty", 0);
            emptyItem.put("supply", 0);
            emptyItem.put("tax", 0);
            stockList.add(emptyItem);
        }

        model.addAttribute("stockList", stockList);

        // ✅ 정확한 1건 입고 행 조회
        List<OrdersDto> order1 = ordersRepository.findByOno(ono); // ocode(입고서 행번호)에 해당하는 OrdersDto 객체 한 건을 DB에서 찾아오는 코드
        if (order1 != null) {
            model.addAttribute("order1", order1);

            // ✅ 공급처 정보 가져오기
            OrdersDto firstOrder = order1.get(0);
            ClientDto supplier = clientRepository.findByCgubunAndCcode("sup", firstOrder.getSupcd());
            model.addAttribute("supplier", supplier);
        }
        
        return "stockIn/list";
    }
    
    // 입고 저장(불용 없을 때 전체 저장1)
    @PostMapping("/saveStockInData")
    @ResponseBody
    public ResponseEntity<Void> saveStockInData(@RequestBody List<OrdersDto> stockList,
    											HttpSession session) {
    	
    	// (필요하면) 저장 시에도 누가 저장하는지 세션에서 꺼내 쓸 수 있습니다.
    	Integer loginEcode = (Integer) session.getAttribute("LOGIN_ECODE");
    	String  loginEname = (String)  session.getAttribute("LOGIN_ENAME");
        
    	for (OrdersDto dto : stockList) {
            // 저장 처리
            ordersService.save(dto);
        }
        return ResponseEntity.ok().build();
    }
    
  }  
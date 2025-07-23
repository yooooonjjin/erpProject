package com.teamProject2.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.teamProject2.entity.ReasonDto;
import com.teamProject2.service.ReasonService;
import com.teamProject2.entity.OrdersId;
import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.OrdersDto;
import com.teamProject2.repository.ClientRepository;
import com.teamProject2.repository.InventoryRepository;
import com.teamProject2.repository.OrdersRepository;
import com.teamProject2.repository.ReasonRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;


@Service
public class OrdersService {
	
	public final OrdersRepository ordersRepository;	
	public final ClientRepository clientRepository;	
	public final InventoryRepository inventoryRepository;
	public final ReasonRepository reasonRepository;
	private final ReasonService reasonService;
	public OrdersService(OrdersRepository ordersRepository,ClientRepository clientRepository,InventoryRepository inventoryRepository,ReasonRepository reasonRepository, ReasonService reasonService) {
		this.ordersRepository = ordersRepository;
		this.clientRepository = clientRepository;
		this.inventoryRepository = inventoryRepository;
		this.reasonRepository = reasonRepository;
		this.reasonService = reasonService;
	}
	
	/**
	 * 발주 목록 총 발주량
	 */
	public Map<String, Long> countByOnoGroup(String ogubun) {
		List<OrdersDto> allList = ordersRepository.findByOgubunAndDelst(ogubun, "Y", Pageable.unpaged()).getContent();

	    // ogubun이 "ORD"이고 ono가 같은 데이터 수 세기
	    return allList.stream()
	        .filter(o -> "ORD".equals(o.getOgubun()))
	        .collect(Collectors.groupingBy(
	            o -> o.getOno().toString(), // ono를 기준으로 그룹핑
	            Collectors.counting()       // 각 그룹의 개수 세기
	        ));
	}
	
	/**
	 * 발주 목록 출력(페이징)
	 */
	public Page<OrdersDto> listByOgubun(int page, int size, String ogubun) {
	    Pageable pageable = PageRequest.of(page, size, Sort.by(
	        Sort.Order.desc("ordate"),
	        Sort.Order.asc("ogubun"),
	        Sort.Order.asc("ono")
	    ));

	    // ORD 전체 데이터 가져오기
	    List<OrdersDto> allList = ordersRepository.findByOgubunAndDelst(ogubun, "Y", Pageable.unpaged()).getContent();

	    // 중복 제거: ogubun + ono 기준, ostate가 null인 경우 새 값으로 대체
	    List<OrdersDto> deduplicated = allList.stream()
	        .filter(o -> "ORD".equals(o.getOgubun()))
	        .collect(Collectors.toMap(
	            o -> o.getOgubun() + "_" + o.getOno(), // key
	            o -> o, // 처음 값
	            (existing, replace) -> {
	                // 기존 값의 ostate가 null이면 새로 들어온 값으로 대체
	                return existing.getOstate() == null && replace.getOstate() != null ? replace : existing;
	            }
	        ))
	        .values()
	        .stream()
	        .sorted((a, b) -> b.getOrdate().compareTo(a.getOrdate()))
	        .toList();

	    // 페이징 처리
	    int start = page * size;
	    int end = Math.min(start + size, deduplicated.size());
	    List<OrdersDto> pageContent = deduplicated.subList(start, end);

	    return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, deduplicated.size());
	}

	
	/**
	 * 발주 목록에 보여질 공급처명 가져오기
	 */
	public Map<Integer, String> getSupplierNamesForOrders(List<Integer> onoList) {
	    List<Object[]> rawList = ordersRepository.findSupplierNamesByOnoList(onoList);

	    return rawList.stream()
	    	    .collect(Collectors.toMap(
	    	        r -> (Integer) r[0], // ono
	    	        r -> (String) r[1] != null ? (String) r[1] : "-",
	    	        (existing, duplicate) -> existing // 중복 시 기존 값 유지
	    	    ));
	}

	/**
	 * 검색 조건 + 페이징
	 */
	public Page<OrdersDto> searchOrders(
		    int page, int size,
		    String ordate, String ostate, String supcd, String omgr, String onote,
		    String ogubun
		) {
		    Pageable pageable = PageRequest.of(page, size, Sort.by("ordate").descending()
		        .and(Sort.by("ogubun")).and(Sort.by("ono")));

		    Page<OrdersDto> rawPage = ordersRepository.searchOrders(
		        ogubun,
		        blankToNull(ordate),
		        blankToNull(ostate),
		        blankToNull(supcd),
		        blankToNull(omgr),
		        blankToNull(onote),
		        Pageable.unpaged() // 모든 데이터 가져옴 (중복 제거용)
		    );

		    // 중복 제거
		    List<OrdersDto> deduplicated = rawPage.getContent().stream()
		        .collect(Collectors.toMap(
		            o -> o.getOgubun() + "_" + o.getOno(),
		            o -> o,
		            (existing, replacement) -> existing.getOstate() == null && replacement.getOstate() != null ? replacement : existing
		        ))
		        .values()
		        .stream()
		        .sorted((a, b) -> b.getOrdate().compareTo(a.getOrdate()))
		        .toList();

		    // 중복 제거된 리스트로 페이징 처리
		    int start = page * size;
		    int end = Math.min(start + size, deduplicated.size());
		    List<OrdersDto> pageContent = deduplicated.subList(start, end);

		    return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, deduplicated.size());
		}

	
	/**
	 * 총 데이터 개수
	 */
	public Long count() {
		return ordersRepository.count();
	}
	
	/**
	 * 발주신청 고유번호
	 */
	public Integer getNextOnoVal() {
		return ordersRepository.getNextOnoVal();
	}
	/**
	 * null 또는 빈 문자열을 null로 변환하는 헬퍼 메서드
	 */
	private String blankToNull(String s) {
	    return (s == null || s.trim().isEmpty()) ? null : s.trim(); 
	}
	
	/**
	 * 발주신청서 공급처 자동완성기능
	 */
	public List<ClientDto> findSuppliersByName(String keyword) {
	    String lowerKeyword = keyword.trim().toLowerCase(); // 앞뒤 공백 제거 + 소문자 처리

	    return clientRepository.findByCgubun("sup").stream()
	        .filter(c -> {
	            String cnameLower = c.getCname().toLowerCase();
	            String chosung = getChosung(c.getCname()).toLowerCase();

	            return cnameLower.contains(lowerKeyword) || chosung.contains(lowerKeyword);
	        })
	        .toList();
	}
	
	/**
	 * 자재 자동완성 기능
	 */
	public List<Map<String, Object>> findItemsByKeyword(String keyword) {
	    String lowerKeyword = keyword.toLowerCase();
	    return inventoryRepository.findAll().stream()
	        .filter(i -> {
	            String igubun = i.getIgubun();
	            return !"SMG".equalsIgnoreCase(igubun);
	        })
	        .filter(i -> i.getIname().toLowerCase().contains(lowerKeyword)
	                  || getChosung(i.getIname()).toLowerCase().contains(lowerKeyword))
	        .limit(10)
	        .map(i -> {
	            Map<String, Object> map = new HashMap<>();
	            map.put("icode", i.getIcode());
	            map.put("iname", i.getIname());
	            map.put("iunit", i.getIunit());
	            map.put("iuprc", i.getIuprc());
	            map.put("igubun", i.getIgubun());
	            return map;
	        })
	        .toList();
	}




    public ClientDto findSupplierByCcode(int ccode) {
        return clientRepository.findByCgubunAndCcode("sup", ccode);
    }
    
    /**
     * 초성까지도 검색하는 기능
     */
    private static final char[] CHOSUNG = {
    	    'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ',
    	    'ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    	};

    	private String getChosung(String text) {
    	    StringBuilder sb = new StringBuilder();
    	    for (char c : text.toCharArray()) {
    	        if (c >= 0xAC00 && c <= 0xD7A3) {
    	            int choIndex = (c - 0xAC00) / (21 * 28);
    	            sb.append(CHOSUNG[choIndex]);
    	        } else {
    	            sb.append(c); // 비한글 문자 그대로
    	        }
    	    }
    	    return sb.toString();
    	}
    	
    	// 발주신청용 insert
    	@Transactional
    	public int saveOrderBatch(
    	        int supcd,
    	        String omgr,
    	        int empcd,
    	        String ogubun,
    	        String ostate,
    	        String onote,
    	        int oaqty,
    	        int oasuprc,
    	        int oatax,
    	        int oatprc,
    	        List<String> odateList,
    	        List<Integer> oqtyList,
    	        List<Integer> ouprcList,
    	        List<Integer> osuprcList,
    	        List<Integer> otaxList,
    	        List<Integer> otprcList,
    	        List<String> igubunList,
    	        List<Integer> icodeList
    	) {
    	    try {
    	        Integer ono = ordersRepository.getNextOnoVal();
    	        Timestamp ordate = Timestamp.valueOf(LocalDateTime.now());

    	        int size = oqtyList.size();
    	        for (int i = 0; i < size; i++) {
    	            Integer qty = oqtyList.get(i);
    	            String date = odateList.get(i);
    	            Integer price = ouprcList.get(i);

    	            if (qty == null || qty == 0) continue;
    	            if (date == null || date.isBlank()) continue;
    	            if (price == null) continue;

    	            OrdersDto dto = new OrdersDto();
    	            dto.setOno(ono);
    	            dto.setOcode(ordersRepository.getNextOcodeVal());
    	            dto.setOgubun(ogubun);
    	            dto.setOrdate(ordate);
    	            dto.setSupcd(supcd);
    	            dto.setOmgr(omgr);
    	            dto.setEmpcd(empcd);
    	            dto.setOstate(ostate);
    	            dto.setOnote(onote);
    	            dto.setOaqty(oaqty);
    	            dto.setOasuprc(oasuprc);
    	            dto.setOatax(oatax);
    	            dto.setOatprc(oatprc);
    	            dto.setOqty(qty);
    	            dto.setOuprc(price);
    	            dto.setOsuprc(osuprcList.get(i));
    	            dto.setOtax(otaxList.get(i));
    	            dto.setOtprc(otprcList.get(i));

    	            if (date != null && !date.isBlank()) {
    	                dto.setOdate(Timestamp.valueOf(date + " 00:00:00"));
    	            }

    	            // 핵심 변경: igubun/icode를 기반으로 분기하여 세팅
    	            String gubun = igubunList.get(i);
    	            Integer icode = icodeList.get(i);

    	            if (gubun != null) {
    	                switch (gubun.toUpperCase()) {
    	                    case "MAT":
    	                        dto.setMatcd(icode);
    	                        dto.setFaccd(null);
    	                        dto.setPrdcd(null);
    	                        break;
    	                    case "FAC":
    	                        dto.setFaccd(icode);
    	                        dto.setMatcd(null);
    	                        dto.setPrdcd(null);
    	                        break;
    	                    case "PRD":
    	                        dto.setPrdcd(icode);
    	                        dto.setMatcd(null);
    	                        dto.setFaccd(null);
    	                        break;
    	                }
    	            }

    	            ordersRepository.save(dto);
    	        }
    	        return 1; // 성공
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        return 0; // 실패
    	    }
    	}
    	
    	// 발주신청 수정용 delete -> insert
    	@Transactional
    	public int updateOrderBatch(
    	    Integer ono,
    	    String ogubun,
    	    int supcd,
    	    String omgr,
    	    int empcd,
    	    String ostate,
    	    String onote,
    	    int oaqty,
    	    int oasuprc,
    	    int oatax,
    	    int oatprc,
    	    List<String> odateList,
    	    List<Integer> oqtyList,
    	    List<Integer> ouprcList,
    	    List<Integer> osuprcList,
    	    List<Integer> otaxList,
    	    List<Integer> otprcList,
    	    List<String> igubunList,
    	    List<Integer> icodeList
    	) {
    	    try {
    	        // 기존 ordate 조회 (가장 앞의 하나만 가져오면 됨)
    	        Timestamp originalOrdate = null;
    	        List<OrdersDto> existing = ordersRepository.findByOnoAndOgubun(ono, ogubun);
    	        if (!existing.isEmpty()) {
    	            originalOrdate = existing.get(0).getOrdate();  // 기준값 사용
    	        }

    	        // 기존 데이터 삭제
    	        ordersRepository.deleteByOnoAndOgubun(ono, ogubun);

    	        // 수정된 발주일자
    	        Timestamp oudate = Timestamp.valueOf(LocalDateTime.now());

    	        int size = oqtyList.size();
    	        for (int i = 0; i < size; i++) {
    	            Integer qty = oqtyList.get(i);
    	            String date = odateList.get(i);
    	            Integer price = ouprcList.get(i);

    	            if (qty == null || qty == 0) continue;
    	            if (date == null || date.isBlank()) continue;
    	            if (price == null) continue;

    	            OrdersDto dto = new OrdersDto();
    	            dto.setOno(ono);
    	            dto.setOcode(ordersRepository.getNextOcodeVal());
    	            dto.setOgubun(ogubun);
    	            dto.setOudate(oudate);
    	            dto.setOrdate(originalOrdate); // 기존 ordate 유지
    	            dto.setSupcd(supcd);
    	            dto.setOmgr(omgr);
    	            dto.setEmpcd(empcd);
    	            dto.setOstate(ostate);
    	            dto.setOnote(onote);
    	            dto.setOaqty(oaqty);
    	            dto.setOasuprc(oasuprc);
    	            dto.setOatax(oatax);
    	            dto.setOatprc(oatprc);
    	            dto.setOqty(qty);
    	            dto.setOuprc(price);
    	            dto.setOsuprc(osuprcList.get(i));
    	            dto.setOtax(otaxList.get(i));
    	            dto.setOtprc(otprcList.get(i));

    	            if (date != null && !date.isBlank()) {
    	                dto.setOdate(Timestamp.valueOf(date + " 00:00:00"));
    	            }

    	            String gubun = igubunList.get(i);
    	            Integer icode = icodeList.get(i);

    	            if (gubun != null) {
    	                switch (gubun.toUpperCase()) {
    	                    case "MAT":
    	                        dto.setMatcd(icode);
    	                        dto.setFaccd(null);
    	                        dto.setPrdcd(null);
    	                        break;
    	                    case "FAC":
    	                        dto.setFaccd(icode);
    	                        dto.setMatcd(null);
    	                        dto.setPrdcd(null);
    	                        break;
    	                    case "PRD":
    	                        dto.setPrdcd(icode);
    	                        dto.setMatcd(null);
    	                        dto.setFaccd(null);
    	                        break;
    	                }
    	            }

    	            ordersRepository.save(dto);
    	        }

    	        return 1;
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        return 0;
    	    }
    	}




    	// 디테일 화면 띄우기
    	public List<OrdersDto> detail(int ono,String ogubun){
    		return ordersRepository.findByOnoAndOgubun(ono, ogubun);
    	}
    	
		/**
		 * 디테일 조인용
		 */
    	public List<OrdersDto> getOrderDetail(Integer ono, String ogubun) {
    	    List<Object[]> rows = ordersRepository.findOrdersRaw(ono, ogubun);
    	    List<OrdersDto> result = new ArrayList<>();

    	    for (Object[] row : rows) {
    	        OrdersDto dto = new OrdersDto();

    	        dto.setOno((Integer) row[0]);
    	        dto.setOgubun((String) row[1]);
    	        dto.setOcode((Integer) row[2]);
    	        dto.setOuprc((Integer) row[3]);
    	        dto.setOqty((Integer) row[4]);
    	        dto.setOtprc((Integer) row[5]);
    	        dto.setOstate((String) row[6]);
    	        dto.setOrdate((Timestamp) row[7]);
    	        dto.setOudate((Timestamp) row[8]);
    	        dto.setOidate((Timestamp) row[9]);
    	        dto.setOdate((Timestamp) row[10]);
    	        dto.setOmgr((String) row[11]);
    	        dto.setOwnm((String) row[12]);
    	        dto.setSupcd((Integer) row[13]);
    	        dto.setEmpcd((Integer) row[14]);
    	        dto.setMatcd((Integer) row[15]);
    	        dto.setPrdcd((Integer) row[16]);
    	        dto.setFaccd((Integer) row[17]);
    	        dto.setOnote((String) row[18]);
    	        dto.setOsuprc((Integer) row[19]);
    	        dto.setOasuprc((Integer) row[20]);
    	        dto.setOtax((Integer) row[21]);
    	        dto.setOatax((Integer) row[22]);
    	        dto.setOaqty((Integer) row[23]);
    	        dto.setOatprc((Integer) row[24]);
    	        dto.setDelst(row[25] != null ? row[25].toString() : null);

    	        // 조인된 필드
    	        dto.setCname((String) row[26]);
    	        dto.setCowner((String) row[27]);
    	        dto.setCphone((String) row[28]);
    	        dto.setEname((String) row[29]);
    	        dto.setEphone((String) row[30]);
    	        dto.setDname((String) row[31]);
    	        dto.setIname((String) row[32]);
    	        dto.setIunit((String) row[33]);
    	        dto.setIcode((Integer) row[34]);
    	        dto.setIgubun((String) row[35]);
    	        
    	        System.out.println("row[34] type = " + (row[34] == null ? "null" : row[34].getClass().getName()));
    	        System.out.println("row[34] value = " + row[34]);


    	        result.add(dto);
    	    }
    	    
    	    

    	    return result;
    	}


    	// 발주서 삭제처리
    	@Transactional
    	public int updateDelst(int ono,String ogubun) {
    		return ordersRepository.updateDelst(ono, ogubun);
    	}

    	
    	
    	/**
    	 * 
    	 * 입고
    	 * 
    	 */
    	// 입고 코드 생성 (ogubun + ono 형태로)
    	public String generateNextReasonCode(int ocode) {
    	    // 입고 코드 생성 로직 확인
    	    String reasonCode = "STI" + String.format("%03d", ocode);  // 예시로 ocode 기반 입고 코드 생성
    	    return reasonCode;
    	}

    	public Page<Object[]> getFilteredOrders(String status, String client, String manager, String date, Pageable pageable) {
    	    return ordersRepository.findFilteredOrders(status, client, manager, date, pageable);
    	}
    	
    	// 입고 명세서 데이터
    	public List<Map<String, Object>> getStockInData(Integer ono) {
    		 // 입고 데이터를 조회하는 쿼리
    	    List<Object[]> rows = ordersRepository.findStockInData(ono);
    	    List<Map<String, Object>> result = new ArrayList<>();

    	    for (Object[] row : rows) {
    	        Map<String, Object> map = new HashMap<>();
    	        System.out.println("========="+row[5]);
    	        // row[0] = ono / row[1] = ostate / row[2] = oqty / row[3] = 입고코드 / row[4] = 자재명/ row[5]= cname/ row[6]= omgr / row[7] = odate
    	        String iname = (String) row[4];
    	        String ownm = (String) row[1];
    	        Integer ouprc = ((Number) row[5]).intValue();
    	        Integer oqty = ((Number) row[2]).intValue();
    	        Integer supply = ouprc * oqty;
    	        Integer tax = (int) (supply * 0.1);

    	        map.put("iname", iname);
    	        map.put("status", "가용");
    	        map.put("ownm", ownm);
    	        map.put("ouprc", ouprc);
    	        map.put("oqty", oqty);
    	        map.put("stiqty", oqty); // 처음엔 입고수량 = 발주수량
    	        map.put("supply", supply);
    	        map.put("tax", tax);

    	        result.add(map);
    	    }

    	    return result;
    	}

    	public void saveAll(List<OrdersDto> data) {
            ordersRepository.saveAll(data);
        }

    	    public List<OrdersDto> findAll() {
    	        return ordersRepository.findAll();
    	    }
    	    
    	    public List<OrdersDto> getOrdersByOcode(int ono) {
    	        return ordersRepository.findByOno(ono); // ocode로 주문 데이터 조회
    	    }

    	    // 불용 사유 저장 후 상태 변경
    	    public void insertReasonAndUpdateState(ReasonDto reasonDto, Integer ono) {
    	        // 1. 불용 사유 저장
    	        reasonService.save(reasonDto);
    	        // 2. 주문 상태 업데이트
    	        updateOrderState(ono);
    	    }

    	    // 주문 상태 변경
    	    @Transactional
    	    public void updateOrderState(Integer ono) {
    	    	List<OrdersDto> orderList = ordersRepository.findByOno(ono);
    	        if (orderList == null || orderList.isEmpty()) {
    	            throw new RuntimeException("주문을 찾을 수 없습니다. ono: " + ono);
    	        }

    	        for (OrdersDto order : orderList) {
    	            order.setOstate("입고 완료");
    	            order.setOidate(new Timestamp(System.currentTimeMillis()));

    	            // 👇 조건 없이 무조건 최길동(1005)으로 담당자 변경
    	            order.setEmpcd(1005);

    	            ordersRepository.save(order);
    	        }

    	    }

    	    public void save(OrdersDto dto) {
    	        // 복합키 생성
    	        OrdersId ordersId = new OrdersId(dto.getOno(), dto.getOgubun(), dto.getOcode());

    	        // 기존 주문 찾기
    	        Optional<OrdersDto> optional = ordersRepository.findById(ordersId);
    	        if (optional.isPresent()) {
    	            OrdersDto order = optional.get();

    	            // 입고 수량 누적 저장
    	            order.setStiqty(dto.getStiqty());

    	            // 공급가액 및 세액 저장
    	            order.setOsuprc(dto.getOsuprc());
    	            order.setOtax(dto.getOtax());

    	            // 입고 완료로 상태 변경
    	            order.setOstate("입고 완료");

    	            ordersRepository.save(order); // 저장
    	        }
    		}

} 
package com.teamProject2.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.repository.OrdersRepository;
import com.teamProject2.entity.OrdersId;
import com.teamProject2.entity.ReasonDto;
import com.teamProject2.repository.ReasonRepository;
import com.teamProject2.service.ReasonService;
import com.teamProject2.repository.ClientRepository;
import com.teamProject2.repository.InventoryRepository;
import com.teamProject2.entity.ClientDto;

@Service
public class OrdersService {

	public final OrdersRepository ordersRepository;
	public final ClientRepository clientRepository;	
	public final InventoryRepository inventoryRepository;
	public final ReasonRepository reasonRepository;
	private final ReasonService reasonService;
	
	public OrdersService(OrdersRepository ordersRepository,
						 ClientRepository clientRepository,
						 InventoryRepository inventoryRepository,
						 ReasonRepository reasonRepository,
						 ReasonService reasonService) {
		
		this.ordersRepository = ordersRepository;
		this.clientRepository = clientRepository;
		this.inventoryRepository = inventoryRepository;
		this.reasonRepository = reasonRepository;
		this.reasonService = reasonService;
	}
	
	@PersistenceContext
	// 공급처명 조회용
	private EntityManager em;
	
	
	/**
	 * 윤진 시작
	 */
	
	/*
	 * 총 주문 건수 구하기
	 */	
	public Long count(String ogubun) {
		List<OrdersDto> all = ordersRepository.findByOgubun(ogubun);
        
		return all.stream()
                  .map(OrdersDto::getOno)	// 발주번호(ONO)만 추출
                  .distinct()				// 중복 제거
                  .count();					// 남은 ONO의 개수 카운트
    }
	
	/**
     * CGUBUN='SUP' 인 모든 공급처 코드를 이름으로 맵 반환
     */
    public Map<Integer, String> getSupplierMap() {
        // Object[] { ccod e, cname }
        List<Object[]> rows = em.createQuery(
            "SELECT c.ccode, c.cname FROM ClientDto c WHERE c.cgubun = 'sup'",
            Object[].class
        ).getResultList();

        return rows.stream().collect(Collectors.toMap(
           r -> (Integer) r[0],
           r -> (String)  r[1]
       ));
    }

	/**
	 * OGUBUN='ORD'인 전체 발주 개수를
	 * ONO별로 그룹핑해서 총 발주 건수 조회
	 */	
    public Map<Integer, Long> getItemCountMap(String ogubun) {
    	List<OrdersDto> all = ordersRepository.findByOgubun(ogubun);
    	
    	return all.stream().collect(Collectors.groupingBy(
    			OrdersDto::getOno,
    			Collectors.counting()
    	));
    }

    /**
	 * 검색 상태 드롭다운
	 */
	public List<String> getStatusList(String ogubun) {
		return ordersRepository.findstateByOgubun(ogubun);
	}
	
	/**
	 * 발주 결재 목록 (페이징 + 중복 제거 + 검색 기능)
	 */
	public Page<OrdersDto> allList(	int page,
									int size,
									String ogubun,
									LocalDate ordate,
									String ostate,
									String supnm,
									String mgrnm,
									String word) {
		
	    // 0) 미리 조회 할 맵들
	    Map<Integer, Long> itemCountMap = getItemCountMap(ogubun);
	    Map<Integer, String> supMap = getSupplierMap();
	    
	    // 1) 구분이 ORD인 전체 목록 조회
	 	List<OrdersDto> ordList = ordersRepository.findByOgubun(ogubun);
	 	
	 	// 2) ONO 중복 제거
		Map<Integer, OrdersDto> map = new LinkedHashMap<>();
		for (OrdersDto dto : ordList) {
			if( !map.containsKey(dto.getOno()) ) {
				map.put(dto.getOno(), dto);
			}
		}
		List<OrdersDto> deduped = new ArrayList<>(map.values());
		
		// 3) 필터 적용
		Stream<OrdersDto> stream = deduped.stream();
		
		// 발주 신청 일자
	    if (ordate != null) {
	        stream = stream.filter(o ->
	            o.getOrdate()
	            .toLocalDateTime()
	            .toLocalDate()
	            .equals(ordate)
	        );
	    }
	    // 발주 상태
	    if (ostate != null && !ostate.isBlank()) {
	        stream = stream.filter(o -> ostate.equals(o.getOstate()));
	    }
	    // 담당자명 (앞뒤 공백 제거, 소문자 무시)
	    if (mgrnm != null && !mgrnm.isBlank()) {
	        String normMgr = mgrnm.trim().toLowerCase();
	        stream = stream.filter(o -> {
	            String mgr = o.getOmgr() == null ? "" : o.getOmgr()
	                               .replaceAll("\\s+", "")
	                               .toLowerCase();
	            return mgr.contains(normMgr);
	        });
	    }
	    // 공급처명 (앞뒤 공백 제거, 소문자 무시)
	    if (supnm != null && !supnm.isBlank()) {
	        String normSup = supnm.trim().toLowerCase();
	        stream = stream.filter(o -> {
	            String supName = supMap.getOrDefault(o.getSupcd(), "")
	                                   .replaceAll("\\s+", "")
	                                   .toLowerCase();
	            return supName.contains(normSup);
	        });
	    }
	    // 코드, 공급처, 담당자, 특이사항 (앞뒤 공백 제거, 소문자 무시)
	    if (word != null && !word.isBlank()) {
	        // 1) 검색어 앞뒤 공백 제거 + 소문자 변환
	        String norm = word.trim().toLowerCase();

	        stream = stream.filter(o -> {
	            // 2) 각 필드별 공백 제거 + 소문자화
	            String code = (o.getOgubun() + o.getOno())
	                              .replaceAll("\\s+", "")
	                              .toLowerCase();
	            String sup  = supMap.getOrDefault(o.getSupcd(), "")
	                              .replaceAll("\\s+", "")
	                              .toLowerCase();
	            String mgr  = (o.getOmgr() == null ? "" : o.getOmgr())
	                              .replaceAll("\\s+", "")
	                              .toLowerCase();
	            String note = (o.getOnote() == null ? "" : o.getOnote())
	                              .replaceAll("\\s+", "")
	                              .toLowerCase();
	          
	            // 3) 합친 뒤 contains 검사
	            String combined = code + sup + mgr + note;
	            return combined.contains(norm);
	        });
	    }
		
	    // 4) 정렬 : 신청일자 내림차순
        List<OrdersDto> filtered = stream
            .sorted(Comparator.comparing(OrdersDto::getOrdate).reversed())
            .toList();
		
		
		// 5) 페이징
        Pageable pageable = PageRequest.of(
        		page,
        		size,
        		Sort.by("ordate").descending());
        
        int total = filtered.size();				// 전체 필터링 후 건수
		int start = page * size;					// 시작 인덱스						
		int end   = Math.min(start + size, total);	// 끝 인덱스	
		
		List<OrdersDto> content = (start >= total)
				? List.of()
				: filtered.subList(start, end);

		return new PageImpl<>(content, pageable, total);
	}
	
	/**
	 * 결재 대기 목록 (검색 필터 + 페이징 처리)
	 */
	public Page<OrdersDto> getPendingList(int page,
            int size,
            String ogubun,
            LocalDate ordate,
            String ostate,
            String supnm,
            String mgrnm,
            String word) {

	// 0) 필터 및 매핑에 필요한 부가 정보 (공급처명, 발주 건수)
	Map<Integer, String> supMap = this.getSupplierMap();          // 공급처 코드 → 공급처명
	Map<Integer, Long> itemCountMap = this.getItemCountMap(ogubun); // 발주번호 → 자재건수
	
	// 1) 기본 조건 (OGUBUN)으로 전체 조회
	List<OrdersDto> orderList = ordersRepository.findByOgubun(ogubun);
	
	// 2) ONO 중복 제거 (한 건의 발주에 여러 품목 존재 시 대표 1건만 보여주기)
	Map<Integer, OrdersDto> uniqueMap = new LinkedHashMap<>();
		for (OrdersDto dto : orderList) {
			if (!uniqueMap.containsKey(dto.getOno())) {
				uniqueMap.put(dto.getOno(), dto);
			}
		}
	List<OrdersDto> dedupedList = new ArrayList<>(uniqueMap.values());
	
	// 3) 검색 필터링 시작
	Stream<OrdersDto> stream = dedupedList.stream();
	
	// 4) 상태: 결재 대기
	if (ostate != null && !ostate.isBlank()) {
		stream = stream.filter(o -> ostate.equals(o.getOstate()));
	}
	
	// 5) 신청일자
	if (ordate != null) {
	stream = stream.filter(o -> 
		o.getOrdate().toLocalDateTime().toLocalDate().equals(ordate)
		);
	}
	
	// 6) 공급처명
	if (supnm != null && !supnm.isBlank()) {
		String keyword = supnm.trim().toLowerCase();
		stream = stream.filter(o -> {
			String name = supMap.getOrDefault(o.getSupcd(), "")
		      .replaceAll("\\s+", "")
		      .toLowerCase();
			return name.contains(keyword);
		});
	}
	
	// 7) 담당자명
	if (mgrnm != null && !mgrnm.isBlank()) {
	String keyword = mgrnm.trim().toLowerCase();
	stream = stream.filter(o -> {
	String mgr = o.getOmgr() == null ? "" : o.getOmgr().replaceAll("\\s+", "").toLowerCase();
	return mgr.contains(keyword);
	});
	}
	
	// 8) 통합 검색 (발주코드, 공급처, 담당자, 특이사항)
	if (word != null && !word.isBlank()) {
	String keyword = word.trim().toLowerCase();
	stream = stream.filter(o -> {
	String code = (o.getOgubun() + o.getOno()).toLowerCase();
	String sup = supMap.getOrDefault(o.getSupcd(), "").toLowerCase();
	String mgr = o.getOmgr() == null ? "" : o.getOmgr().toLowerCase();
	String note = o.getOnote() == null ? "" : o.getOnote().toLowerCase();
	return (code + sup + mgr + note).contains(keyword);
	});
	}
	
	// 9) 정렬: 신청일자 내림차순
	List<OrdersDto> filteredList = stream
	.sorted(Comparator.comparing(OrdersDto::getOrdate).reversed())
	.toList();
	
	// 10) 페이징 처리
	Pageable pageable = PageRequest.of(page, size);
	int total = filteredList.size();
	int start = page * size;
	int end = Math.min(start + size, total);
	
	List<OrdersDto> pageContent = (start >= total)
	? List.of()
	: filteredList.subList(start, end);
	
	return new PageImpl<>(pageContent, pageable, total);
	}
	
	/**
	 * 결재 대기함 (전체 건수)
	 */
	public Long countPending(String ogubun, String ostate) {
	    return ordersRepository.countByOgubunAndOstate(ogubun, ostate);
	}
	
	/**
	 * 결재 이력 목록 (검색 필터 + 페이징)
	 */
	public Page<OrdersDto> getHistoryList(
	        int page,
	        int size,
	        String ogubun,
	        LocalDate ordate,
	        List<String> ostateList,
	        String supnm,
	        String mgrnm,
	        String word) {

	    // 공급처명·건수 맵 조회
	    Map<Integer,String> supMap = getSupplierMap();
	    Map<Integer,Long> itemCountMap = getItemCountMap(ogubun);

	    // 1) 전체 조회 → 중복 제거
	    List<OrdersDto> all = ordersRepository.findByOgubun(ogubun);
	    LinkedHashMap<Integer,OrdersDto> uniq = new LinkedHashMap<>();
	    for (OrdersDto o : all) {
	        uniq.putIfAbsent(o.getOno(), o);
	    }

	    // 2) 스트림 필터링
	    Stream<OrdersDto> stream = uniq.values().stream()
	        // 상태 IN ("결재 승인","결재 반려")
	        .filter(o -> ostateList.contains(o.getOstate()));

	    if (ordate != null) {
	        stream = stream.filter(o -> 
	            o.getOrdate().toLocalDateTime().toLocalDate().equals(ordate));
	    }
	    if (supnm != null && !supnm.isBlank()) {
	        String key = supnm.trim().toLowerCase();
	        stream = stream.filter(o ->
	            supMap.getOrDefault(o.getSupcd(), "")
	                  .replaceAll("\\s+","")
	                  .toLowerCase()
	                  .contains(key));
	    }
	    if (mgrnm != null && !mgrnm.isBlank()) {
	        String key = mgrnm.trim().toLowerCase();
	        stream = stream.filter(o ->
	            (o.getOmgr()==null? "":o.getOmgr())
	                  .replaceAll("\\s+","")
	                  .toLowerCase()
	                  .contains(key));
	    }
	    if (word != null && !word.isBlank()) {
	        String key = word.trim().toLowerCase();
	        stream = stream.filter(o -> {
	            String combined = (o.getOgubun()+o.getOno())
	                + supMap.getOrDefault(o.getSupcd(),"")
	                + (o.getOmgr()==null?"":o.getOmgr())
	                + (o.getOnote()==null?"":o.getOnote());
	            return combined.replaceAll("\\s+","").toLowerCase().contains(key);
	        });
	    }

	    // 3) 정렬 및 페이징
	    List<OrdersDto> filtered = stream
	        .sorted(Comparator.comparing(OrdersDto::getOrdate).reversed())
	        .toList();

	    Pageable pageable = PageRequest.of(page, size);
	    int total = filtered.size();
	    int start = page * size;
	    int end = Math.min(start + size, total);
	    List<OrdersDto> content = start >= total ? List.of() : filtered.subList(start, end);

	    return new PageImpl<>(content, pageable, total);
	}

	/**
	 * 결재 이력 전체 건수 조회
	 */
	public Long countHistory(String ogubun) {
	    return ordersRepository.countByOgubunAndOstateIn(
	        ogubun,
	        List.of("결재 승인", "결재 반려")
	    );
	}
	
	/**
	 * 내가 결재(승인·반려)한 이력 조회 (페이징/검색)
	 */
	
	public Page<OrdersDto> getMyHistoryList(
            int page,
            int size,
            String ogubun,
            LocalDate ordate,
            String supnm,
            String mgrnm,
            String word,
            Integer loginEmpCd,
            List<String> statuses) {
        Pageable pageable = PageRequest.of(page, size);

        return ordersRepository.findMyHistoryOrders(
                ogubun,
                ordate,
                statuses,
                supnm,
                mgrnm,
                word,
                loginEmpCd,
                pageable
        );
    }
	
	
	
	
	/**
	 * 발주서 상세보기 (자재 정보)
	 */
	public List<Map<String, Object>> detail(int ono) {
		
		List<Map<String, Object>> result = ordersRepository.findOrderDetailsByOno(ono);

        // null 이거나 INAME이 없는 빈 Map은 제거
        List<Map<String, Object>> filtered = result.stream()
            .filter(map -> map.get("INAME") != null)
            .collect(Collectors.toList());

        return filtered;
	}
	
	/**
	 * 발주서 상세보기 (수신처)
	 */
	public Map<String, Object> getReceiverInfo(int ono) {
	    
		List<Map<String, Object>> list = ordersRepository.findReceiverInfoByOno(ono);
	    
		// 첫 번째 결과만 사용
		return list.isEmpty() ? null : list.get(0);
	}


	/**
	 * 발주서 상세보기 (공급처)
	 */
	public Map<String, Object> getSupplierInfo(int ono) {
	    List<Map<String, Object>> list = ordersRepository.findSupplierInfoByOno(ono);
	    return list.isEmpty() ? null : list.get(0);
	}

	/** 발주서 합계 가져오기 */
	@Transactional(readOnly = true)
    public OrdersDto getOrderSummary(int ono) {
        List<OrdersDto> rows = ordersRepository.findByOnoAndOgubunAndDelst(ono, "ORD", "Y");

        if (rows.isEmpty()) {
            throw new NoSuchElementException("발주번호 " + ono + " 에 해당하는 데이터가 없습니다.");
        }

        // 첫 번째 행만 꺼내 반환
        return rows.get(0);
    }
	/**
	 * 윤진 끝
	 */

	
	
	
	/**
	 * 수림 시작
	 */

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

	        result.add(dto);
	    }
	    
	    

	    return result;
	}


	// 발주서 삭제처리
	@Transactional
	public int updateDelst(int ono,String ogubun) {
		return ordersRepository.updateDelst(ono, ogubun);
	}
	
	// 로그인한 사람 목록 띄우기
    // empcd String 타입으로 맞추기
	public Page<OrdersDto> listByOgubunAndLoginEmp(int page, int size, String ogubun, String empcd, String omgr) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("ordate").descending());

        List<OrdersDto> allList = ordersRepository.findByOgubunAndDelst(ogubun, "Y", Pageable.unpaged()).getContent();

        List<OrdersDto> filtered = allList.stream()
            .filter(o -> o.getEmpcd() != null && empcd.equals(String.valueOf(o.getEmpcd()))) // 비교: String 으로 변환 후 equals
            //.filter(o -> omgr.equals(o.getOmgr()))
            .collect(Collectors.toMap(
                o -> o.getOgubun() + "_" + o.getOno(),
                o -> o,
                (exist, replace) -> exist.getOstate() == null && replace.getOstate() != null ? replace : exist
            ))
            .values()
            .stream()
            .sorted((a, b) -> b.getOrdate().compareTo(a.getOrdate()))
            .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<OrdersDto> pageContent = filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    // 검색도 동일하게 empcd를 String 으로 처리
    public Page<OrdersDto> searchOrdersByLoginEmp(int page, int size,
            String ordate, String ostate, String supcd, String onote,
            String ogubun, String empcd, String omgr) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ordate").descending());

        Page<OrdersDto> rawPage = ordersRepository.searchOrders(
            ogubun,
            blankToNull(ordate),
            blankToNull(ostate),
            blankToNull(supcd),
            blankToNull(omgr),
            blankToNull(onote),
            Pageable.unpaged()
        );

        List<OrdersDto> filtered = rawPage.getContent().stream()
            .filter(o -> o.getEmpcd() != null && empcd.equals(String.valueOf(o.getEmpcd())))
            //.filter(o -> omgr.equals(o.getOmgr()))
            .collect(Collectors.toMap(
                o -> o.getOgubun() + "_" + o.getOno(),
                o -> o,
                (exist, replace) -> exist.getOstate() == null && replace.getOstate() != null ? replace : exist
            ))
            .values()
            .stream()
            .sorted((a, b) -> b.getOrdate().compareTo(a.getOrdate()))
            .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<OrdersDto> pageContent = filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

	/**
	 * 수림 끝
	 */
    	
    	
<<<<<<< HEAD
=======
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
>>>>>>> branch 'main' of https://github.com/yooooonjjin/erpProject.git
    	
	/**
	 * 수경 시작
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

<<<<<<< HEAD
            ordersRepository.save(order);
        }
    }
=======
    	    // 주문 상태 변경
    	    @Transactional
    	    public void updateOrderState(Integer ono) {
    	    	List<OrdersDto> orderList = ordersRepository.findByOno(ono);
    	        if (orderList == null || orderList.isEmpty()) {
    	            throw new RuntimeException("주문을 찾을 수 없습니다. ono: " + ono);
    	        }
>>>>>>> branch 'main' of https://github.com/yooooonjjin/erpProject.git

<<<<<<< HEAD
    public void save(OrdersDto dto) {
        // 복합키 생성
        OrdersId ordersId = new OrdersId(dto.getOno(), dto.getOgubun(), dto.getOcode());

        // 기존 주문 찾기
        Optional<OrdersDto> optional = ordersRepository.findById(ordersId);
        if (optional.isPresent()) {
            OrdersDto order = optional.get();
=======
    	        for (OrdersDto order : orderList) {
    	            order.setOstate("입고 완료");
    	            order.setOidate(new Timestamp(System.currentTimeMillis()));

    	            // 👇 조건 없이 무조건 최길동(1005)으로 담당자 변경
    	            order.setEmpcd(1005);

    	            ordersRepository.save(order);
    	        }
>>>>>>> branch 'main' of https://github.com/yooooonjjin/erpProject.git

<<<<<<< HEAD
            // 입고 수량 누적 저장
            order.setStiqty(dto.getStiqty());
=======
    	    }
>>>>>>> branch 'main' of https://github.com/yooooonjjin/erpProject.git

            // 공급가액 및 세액 저장
            order.setOsuprc(dto.getOsuprc());
            order.setOtax(dto.getOtax());

            // 입고 완료로 상태 변경
            order.setOstate("입고 완료");

            ordersRepository.save(order); // 저장
        }
	}

<<<<<<< HEAD
	/**
	 * 수경 끝
	 */
}
=======
    	            // 공급가액 및 세액 저장
    	            order.setOsuprc(dto.getOsuprc());
    	            order.setOtax(dto.getOtax());

    	            // 입고 완료로 상태 변경
    	            order.setOstate("입고 완료");

    	            ordersRepository.save(order); // 저장
    	        }
    		}

} 
>>>>>>> branch 'main' of https://github.com/yooooonjjin/erpProject.git

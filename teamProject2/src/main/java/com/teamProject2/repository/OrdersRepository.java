package com.teamProject2.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.entity.OrdersId;

public interface OrdersRepository extends JpaRepository<OrdersDto, OrdersId> {
    
	
	/**
	 * 윤진 시작
	 */
	
	// 발주 목록 ORD만 보여주기 (+ del 상태값 Y)
	@Query("SELECT o FROM OrdersDto o WHERE o.ogubun = :ogubun AND o.delst = 'Y'")
	List<OrdersDto> findByOgubun(@Param("ogubun") String ogubun);

	// 검색 상태 드롭다운
	@Query("SELECT DISTINCT o.ostate FROM OrdersDto o WHERE o.ogubun = :ogubun AND o.delst = 'Y'")
	List<String> findstateByOgubun(@Param("ogubun") String ogubun);

	// 상세보기 필터링 (OGUBUN, ONO, DELST)
	List<OrdersDto> findByOgubunAndOnoAndDelst(String ogubun,
											   Integer ono,
											   String delst);
	
	// 결재 대기 목록 (페이징 + 검색 필터 포함)
	@Query(value = " SELECT * 																"
	        	 + " FROM																	"
	        	 + "		ORDERS															"
	        	 + " WHERE																	"
	        	 + "		OGUBUN = :ogubun												"
	        	 + " AND																	"
	        	 + "		OSTATE = :ostate												"
	        	 + " AND																	"
	        	 + "		(:ordate IS NULL OR TRUNC(ORDATE) = TRUNC(:ordate))				"
	        	 + " AND																	"
	        	 + "		(:supnm IS NULL OR SUPCD IN (									"
	        	 + "		SELECT															"
	        	 + "				CCODE													"
	        	 + "		FROM															"
	        	 + "				CLIENT 													"
	        	 + "	    WHERE															"
	        	 + "				CGUBUN = 'SUP'											"
	        	 + "		AND																"
	        	 + "				CNAME LIKE '%' || :supnm || '%'))						"
	        	 + "		AND																"
	        	 + "				(:mgrnm IS NULL OR OMGR LIKE '%' || :mgrnm || '%')		"
	        	 + "		AND																"
	        	 + "				(:word IS NULL OR ONOTE LIKE '%' || :word || '%')		"
	        	 + " ORDER BY ONO DESC, OCODE",
	       countQuery = " SELECT COUNT(*)															"
	       			  + " FROM																		"
	       			  + "		ORDERS																"
	       			  + " WHERE																		"
	       			  + "		OGUBUN = :ogubun													"
	       			  + " AND																		"
	       			  + "		OSTATE = :ostate													"
	       			  + " AND																		"
	       			  + "		(:ordate IS NULL OR TRUNC(ORDATE) = TRUNC(:ordate))					"
	       			  + " AND																		"
	       			  + "		(:supnm IS NULL OR SUPCD IN (										"
	       			  + "	    	SELECT															"
	       			  + "					CCODE													"
	       			  + "			FROM															"
	       			  + "					CLIENT 													"
	       			  + "	        WHERE															"
	       			  + "					CGUBUN = 'SUP'											"
	       			  + "			AND																"
	       			  + "					CNAME LIKE '%' || :supnm || '%'))						"
	       			  + "			AND																"
	       			  + "					(:mgrnm IS NULL OR OMGR LIKE '%' || :mgrnm || '%')		"
	       			  + "			AND																"
	       			  + "					(:word IS NULL OR ONOTE LIKE '%' || :word || '%')",
	        nativeQuery = true)
	Page<OrdersDto> findPendingOrders(@Param("ogubun") String ogubun,
	                                  @Param("ordate") LocalDate ordate,
	                                  @Param("ostate") String ostate,
	                                  @Param("supnm") String supnm,
	                                  @Param("mgrnm") String mgrnm,
	                                  @Param("word") String word,
	                                  Pageable pageable);
	
	// 결재 대기 목록 건수
	@Query(value = " SELECT COUNT(*) 			"
				 + " FROM						"
				 + "		ORDERS 				"
				 + " WHERE						"
				 + "		OGUBUN = :ogubun	"
				 + " AND						"
				 + "		OSTATE = :ostate", nativeQuery = true)
	    Long countByOgubunAndOstate(@Param("ogubun") String ogubun,
	                                @Param("ostate") String ostate);
	
	
	// 결재 이력 목록 (승인 + 반려) – 페이징 + 검색 필터 포함
	@Query(value = " SELECT * 																	"
				 + " FROM																		"
				 + "		ORDERS																"
				 + " WHERE																		"
				 + "		OGUBUN = :ogubun													"
				 + " AND																		"
				 + "		OSTATE IN (:ostateList)												"
				 + " AND																		"
				 + "		(:ordate  IS NULL OR TRUNC(ORDATE) = TRUNC(:ordate))				"
				 + " AND																		"
				 + "		(:supnm   IS NULL OR SUPCD IN (										"
				 + "		SELECT																"
				 + "				CCODE														"
				 + "		FROM																"
				 + "				CLIENT														"
				 + "		WHERE																"
				 + "				CGUBUN = 'SUP'												"
				 + "		AND																	"
				 + "				CNAME LIKE '%' || :supnm || '%'))							"
				 + "		AND																	"
				 + "				(:mgrnm   IS NULL OR OMGR   LIKE '%' || :mgrnm || '%')		"
				 + "		AND																	"
				 + "				(:word    IS NULL OR ONOTE  LIKE '%' || :word  || '%')		"
				 + " ORDER BY"
				 + "		ONO DESC, OCODE",
	  countQuery = " SELECT COUNT(*)															" 	 
			  	 + " FROM																		"
			  	 + "		ORDERS																"
			  	 + " WHERE																		"
			  	 + "		OGUBUN = :ogubun													"	 
			  	 + " AND																		"
			  	 + "		OSTATE IN (:ostateList)												" 
			  	 + " AND																		"
			  	 + "		(:ordate  IS NULL OR TRUNC(ORDATE) = TRUNC(:ordate))				" 
			  	 + " AND																		"
			  	 + "		(:supnm   IS NULL OR SUPCD IN (										" 
			  	 + "		SELECT																"
			  	 + "				CCODE														" 
			  	 + "		FROM																"
			  	 + "				CLIENT														" 
			  	 + "		WHERE																"
			  	 + "				CGUBUN = 'SUP'												" 
			  	 + "		AND																	"
			  	 + "				CNAME LIKE '%' || :supnm || '%'))							" 
			  	 + "		AND																	"
			  	 + "				(:mgrnm   IS NULL OR OMGR   LIKE '%' || :mgrnm || '%')		" 
			  	 + "   		AND																	"
			  	 + "				(:word    IS NULL OR ONOTE  LIKE '%' || :word  || '%')",
	 nativeQuery = true)
	Page<OrdersDto> findHistoryOrders(@Param("ogubun")        String ogubun,
		        					  @Param("ordate")        LocalDate ordate,
		        					  @Param("ostateList")    List<String> ostateList,
		        					  @Param("supnm")         String supnm,
		        					  @Param("mgrnm")         String mgrnm,
		        					  @Param("word")          String word,
		        					  Pageable pageable);
	
	// 결재 이력 건수 (승인 + 반려)
	@Query(value = " SELECT COUNT(*)				"
		         + " FROM							"
		         + "		ORDERS					"	 
		         + " WHERE							"
		         + "		OGUBUN = :ogubun		" 
		         + " AND							"
		         + "		OSTATE IN (:ostateList)",
	 nativeQuery = true)
	Long countByOgubunAndOstateIn(@Param("ogubun")      String ogubun,
		        				  @Param("ostateList")  List<String> ostateList);
	
	// 내가 결재한 이력만 조회 (승인, 반려만) (페이징 + 검색 필터 포함)
	@Query(value = " SELECT *															"	 
	          	 + " FROM																"
	          	 + "		ORDERS o													" 
	          	 + " WHERE																"
	          	 + "		o.OGUBUN     = :ogubun										" 
	          	 + " AND																"
	          	 + "		o.OSTATE IN (:ostateList)									" 
	          	 + " AND																"
	          	 + "		o.cfmemp    = :cfmemp										" 
	          	 + " AND																"
	          	 + "		(:ordate IS NULL OR TRUNC(o.ORDATE) = TRUNC(:ordate))		" 
	          	 + " AND																"
	          	 + "		(:supnm  IS NULL OR o.SUPCD IN (							" 
	          	 + "		SELECT														"
	          	 + "				CCODE												"
	          	 + "		FROM														"
	          	 + "				CLIENT												"
	          	 + "		WHERE														"
	          	 + "				CGUBUN='SUP'										"
	          	 + "		AND															"
	          	 + "				CNAME LIKE '%'||:supnm||'%'))						" 
	          	 + " AND																"
	          	 + "		(:mgrnm  IS NULL OR o.OMGR   LIKE '%'||:mgrnm||'%')			" 
	          	 + " AND																"
	          	 + "		(:word   IS NULL OR o.ONOTE  LIKE '%'||:word||'%')			" 
	          	 + " ORDER BY															"
	          	 + "		o.ONO DESC, o.OCODE",
	  countQuery = " SELECT COUNT(*)													"	 
	          	 + " FROM																"
	          	 + "		ORDERS o													" 
	          	 + " WHERE																"
	          	 + "		o.OGUBUN     = :ogubun										" 
	          	 + " AND																"
	          	 + "		o.OSTATE IN (:ostateList)									" 
	          	 + " AND																"
	          	 + "		o.cfmemp    = :cfmemp										" 
	          	 + " AND																"
	          	 + "		(:ordate IS NULL OR TRUNC(o.ORDATE) = TRUNC(:ordate))		" 
	          	 + " AND																"
	          	 + "		(:supnm  IS NULL OR o.SUPCD IN (							" 
	          	 + "		SELECT														"
	          	 + "				CCODE												"
	          	 + "		FROM														"
	          	 + "				CLIENT												"
	          	 + "		WHERE														"
	          	 + "				CGUBUN='SUP'										"
	          	 + "		AND															"
	          	 + "				CNAME LIKE '%'||:supnm||'%'))						" 
	          	 + " AND																"
	          	 + "		(:mgrnm  IS NULL OR o.OMGR   LIKE '%'||:mgrnm||'%')			" 
	          	 + " AND																"
	          	 + "		(:word   IS NULL OR o.ONOTE  LIKE '%'||:word||'%')",nativeQuery = true)
	Page<OrdersDto> findMyHistoryOrders(@Param("ogubun")      String ogubun,
	        							@Param("ordate")      LocalDate ordate,
	        							@Param("ostateList")  List<String> ostateList,
	        							@Param("supnm")       String supnm,
	        							@Param("mgrnm")       String mgrnm,
	        							@Param("word")        String word,
	        							@Param("cfmemp")      Integer cfmemp,
	        							Pageable pageable);
	
	// 발주서 상세보기 (자재 구분 후 자재 정보 불러오기)
	@Query(value = " SELECT	o.ONO,                   						"
				 + "    	o.OCODE,                 						"
				 + "    	NVL(o.MATCD, NVL(o.FACCD, o.PRDCD)) AS ICODE,	"
				 + " CASE 													"
				 + "        WHEN o.MATCD IS NOT NULL THEN 'MAT' 			"
				 + "        WHEN o.FACCD IS NOT NULL THEN 'FAC' 			"
				 + "        WHEN o.PRDCD IS NOT NULL THEN 'PRD' 			"
				 + " END AS IGUBUN,											"
				 + "    	i.INAME,                 						"
				 + "    	i.IUNIT,                 						"
				 + "    	i.IUPRC,                 						"
				 + "    	o.OQTY,                  						"
				 + "    	o.SUPCD,                 						"
				 + "    	o.ODATE,										"
				 + "		o.ORDATE,                 						"
				 + "    	o.OIDATE,										"
				 + "		o.OSUPRC,										"
				 + "		o.OTAX,											"
				 + "		o.OATPRC,   									"
				 + "		o.OTPRC,										"
				 + "		o.OSTATE,    									"
				 + "    	o.ONOTE                  						"
				 + " FROM 													"
				 + "		ORDERS o 										"
				 + " JOIN 													"
				 + "		INVENTORY i 									"
				 + " ON i.ICODE = NVL(o.MATCD, NVL(o.FACCD, o.PRDCD)) 		"
				 + " AND 													"
				 + "		i.IGUBUN = CASE 								"
				 + "        	WHEN o.MATCD IS NOT NULL THEN 'MAT' 		"
				 + "            WHEN o.FACCD IS NOT NULL THEN 'FAC' 		"
				 + "            WHEN o.PRDCD IS NOT NULL THEN 'PRD' 		"
				 + " END 													"
				 + " WHERE 													"
				 + "		o.ONO = :ono									"
				 + " AND													"
				 + "		o.OGUBUN = 'ORD'								"
				 + " AND													"
				 + "    	o.DELST = 'Y'", nativeQuery = true)
    List<Map<String, Object>> findOrderDetailsByOno(@Param("ono") int ono);
	
	
	// 발주서 상세보기 (수신처 정보, 사원+부서)
	@Query(value = " SELECT	e.ENAME AS ENAME,								"
				 + "    	e.EPHONE AS EPHONE,						        "
				 + "    	d.DNAME AS DNAME          						"
				 + " FROM													"
				 + "    	ORDERS o										"
				 + " JOIN													"
				 + "    	EMP e ON o.EMPCD = e.ECODE						"
				 + " JOIN													"
				 + "    	DEPT d ON e.DPTCD = d.DCODE						"
				 + " WHERE													"
				 + "    	o.ONO = :ono									"
				 + " AND 													"
				 + "    	o.OGUBUN = 'ORD'								"
				 + " AND													"
				 + "    	o.DELST = 'Y'", nativeQuery = true)
	List<Map<String, Object>> findReceiverInfoByOno(@Param("ono") int ono);

	
	// 발주서 상세보기 (공급처)
	@Query(value = " SELECT	c.cname  AS cname, 								"
				 + "    	c.cowner AS cowner,								"
				 + "    	c.cphone AS cphone								"
				 + " FROM										 			"
				 + "    	client c										"
				 + " JOIN													"
				 + "    	orders o ON o.supcd = c.ccode					"
				 + " WHERE													"
				 + "    	o.ogubun = 'ORD'								"
				 + " AND													"
				 + "		o.ono = :ono									"
				 + " AND													"
				 + "		o.delst = 'Y'									"
				 + " AND													"
				 + "		c.cgubun = 'sup'", nativeQuery = true)
	List<Map<String, Object>> findSupplierInfoByOno(@Param("ono") int ono);
	
	// 발주 상태값 업데이트 + 결재자 정보 저장
	@Modifying
    @Transactional
    @Query(value = " UPDATE							"
    			 + "		ORDERS o				"	 
    			 + " SET							"
    			 + "		o.ostate   = :ostate,	"
    			 + "       	o.cfmemp   = :cfmemp,	" 
    			 + "       	o.cfmname  = :cfmname	" 
    			 + " WHERE							"
    			 + "		o.ono      = :ono		" 
    			 + " AND							"
    			 + "		o.ogubun   = 'ORD'		"	 
    			 + " AND							"
    			 + "		o.delst    = 'Y'", nativeQuery = true)
    void updateStateWithApprover(@Param("ono")     int ono,
                                 @Param("ostate")  String ostate,
                                 @Param("cfmemp")  Integer cfmemp,
                                 @Param("cfmname") String cfmname);

    OrdersDto findTopByOnoAndOgubunAndDelst(int ono, String ogubun, String delst);

    @Query(value = "SELECT ORD_OCODE_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    int getNextOcodeVal();

    List<OrdersDto> findByOnoAndOgubunAndDelst(int ono, String ogubun, String delst);
    

	/**
	 * 윤진 끝
	 */
	
	
	
	
	
	/**
	 * 수림 시작
	 */

	// ORD 시퀀스 값 하나 가져오기
	Page<OrdersDto> findByOgubunAndDelst(String ogubun, String delst, Pageable pageable);

	   
	// ono
	@Query(value = "SELECT ORD_ONO_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	Integer getNextOnoVal();  
	    
	// 발주서 삭제처리 ( delst = 'N'처리 )
	@Modifying
	@Query(value = "UPDATE orders SET delst = 'N' WHERE ono = :ono AND ogubun = :ogubun", nativeQuery = true)
	int updateDelst(@Param("ono") int ono, @Param("ogubun") String ogubun);

	    
	// 공급처명 가져오기
	@Query(value = " SELECT o.ono,								"
				 + "		c.cname								"
				 + " FROM										"
				 + "		OrdersDto o							"
				 + " LEFT JOIN									"
				 + "		ClientDto c ON o.supcd = c.ccode	"
				 + " AND										"
				 + "		c.cgubun = 'sup'					"
				 + " WHERE										"
				 + "		o.ogubun = 'ORD'					"
				 + " AND										"
				 + "		o.ono IN :onoList")
	List<Object[]> findSupplierNamesByOnoList(@Param("onoList") List<Integer> onoList);
	         
	         
	         
	@Query(value = " SELECT o.* "
				 + " FROM"
				 + "		ORDERS o\r\n"
				 + " LEFT JOIN"
				 + "		CLIENT c ON o.SUPCD = c.CCODE"
				 + " AND"
				 + "		c.CGUBUN = 'sup'\r\n"
				 + " WHERE"
				 + "		o.OGUBUN = :ogubun\r\n"
				 + " AND"
				 + "		NVL(o.DELST, 'Y') != 'N'\r\n"
				 + " AND"
				 + "		(:ordate IS NULL OR TO_CHAR(o.ORDATE,'YYYY-MM-DD') = :ordate)\r\n"
				 + " AND"
				 + "		(:ostate IS NULL OR o.OSTATE = :ostate)\r\n"
				 + " AND"
				 + "		(:supcd IS NULL OR UPPER(c.CNAME) LIKE UPPER('%'||:supcd||'%'))\r\n"
				 + " AND"
				 + "		(:omgr IS NULL OR UPPER(o.OMGR) LIKE UPPER('%'||:omgr||'%'))\r\n"
				 + " AND"
				 + "		(:onote IS NULL\r\n"
				 + "			OR (UPPER(o.ONOTE) LIKE UPPER('%'||:onote||'%')\r\n"
				 + "			OR UPPER(c.CNAME) LIKE UPPER('%'||:onote||'%')\r\n"
				 + "	    	OR UPPER(o.OMGR) LIKE UPPER('%'||:onote||'%')\r\n"
				 + "	    	OR TO_CHAR(o.ONO) LIKE '%'||:onote||'%'\r\n"
				 + "	    	OR UPPER(o.OGUBUN) LIKE UPPER('%'||:onote||'%')\r\n"
				 + "	    	OR (UPPER(o.OGUBUN || TO_CHAR(o.ONO)) LIKE UPPER('%'||:onote||'%'))\r\n"
				 + "	    )\r\n"
				 + "	)\r\n"
				 + " ORDER BY"
				 + "		o.ORDATE DESC, o.OGUBUN ASC, o.ONO ASC",
	  countQuery = " SELECT COUNT(*)\r\n"
	  			 + " FROM"
	  			 + "		ORDERS o\r\n"
	  			 + " LEFT JOIN"
	  			 + "		CLIENT c ON o.SUPCD = c.CCODE"
	  			 + " AND"
	  			 + "		c.CGUBUN = 'sup'\r\n"
	  			 + " WHERE"
	  			 + "		o.OGUBUN = :ogubun\r\n"
	  			 + " AND"
	  			 + "		NVL(o.DELST, 'Y') != 'N'\r\n"
	  			 + " AND"
	  			 + "		(:ordate IS NULL OR TO_CHAR(o.ORDATE,'YYYY-MM-DD') = :ordate)\r\n"
	  			 + " AND"
	  			 + "		(:ostate IS NULL OR o.OSTATE = :ostate)\r\n"
	  			 + " AND"
	  			 + "		(:supcd IS NULL OR UPPER(c.CNAME) LIKE UPPER('%'||:supcd||'%'))\r\n"
	  			 + " AND"
	  			 + "		(:omgr IS NULL OR UPPER(o.OMGR) LIKE UPPER('%'||:omgr||'%'))\r\n"
	  			 + " AND"
	  			 + "		(:onote IS NULL\r\n"
	  			 + "			OR (UPPER(o.ONOTE) LIKE UPPER('%'||:onote||'%')\r\n"
	  			 + "			OR UPPER(c.CNAME) LIKE UPPER('%'||:onote||'%')\r\n"
	  			 + "	        OR UPPER(o.OMGR) LIKE UPPER('%'||:onote||'%')\r\n"
	  			 + "	        OR TO_CHAR(o.ONO) LIKE '%'||:onote||'%'\r\n"
	  			 + "	        OR UPPER(o.OGUBUN) LIKE UPPER('%'||:onote||'%')\r\n"
	  			 + "	        OR (UPPER(o.OGUBUN || TO_CHAR(o.ONO)) LIKE UPPER('%'||:onote||'%'))\r\n"
	  			 + "	    )\r\n"
	  			 + " )",nativeQuery = true)
	Page<OrdersDto> searchOrders(@Param("ogubun") String ogubun,
					             @Param("ordate") String ordate,
					             @Param("ostate") String ostate,
					             @Param("supcd") String supcd,
					             @Param("omgr") String omgr,
					             @Param("onote") String onote,
					             Pageable pageable);

	      
	// detail 화면 띄우기용
	List<OrdersDto> findByOnoAndOgubun(Integer ono, String ogubun);
	       
	// 디테일 화면에 필요한 공급처,자재,사원 조인용
	@Query(value = " SELECT o.ONO,																			"
				 + "		o.OGUBUN,																		"
				 + "		o.OCODE,																		"
				 + "		o.OUPRC,																		"
				 + "		o.OQTY,																			"
				 + "		o.OTPRC,																		"
				 + "		o.OSTATE,																		"
				 + "	    o.ORDATE,																		"
				 + "		o.OUDATE,																		"
				 + "		o.OIDATE,																		"
				 + "		o.ODATE,																		"
				 + "		o.OMGR,																			"
				 + "		o.OWNM,																			"
				 + "	    o.SUPCD,																		"
				 + "		o.EMPCD,																		"
				 + "		o.MATCD,																		"
				 + "		o.PRDCD,																		"
				 + "		o.FACCD,																		"
				 + "		o.ONOTE, 																		"
				 + "	    o.OSUPRC,																		"
				 + "		o.OASUPRC,																		"
				 + "		o.OTAX,																			"
				 + "		o.OATAX,																		"
				 + "		o.OAQTY,																		"
				 + "		o.OATPRC,																		"
				 + "		o.DELST,																		"
				 + "	    c.CNAME,																		"
				 + "		c.COWNER,																		"
				 + "		c.CPHONE,																		"
				 + "	    e.ENAME,																		"
				 + "		e.EPHONE,																		"
				 + "	    d.DNAME,																		"
				 + "	    i.INAME,																		"
				 + "		i.IUNIT,																		"
				 + "	    i.ICODE,																		"
				 + "		i.IGUBUN																		"
				 + " FROM																					"
				 + "		ORDERS o																		"
				 + " LEFT JOIN																				"
				 + "		CLIENT c ON c.CGUBUN = 'sup'													"
				 + " AND																					"
				 + "		c.CCODE = o.SUPCD																"
				 + " LEFT JOIN																				"
				 + "		EMP e ON e.ECODE = o.EMPCD														"
				 + " LEFT JOIN																				"
				 + "		DEPT d ON d.DGUBUN = 'dpt'														"
				 + " AND																					"
				 + "		d.DCODE = e.DPTCD																"
				 + " LEFT JOIN																				"
				 + "		INVENTORY i 																	"
				 + " 		ON ( (o.MATCD IS NOT NULL AND i.IGUBUN = 'MAT' AND i.ICODE = o.MATCD)			"
				 + "	    	OR (o.FACCD IS NOT NULL AND i.IGUBUN = 'FAC' AND i.ICODE = o.FACCD)			"
				 + "	        OR (o.PRDCD IS NOT NULL AND i.IGUBUN = 'PRD' AND i.ICODE = o.PRDCD) )		"
				 + " WHERE																					"
				 + "		o.ONO = :ono																	"
				 + " AND																					"
				 + "		o.OGUBUN = :ogubun																"
				 + " ORDER BY o.OCODE", nativeQuery = true)
	List<Object[]> findOrdersRaw(@Param("ono") Integer ono, @Param("ogubun") String ogubun);
	            
	// 발주신청서 삭제 후 인서트 수정용
	@Modifying
	@Query(value = "DELETE FROM ORDERS WHERE ONO = :ono AND OGUBUN = :ogubun", nativeQuery = true)
	void deleteByOnoAndOgubun(@Param("ono") Integer ono, @Param("ogubun") String ogubun);

	
	/**
	 * 수림 끝
	 */
	
	
	
	/**
	 *  수경
	 */
	// 수신처 데이터
	@Query(value = " SELECT e.ename,						"
				 + " 		d.dname,						"
				 + "		e.ephone						"
				 + " FROM									"
				 + "		emp e							"
				 + " JOIN									"
				 + "		dept d ON e.dptcd = d.dcode		"
				 + " WHERE									"
				 + "		e.ecode = :empCode", nativeQuery = true)
	List<Object[]> findEmployeeWithDept(@Param("empCode") int empCode);
				
	// 입고 코드(ocode) 기준으로 한 줄만 보여주는 요약 데이터
	@Query(value = " SELECT DISTINCT																	"
				 + "		o.ono,																		"
				 + "		MIN(o.ostate) AS ostate,													"
				 + "		MIN(o.oqty) AS oqty,														"
				 + " CASE 																				"
				 + " 		WHEN MIN(o.matcd) IS NOT NULL THEN CONCAT('MAT', MIN(o.matcd))				"
				 + " 		WHEN MIN(o.prdcd) IS NOT NULL THEN CONCAT('PRD', MIN(o.prdcd))				"
				 + " 		WHEN MIN(o.faccd) IS NOT NULL THEN CONCAT('FAC', MIN(o.faccd))				"
				 + " ELSE 'Unknown'																		"
				 + "		END AS matCode,																"
				 + "		MIN(i.iname) AS iname,														"
				 + "		MIN(c.cname) AS cname,														"
				 + "		MIN(o.omgr) AS omgr,														"
				 + "		MIN(o.odate) AS odate														"
				 + " FROM																				"
				 + "		orders o																	"
				 + " LEFT JOIN																			"
				 + "		client c ON o.supcd = c.ccode												"
				 + " LEFT JOIN																			"
				 + "		inventory i ON (															"
				 + "					   (o.matcd = i.icode AND i.igubun = 'MAT') OR					"
				 + "					   (o.prdcd = i.icode AND i.igubun = 'PRD') OR					"
				 + "					   (o.faccd = i.icode AND i.igubun = 'FAC'))					"
				 + " WHERE																				"
				 + "		o.ogubun = 'STI'															"
				 + " AND (?1 IS NULL OR o.ostate LIKE '%' || ?1 || '%')					"
				 + " AND (?2 IS NULL OR LOWER(c.cname) LIKE LOWER('%' || ?2 || '%'))	"
				 + " AND (?3 IS NULL OR o.omgr LIKE '%' || ?3 || '%')					"
				 + " AND (?4 IS NULL OR TO_CHAR(o.odate, 'YYYY-MM-DD') = ?4)				"
				 + " GROUP BY																			"
				 + "		o.ono, o.supcd																"
				 + " ORDER BY																			"
				 + "		o.ono DESC",
      countQuery = " SELECT COUNT(*)																	"
      			 + " FROM (																				"
      			 + "		SELECT																		"
      			 + "				DISTINCT o.ono														"
      			 + "		FROM																		"
      			 + "				orders o															"
      			 + "		LEFT JOIN																	"
      			 + "				client c ON o.supcd = c.ccode										"
      			 + "		WHERE																		"
      			 + "				o.ogubun = 'STI'													"
      			 + "		AND (?1 IS NULL OR o.ostate LIKE '%' || ?1 || '%')							"
      			 + "		AND (?2 IS NULL OR LOWER(c.cname) LIKE LOWER('%' || ?2 || '%'))				"
      			 + "		AND (?3 IS NULL OR o.omgr LIKE '%' || ?3 || '%')							"
      			 + "		AND (?4 IS NULL OR TO_CHAR(o.odate, 'YYYY-MM-DD') = ?4)						"
      			 + " GROUP BY																			"
      			 + "		o.ono, o.supcd) sub",nativeQuery = true)
	Page<Object[]> findFilteredOrders(@Param("status") String status,
						    		 @Param("client") String client,
						    		 @Param("manager") String manager,
						    		 @Param("date") String date,
						    		 Pageable pageable);
				
				
	// 입고 명세서 데이터
	@Query(value = " SELECT o.ono,																									"
				 + "		o.ostate AS stateText,																					"
				 + "		o.oqty AS qtyWithUnit,																					"
				 + " CASE 																											"
				 + "		WHEN o.matcd IS NOT NULL THEN CONCAT('MAT', o.matcd)													"
				 + "		WHEN o.prdcd IS NOT NULL THEN CONCAT('PRD', o.prdcd)													"
				 + "		WHEN o.faccd IS NOT NULL THEN CONCAT('FAC', o.faccd)													"
				 + " ELSE 'Unknown'																									"
				 + "		END AS matCode,																							"
				 + "		i.iname AS matName,																						"
				 + "		o.ouprc AS price,																						"
				 + "		(SELECT cname FROM client WHERE ccode = o.supcd AND ROWNUM = 1) AS clientName,							"
				 + "		o.omgr AS manager,  																					"
				 + "		o.odate																									"
				 + " FROM 																											"
				 + "		orders o																								"
				 + " LEFT JOIN 																										"
				 + "		inventory i ON (																						"
				 + "					   (o.matcd = i.icode AND i.igubun = 'MAT') OR												"
				 + "					   (o.prdcd = i.icode AND i.igubun = 'PRD') OR												"
				 + "					   (o.faccd = i.icode AND i.igubun = 'FAC'))												"
				 + " WHERE 																											"
				 + "		o.ogubun = 'STI' 																						"
				 + " AND																											"
				 + "		o.ono = :ono																							"
				 + " AND (																											"
				 + "	 (o.matcd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.matcd AND igubun = 'MAT')) OR		"
				 + "	 (o.prdcd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.prdcd AND igubun = 'PRD')) OR		"
				 + "	 (o.faccd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.faccd AND igubun = 'FAC')))", nativeQuery = true)
	List<Object[]> findStockInData(@Param("ono") Integer ono);
				

	// 상태 '입고' 포함된 항목 조회
	@Query("SELECT o FROM OrdersDto o WHERE o.ostate = '입고'")
	List<OrdersDto> findOnlyIpgo();

	// 입고 명세서 상세
	@Query("SELECT o FROM OrdersDto o WHERE o.ono = :ono AND o.ogubun = 'STI'")
	List<OrdersDto> findByOno(@Param("ono") Integer ono);

	@Query("SELECT o FROM OrdersDto o WHERE o.ono = :ono")
	List<OrdersDto> findOrderDetailsByOcode(@Param("ono") Integer ono);
				
	@Query(value = " SELECT o.ono,																			"
				 + "		o.ostate AS stateText,															"
				 + "		o.oqty AS qtyWithUnit,															"
				 + " CASE 																					"
				 + "		WHEN o.matcd IS NOT NULL THEN CONCAT('MAT', o.matcd)							"
				 + "		WHEN o.prdcd IS NOT NULL THEN CONCAT('PRD', o.prdcd)							"
				 + "		WHEN o.faccd IS NOT NULL THEN CONCAT('FAC', o.faccd)							"
				 + " ELSE 'Unknown'																			"
				 + " 		END AS matCode,																	"
				 + "		i.iname AS matName,  															"
				 + "		(select cname from client where ccode = o.supcd and rownum = 1 ) AS clientName,	"
				 + "		o.omgr AS manager,  															"
				 + "		o.odate  																		"
				 + " FROM 																					"
				 + "		orders o																		"
				 + " LEFT JOIN 																				"
				 + "		inventory i ON (																"
				 + "		(o.matcd = i.icode AND i.igubun = 'MAT') OR										"
				 + "		(o.prdcd = i.icode AND i.igubun = 'PRD') OR										"
				 + "		(o.faccd = i.icode AND i.igubun = 'FAC'))										"
				 + " WHERE 																					"
				 + "		o.ogubun = 'STI' 																"
				 + " AND																					"
				 + "		o.ono = :ono", nativeQuery = true)
	List<Object[]> findStockInDetailsByOno(@Param("ono") Integer ono);
			    
			    
	// 입고 완료 상태로 업데이트
	@Modifying
	@Transactional
	@Query("UPDATE OrdersDto o SET o.ostate = '입고 완료' WHERE o.ono = :ono AND o.ogubun = 'STI'")
	int updateOrderState(@Param("ono") Integer ono);
			    		 
	@Query("SELECT o.oqty FROM OrdersDto o WHERE o.ono = :ordcd")
	Integer findQtyByOrdcd(@Param("ordcd") Integer ordcd);
	
			    
	/** 
	 * 수경 끝
	 */

	
	
	

}

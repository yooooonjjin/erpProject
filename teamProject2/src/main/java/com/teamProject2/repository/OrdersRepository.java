package com.teamProject2.repository;

import java.util.List;

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
	 * 수림 시작
	 */

	// ORD 시퀀스 값 하나 가져오기
	Page<OrdersDto> findByOgubunAndDelst(String ogubun, String delst, Pageable pageable);

	
	// ono
	 @Query(value = "SELECT ORD_ONO_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	   Integer getNextOnoVal();  
	 
	 // ocode
	 @Query(value = "SELECT ORD_OCODE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	 Integer getNextOcodeVal();

	 // 발주서 삭제처리 ( delst = 'N'처리 )
	 @Modifying
	 @Query(value = "UPDATE orders SET delst = 'N' WHERE ono = :ono AND ogubun = :ogubun", nativeQuery = true)
	 int updateDelst(@Param("ono") int ono, @Param("ogubun") String ogubun);

	 
	// 공급처명 가져오기
	 @Query("""
			    SELECT o.ono, c.cname
			    FROM OrdersDto o
			    LEFT JOIN ClientDto c ON o.supcd = c.ccode AND c.cgubun = 'sup'
			    WHERE o.ogubun = 'ORD' AND o.ono IN :onoList
			""")
			List<Object[]> findSupplierNamesByOnoList(@Param("onoList") List<Integer> onoList);
			
			
			
		@Query(value = """
			    SELECT o.*
			    FROM ORDERS o
			    LEFT JOIN CLIENT c ON o.SUPCD = c.CCODE AND c.CGUBUN = 'sup'
			    WHERE o.OGUBUN = :ogubun
			      AND (:ordate IS NULL OR TO_CHAR(o.ORDATE,'YYYY-MM-DD') = :ordate)
			      AND (:ostate IS NULL OR o.OSTATE = :ostate)
			      AND (:supcd IS NULL OR UPPER(c.CNAME) LIKE UPPER('%'||:supcd||'%'))
				  AND (:omgr IS NULL OR UPPER(o.OMGR) LIKE UPPER('%'||:omgr||'%'))
				  AND (:onote IS NULL
							    OR (
							        UPPER(o.ONOTE) LIKE UPPER('%'||:onote||'%')
							        OR UPPER(c.CNAME) LIKE UPPER('%'||:onote||'%')
							        OR UPPER(o.OMGR) LIKE UPPER('%'||:onote||'%')
							        OR TO_CHAR(o.ONO) LIKE '%'||:onote||'%'
							        OR UPPER(o.OGUBUN) LIKE UPPER('%'||:onote||'%')
							        OR (UPPER(o.OGUBUN || TO_CHAR(o.ONO)) LIKE UPPER('%'||:onote||'%'))
							    )
							)
			    ORDER BY o.ORDATE DESC, o.OGUBUN ASC, o.ONO ASC
			""",
			countQuery = """
			    SELECT COUNT(*)
			    FROM ORDERS o
			    LEFT JOIN CLIENT c ON o.SUPCD = c.CCODE AND c.CGUBUN = 'sup'
			    WHERE o.OGUBUN = :ogubun
			      AND (:ordate IS NULL OR TO_CHAR(o.ORDATE,'YYYY-MM-DD') = :ordate)
			      AND (:ostate IS NULL OR o.OSTATE = :ostate)
			      AND (:supcd IS NULL OR c.CNAME LIKE %:supcd%)
			      AND (:omgr IS NULL OR o.OMGR LIKE %:omgr%)
			      AND (:onote IS NULL
						    OR (
						        UPPER(o.ONOTE) LIKE UPPER('%'||:onote||'%')
						        OR UPPER(c.CNAME) LIKE UPPER('%'||:onote||'%')
						        OR UPPER(o.OMGR) LIKE UPPER('%'||:onote||'%')
						        OR TO_CHAR(o.ONO) LIKE '%'||:onote||'%'
						        OR UPPER(o.OGUBUN) LIKE UPPER('%'||:onote||'%')
							    OR (UPPER(o.OGUBUN || TO_CHAR(o.ONO)) LIKE UPPER('%'||:onote||'%'))
						    )
						)
			""",
			nativeQuery = true)
			Page<OrdersDto> searchOrders(
			    @Param("ogubun") String ogubun,
			    @Param("ordate") String ordate,
			    @Param("ostate") String ostate,
			    @Param("supcd") String supcd,
			    @Param("omgr") String omgr,
			    @Param("onote") String onote,
			    Pageable pageable
			);
		
		
		// detail 화면 띄우기용
		 List<OrdersDto> findByOnoAndOgubun(Integer ono, String ogubun);
		 
		 /**
		  * 디테일 화면에 필요한 공급처,자재,사원 조인용
		  */
		 @Query(value = """
				    SELECT 
				        o.ONO, o.OGUBUN, o.OCODE, o.OUPRC, o.OQTY, o.OTPRC, o.OSTATE, 
				        o.ORDATE, o.OUDATE, o.OIDATE, o.ODATE, o.OMGR, o.OWNM,
				        o.SUPCD, o.EMPCD, o.MATCD, o.PRDCD, o.FACCD, o.ONOTE, 
				        o.OSUPRC, o.OASUPRC, o.OTAX, o.OATAX, o.OAQTY, o.OATPRC, o.DELST,
				        c.CNAME, c.COWNER, c.CPHONE,
				        e.ENAME, e.EPHONE,
				        d.DNAME,
				        i.INAME, i.IUNIT,
		 				i.ICODE, i.IGUBUN
				    FROM ORDERS o
				    LEFT JOIN CLIENT c ON c.CGUBUN = 'sup' AND c.CCODE = o.SUPCD
				    LEFT JOIN EMP e ON e.ECODE = o.EMPCD
				    LEFT JOIN DEPT d ON d.DGUBUN = 'dpt' AND d.DCODE = e.DPTCD
				    LEFT JOIN INVENTORY i 
				        ON ( (o.MATCD IS NOT NULL AND i.IGUBUN = 'MAT' AND i.ICODE = o.MATCD)
				           OR (o.FACCD IS NOT NULL AND i.IGUBUN = 'FAC' AND i.ICODE = o.FACCD)
				           OR (o.PRDCD IS NOT NULL AND i.IGUBUN = 'PRD' AND i.ICODE = o.PRDCD) )
				    WHERE o.ONO = :ono AND o.OGUBUN = :ogubun
				    ORDER BY o.OCODE
				""", nativeQuery = true)
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
		@Query(value = """
			    SELECT e.ename, d.dname, e.ephone
			    FROM emp e
			    JOIN dept d ON e.dptcd = d.dcode
			    WHERE e.ecode = :empCode
			    """, nativeQuery = true)
			List<Object[]> findEmployeeWithDept(@Param("empCode") int empCode);
		
		// 입고 코드(ocode) 기준으로 한 줄만 보여주는 요약 데이터
			@Query(
				    value = """
				        SELECT DISTINCT
				            o.ono,
				            MIN(o.ostate) AS ostate,
				            MIN(o.oqty) AS oqty,
				            CASE 
				                WHEN MIN(o.matcd) IS NOT NULL THEN CONCAT('MAT', MIN(o.matcd))
				                WHEN MIN(o.prdcd) IS NOT NULL THEN CONCAT('PRD', MIN(o.prdcd))
				                WHEN MIN(o.faccd) IS NOT NULL THEN CONCAT('FAC', MIN(o.faccd))
				                ELSE 'Unknown'
				            END AS matCode,
				            MIN(i.iname) AS iname,
				            MIN(c.cname) AS cname,
				            MIN(o.omgr) AS omgr,
				            MIN(o.odate) AS odate
				        FROM
				            orders o
				        LEFT JOIN
				            client c ON o.supcd = c.ccode
				        LEFT JOIN
				            inventory i ON (
				                (o.matcd = i.icode AND i.igubun = 'MAT') OR
				                (o.prdcd = i.icode AND i.igubun = 'PRD') OR
				                (o.faccd = i.icode AND i.igubun = 'FAC')
				            )
				        WHERE
				            o.ogubun = 'STI'
				            AND (?1 IS NULL OR o.ostate LIKE '%' || ?1 || '%')
				            AND (?2 IS NULL OR LOWER(c.cname) LIKE LOWER('%' || ?2 || '%'))
				            AND (?3 IS NULL OR o.omgr LIKE '%' || ?3 || '%')
				            AND (?4 IS NULL OR TO_CHAR(o.odate, 'YYYY-MM-DD') = ?4)
				        GROUP BY
				            o.ono, o.supcd
				        ORDER BY
				            o.ono DESC
				        """,
				    countQuery = """
				        SELECT COUNT(*)
				        FROM (
				            SELECT DISTINCT o.ono
				            FROM orders o
				            LEFT JOIN client c ON o.supcd = c.ccode
				            WHERE
				                o.ogubun = 'STI'
				                AND (?1 IS NULL OR o.ostate LIKE '%' || ?1 || '%')
				                AND (?2 IS NULL OR LOWER(c.cname) LIKE LOWER('%' || ?2 || '%'))
				                AND (?3 IS NULL OR o.omgr LIKE '%' || ?3 || '%')
				                AND (?4 IS NULL OR TO_CHAR(o.odate, 'YYYY-MM-DD') = ?4)
				            GROUP BY o.ono, o.supcd
				        ) sub
				        """,
				    nativeQuery = true
				)
				Page<Object[]> findFilteredOrders(
				    @Param("status") String status,
				    @Param("client") String client,
				    @Param("manager") String manager,
				    @Param("date") String date,
				    Pageable pageable
				);
		
		
		// 입고 명세서 데이터
				@Query(value = """
					    SELECT 
					        o.ono,
					        o.ostate AS stateText,
					        o.oqty AS qtyWithUnit,
					        CASE 
					            WHEN o.matcd IS NOT NULL THEN CONCAT('MAT', o.matcd)
					            WHEN o.prdcd IS NOT NULL THEN CONCAT('PRD', o.prdcd)
					            WHEN o.faccd IS NOT NULL THEN CONCAT('FAC', o.faccd)
					            ELSE 'Unknown'
					        END AS matCode,
					        i.iname AS matName,
					        o.ouprc AS price,
					        (SELECT cname FROM client WHERE ccode = o.supcd AND ROWNUM = 1) AS clientName,
					        o.omgr AS manager,  
					        o.odate
					    FROM 
					        orders o
					    LEFT JOIN 
					        inventory i ON (
					            (o.matcd = i.icode AND i.igubun = 'MAT') OR
					            (o.prdcd = i.icode AND i.igubun = 'PRD') OR
					            (o.faccd = i.icode AND i.igubun = 'FAC')
					        )
					    WHERE 
					        o.ogubun = 'STI' 
					        AND o.ono = :ono
					        AND (
					            (o.matcd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.matcd AND igubun = 'MAT')) OR
					            (o.prdcd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.prdcd AND igubun = 'PRD')) OR
					            (o.faccd IS NOT NULL AND EXISTS (SELECT 1 FROM inventory WHERE icode = o.faccd AND igubun = 'FAC'))
					        )
					""", nativeQuery = true)
			List<Object[]> findStockInData(@Param("ono") Integer ono);
		

		// 상태 '입고' 포함된 항목 조회
		@Query("SELECT o FROM OrdersDto o WHERE o.ostate = '입고'")
		List<OrdersDto> findOnlyIpgo();

		// 입고 명세서 상세
		@Query("SELECT o FROM OrdersDto o WHERE o.ono = :ono AND o.ogubun = 'STI'")
		List<OrdersDto> findByOno(@Param("ono") Integer ono);

		@Query("SELECT o FROM OrdersDto o WHERE o.ono = :ono")
		List<OrdersDto> findOrderDetailsByOcode(@Param("ono") Integer ono);
		
		@Query(value = """
	            SELECT 
	                o.ono,
	                o.ostate AS stateText,
	                o.oqty AS qtyWithUnit,
	                CASE 
	                    WHEN o.matcd IS NOT NULL THEN CONCAT('MAT', o.matcd)
	                    WHEN o.prdcd IS NOT NULL THEN CONCAT('PRD', o.prdcd)
	                    WHEN o.faccd IS NOT NULL THEN CONCAT('FAC', o.faccd)
	                    ELSE 'Unknown'
	                END AS matCode,
	                i.iname AS matName,  
	             
	                (select cname from client where ccode = o.supcd and rownum = 1 ) AS clientName,
	                o.omgr AS manager,  
	                o.odate  
	            FROM 
	                orders o
	           
	            LEFT JOIN 
	                inventory i ON (
	                    (o.matcd = i.icode AND i.igubun = 'MAT') OR
	                    (o.prdcd = i.icode AND i.igubun = 'PRD') OR
	                    (o.faccd = i.icode AND i.igubun = 'FAC')
	                )
	            WHERE 
	                o.ogubun = 'STI' 
	                AND o.ono = :ono
	            """, nativeQuery = true)
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

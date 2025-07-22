package com.teamProject2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.ClientId;

public interface ClientRepository extends JpaRepository<ClientDto, ClientId> {
	
		/**
		 *  수림 시작
		 */
		// 공급처 자동완성에 필요한 sql
		List<ClientDto> findByCgubunAndCnameContaining(String cgubun, String cname);
	    ClientDto findByCgubunAndCcode(String cgubun, int ccode);
	    
	    List<ClientDto> findByCgubun(String cgubun);
	    /**
		 *  끝
		 */


	    /**
	     * 수경
	     */
		
	    ClientDto findTopByCgubunOrderByCcodeDesc(String cgubun);
		
		// cgubun = 'sup'인 공급처만 가져오기
	    //List<ClientDto> findByCgubun(String cgubun);
	    
		// 공급처 1개 가져오기 (코드로)
	    @Query("SELECT c FROM ClientDto c WHERE c.ccode = :code AND c.cgubun = 'sup'")
	    ClientDto findSupplierByCode(@Param("code") Integer supplierCode);

		ClientDto findByCgubunAndCcode(String string, Integer supcd);


		/** 
		 * 끝
		 */
}
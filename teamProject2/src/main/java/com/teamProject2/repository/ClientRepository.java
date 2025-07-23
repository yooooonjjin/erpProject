package com.teamProject2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.ClientId;

public interface ClientRepository extends JpaRepository<ClientDto, ClientId> {
	
	ClientDto findTopByCgubunOrderByCcodeDesc(String cgubun);
	
	
	/**
	 *  공급처 자동완성에 필요한 sql
	 */
	List<ClientDto> findByCgubunAndCnameContaining(String cgubun, String cname);
    ClientDto findByCgubunAndCcode(String cgubun, int ccode);
    
    List<ClientDto> findByCgubun(String cgubun);

	// 공급처 1개 가져오기 (코드로)
    @Query("SELECT c FROM ClientDto c WHERE c.cgubun = 'sup' AND c.ccode = :code")
    ClientDto findSupplierByCode(@Param("code") Integer code);

	ClientDto findByCgubunAndCcode(String string, Integer supcd);

}
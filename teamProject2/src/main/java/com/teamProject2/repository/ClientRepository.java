package com.teamProject2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.ClientId;

public interface ClientRepository extends JpaRepository<ClientDto, ClientId> {
	
	ClientDto findTopByCgubunOrderByCcodeDesc(String cgubun);
	
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
}
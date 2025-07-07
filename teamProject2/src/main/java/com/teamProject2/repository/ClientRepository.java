package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myerp.entity.ClientDto;
import com.myerp.entity.ClientId;

public interface ClientRepository extends JpaRepository<ClientDto, ClientId> {
	
	ClientDto findTopByCgubunOrderByCcodeDesc(String cgubun);
}
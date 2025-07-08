package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ClientDto;
import com.teamProject2.entity.ClientId;

public interface ClientRepository extends JpaRepository<ClientDto, ClientId> {
	
	ClientDto findTopByCgubunOrderByCcodeDesc(String cgubun);
}
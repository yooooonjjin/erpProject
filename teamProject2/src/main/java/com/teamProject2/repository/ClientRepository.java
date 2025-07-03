package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ClientDto;

public interface ClientRepository extends JpaRepository<ClientDto,Integer>{

}

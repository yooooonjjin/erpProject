package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.ClientDto;

public interface ClientRepository extends JpaRepository<ClientDto,Integer>{

}

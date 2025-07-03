package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.OrdersDto;

public interface OrdersRepository extends JpaRepository<OrdersDto,Integer>{

}

package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.OrdersDto;

public interface OrdersRepository extends JpaRepository<OrdersDto,Integer>{

}

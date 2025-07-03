package com.myerp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.myerp.entity.OrdersDto;
import com.myerp.repository.OrdersRepository;

@Service
public class OrdersService {

	public final OrdersRepository ordersRepository;
	
	public OrdersService(OrdersRepository ordersRepository) {
		this.ordersRepository = ordersRepository;
	}

	public List<OrdersDto> list() {
		
		List<OrdersDto> list = ordersRepository.findAll();
		
		return list;
	}
}

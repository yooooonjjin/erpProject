package com.teamProject2.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.repository.OrdersRepository;

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

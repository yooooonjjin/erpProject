package com.myerp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.myerp.entity.OrdersDto;
import com.myerp.service.OrdersService;

@Controller
@RequestMapping("/confirm")
public class ConfirmController {

	public final OrdersService ordersService;
	
	public ConfirmController(OrdersService ordersService) {
		this.ordersService = ordersService;
	}
	
	@GetMapping
	public ModelAndView list() {
		
		ModelAndView model = new ModelAndView();
		List<OrdersDto> list = ordersService.list();
		model.addObject("list",list);
		model.setViewName("confirm/confirmList");
		
		// System.out.println("불러온 개수: " + list.size());
		
		return model;
	}
	
	
	
}

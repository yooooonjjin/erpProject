package com.teamProject2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class InventoryOrderController {
	
//	public final InventoryService inventoryService;
//	public final ClientService clientService;
//	public InventoryOrderController(InventoryService inventoryService,ClientService clientService) {
//		this.inventoryService = inventoryService;
//		this.clientService = clientService;
//	}
//	
//	@GetMapping
//	public ModelAndView list(@RequestParam(defaultValue = "1") int indexpage) {
//		
//		ModelAndView model = new ModelAndView();
//		
//		// 자재관련
//		Long total = inventoryService.count();	
//		
//		Page<InventoryDto> page = inventoryService.list(indexpage-1,10);
//		
//		// 예) 현재 페이지 번호 2번인 경우 : (총데이터개수 - (현재페이지번호-1)*출력단위)
//		int startPageRownum = (int)(page.getTotalElements() -page.getNumber()*10);
//		
//		model.addObject("matList", page.getContent()); // getContent 가 list 출력매소드
//		model.addObject("currentPage", page.getNumber());	// 현재페이지 번호
//		model.addObject("ptotal", page.getTotalElements()); // 전체 데이터 갯수
//		model.addObject("ptotalPage", page.getTotalPages()); // 페이지 갯수		
//		model.addObject("startPageRownum",startPageRownum);
//		
//		// 공급처관련
//		List<ClientDto> supList = clientService.supList();
//		
//		model.addObject("supList",supList);
//		
//		
//		model.setViewName("/order/inventoryList");
//		
//		
//		return model;
//	}
}

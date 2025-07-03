package com.myerp.service;

import org.springframework.stereotype.Service;

import com.myerp.repository.InventoryRepository;

@Service
public class InventoryService {

	public final InventoryRepository inventoryRepository;
	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}
	
}

package com.teamProject2.service;

import org.springframework.stereotype.Service;

import com.teamProject2.repository.InventoryRepository;

@Service
public class InventoryService {

	public final InventoryRepository inventoryRepository;
	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}
	
}

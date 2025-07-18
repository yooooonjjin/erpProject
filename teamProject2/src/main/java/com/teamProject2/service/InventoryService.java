package com.teamProject2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.entity.OrdersDto;
import com.teamProject2.repository.InventoryRepository;

@Service
public class InventoryService {

	public final InventoryRepository inventoryRepository;
	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	// getMaterialName 메소드 호출
    public String getMaterialName(int matcd) {
        // matcd에 해당하는 자재 정보를 가져옵니다
        Optional<InventoryDto> inventoryOptional = inventoryRepository.findByMatcd(matcd);

        // 자재명이 있으면 반환, 없으면 "Unknown"
        return inventoryOptional.map(InventoryDto::getIname).orElse("Unknown");
    }
}

package com.teamProject2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.repository.InventoryRepository;

@Service
public class InventoryService {

	public final InventoryRepository inventoryRepository;
	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	// getMaterialName 메소드 호출
	public String getMaterialName(int matcd) {
        List<InventoryDto> list = inventoryRepository.findByMatcd(matcd);
        return list.stream()
                   .findFirst()
                   .map(InventoryDto::getIname)
                   .orElse("알수없음");
    }
}

package com.teamProject2.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.repository.InventoryRepository;

@Service
public class InventoryService {
	
	public final InventoryRepository inventoryRepository;	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}
	
	
//	/**
//	 * 자재 리스트
//	 */
//	public List<InventoryDto> matList(){
//		return inventoryRepository.findAll();
//	}
	
	/**
	 * 자재 목록 출력(페이징)
	 */
	public Page<InventoryDto> list(int page, int size){ 
		// PageRequest.of(현재 페이지 번호(출력 페이지 번호), 화면에 보여질 갯수)
		// Pageable : 페이징 처리를 위한 스프링에서 제공하는 인터페이스,페이징처리 조건을 담는
		Pageable pageable = PageRequest.of(page, size,Sort.by("invcode").descending()); // of 페이징처리를 위한 클래스
		return inventoryRepository.findAll(pageable);
	}
	
	/**
	 * 총 데이터 개수
	 */
	public Long count() {
		return inventoryRepository.count();
	}
	

}

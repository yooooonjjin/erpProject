package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.entity.InventoryId;


public interface InventoryRepository extends JpaRepository<InventoryDto, InventoryId> {
    
    // 카테고리별로 가장 큰 코드 조회
    InventoryDto findTopByIgubunOrderByIccodeDesc(String igubun);
}

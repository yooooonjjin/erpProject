package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myerp.entity.InventoryDto;
import com.myerp.entity.InventoryId;

public interface InventoryRepository extends JpaRepository<InventoryDto, InventoryId> {
    
    // 카테고리별로 가장 큰 코드 조회
    InventoryDto findTopByIgubunOrderByIccodeDesc(String igubun);
}

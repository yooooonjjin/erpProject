package com.teamProject2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.entity.InventoryId;


public interface InventoryRepository extends JpaRepository<InventoryDto, InventoryId> {
    
    // 카테고리별로 가장 큰 코드 조회
    InventoryDto findTopByIgubunOrderByIcodeDesc(String igubun);
    
    // InventoryRepository에서 matcd에 해당하는 icode 값을 기준으로 조회
    @Query("SELECT i FROM InventoryDto i WHERE i.icode = :matcd")
    List<InventoryDto> findByMatcd(int matcd);
    
    InventoryDto findByIcodeAndIgubun(Integer matcd, String string);
}

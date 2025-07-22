package com.teamProject2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.teamProject2.entity.InventoryDto;
import com.teamProject2.entity.InventoryId;


public interface InventoryRepository extends JpaRepository<InventoryDto, InventoryId> {
    
	// 카테고리별로 가장 큰 코드 조회
    com.teamProject2.entity.InventoryDto findTopByIgubunOrderByIcodeDesc(String igubun);

    // InventoryRepository에서 matcd에 해당하는 icode 값을 기준으로 조회
    @Query("SELECT i FROM InventoryDto i WHERE i.icode = :matcd")
    // matcd를 기준으로 단일 자재를 반환하는 메소드
    Optional<InventoryDto> findByMatcd(int matcd);
    
    InventoryDto findByIcodeAndIgubun(Integer matcd, String string);

}

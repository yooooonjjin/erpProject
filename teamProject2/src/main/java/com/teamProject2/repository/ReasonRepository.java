package com.teamProject2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ReasonDto;
import com.teamProject2.entity.ReasonId;

public interface ReasonRepository extends JpaRepository<ReasonDto, ReasonId> {

	// 기본 CRUD 메서드 제공
	Optional<ReasonDto> findBySticd(int sticd);  // sticd가 OrdersDto의 ocode와 연결됨
}

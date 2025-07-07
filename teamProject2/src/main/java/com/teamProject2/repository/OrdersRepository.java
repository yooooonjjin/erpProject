package com.teamProject2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.entity.OrdersId;

public interface OrdersRepository extends JpaRepository<OrdersDto, OrdersId> {
    // 필요한 커스텀 쿼리 추가 가능
	Page<OrdersDto> findByOgubun(String ogubun, Pageable pageable);
}

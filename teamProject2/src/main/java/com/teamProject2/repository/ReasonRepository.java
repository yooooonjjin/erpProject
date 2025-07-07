package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ReasonDto;
import com.teamProject2.entity.ReasonId;

public interface ReasonRepository extends JpaRepository<ReasonDto, ReasonId> {
    // 기본 CRUD 메서드 제공
}

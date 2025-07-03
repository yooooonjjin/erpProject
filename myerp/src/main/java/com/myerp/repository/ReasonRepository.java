package com.myerp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myerp.entity.ReasonDto;

public interface ReasonRepository extends JpaRepository<ReasonDto, Integer> {

}

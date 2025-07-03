package com.teamProject2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.ReasonDto;

public interface ReasonRepository extends JpaRepository<ReasonDto, Integer> {

}

package com.teamProject2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.DeptDto;
import com.teamProject2.entity.DeptId;

public interface DeptRepository extends JpaRepository<DeptDto, DeptId> {

	/**
	 * 부서 조회
	 */
	Optional<DeptDto> findByDgubunAndDcode(String dgubun, int dcode);
}

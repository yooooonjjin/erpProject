package com.teamProject2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teamProject2.entity.EmpDto;

public interface EmpRepository extends JpaRepository<EmpDto, Integer> {

	/**
	 * 사원번호와 이름으로 로그인 인증
	 */
	Optional<EmpDto> findByEcodeAndEname(int ecode, String ename);
}

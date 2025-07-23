package com.teamProject2.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamProject2.entity.ReasonDto;
import com.teamProject2.repository.ReasonRepository;

import jakarta.persistence.OptimisticLockException;


@Service
public class ReasonService {

	public final ReasonRepository reasonRepository;
	
	public ReasonService(ReasonRepository reasonRepository) {
		this.reasonRepository = reasonRepository;
	}

	// 불용 사유 저장 메서드
	public void save(ReasonDto reasonDto) {
	    try {
	        reasonRepository.save(reasonDto);
	    } catch (OptimisticLockException e) {
	        // 예외 처리: 해당 데이터가 다른 트랜잭션에서 수정되었을 경우
	        throw new RuntimeException("이 데이터는 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해 주세요.");
	    }
    }

	// 불용 사유를 ocode로 조회하는 메서드
	public Optional<ReasonDto> getReasonByOrderCode(int ocode) {
	    return reasonRepository.findBySticd(ocode);
	}
	

	
}

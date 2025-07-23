package com.teamProject2.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamProject2.entity.OrdersDto;
import com.teamProject2.entity.ReasonDto;
import com.teamProject2.repository.OrdersRepository;
import com.teamProject2.repository.ReasonRepository;

@Service
public class ConfirmService {

	private final OrdersRepository ordersRepository;	// 상태값 업데이트용
	public final ReasonRepository reasonRepository;		// 사유 저장용
	
	public ConfirmService(OrdersRepository ordersRepository,
						  ReasonRepository reasonRepository) {
		this.reasonRepository = reasonRepository;
		this.ordersRepository = ordersRepository;
	}
	
	/**
	 * 결재 승인 처리
	 */
	@Transactional
	public void approve(int ono,
						String rnote,
						String rdetail,
						Integer loginEmpCd,
                        String loginEname) {
	
		// (1) ORDERS 테이블에 상태값 (결재 대기 -> 결재 승인) 변경 + 결재자 정보 업데이트
		ordersRepository.updateStateWithApprover(ono, "결재 승인", loginEmpCd, loginEname);
		
		// (2) REASON 테이블에 승인 사유 저장
		ReasonDto dto = ReasonDto.builder()
				.rgubun("CFM")										// 구분값 -> 결재 -> CFM
				.rcode(ono)											// 발주번호와 동일
				.rname("승인")										// 결재 이름
				.rdetail(rdetail)									// 사유 선택	
				.rnote(rnote)										// 상세 사유 입력
				.rstate("Y")										// 상태값 defalut = Y
				.rrdate(new Timestamp(System.currentTimeMillis()))	// 현재 시간
				.ordcd(ono)                          				// 발주 코드 연동
                .build();
		
		// (3) 기존 ORDERS 데이터에서 발주건 하나 조회
		List<OrdersDto> ordList = ordersRepository.findByOnoAndOgubunAndDelst(ono, "ORD", "Y");

		
		// (4) 입고 STI 행 추가
		for (OrdersDto ord : ordList) {
            OrdersDto sti = OrdersDto.builder()
                .ono(ord.getOno())
                .ogubun("STI")
                .ocode(ordersRepository.getNextOcodeVal())
                .oqty(ord.getOqty())
                .ouprc(ord.getOuprc())
                .oatprc(ord.getOatprc())
                .otprc(ord.getOtprc())
                .osuprc(ord.getOsuprc())
                .otax(ord.getOtax())
                .ordate(new Timestamp(System.currentTimeMillis()))
                .odate(ord.getOdate())
                .omgr(ord.getOmgr())
                .ownm(ord.getOwnm())
                .supcd(ord.getSupcd())
                .empcd(ord.getEmpcd())
                .matcd(ord.getMatcd())
                .prdcd(ord.getPrdcd())
                .faccd(ord.getFaccd())
                .onote(ord.getOnote())
                .ostate("입고 대기")
                .delst("Y")
                .cfmemp(loginEmpCd)
                .cfmname(loginEname)
                .build();
            ordersRepository.save(sti);
        }
    }
	
	
	/**
	 * 결재 반려 처리
	 */
	@Transactional
	public void reject(int ono,
					   String rnote,
					   String rdetail,
					   Integer loginEmpCd,
                       String loginEname) {
		
		// (1) ORDERS 테이블에 상태값 (결재 대기 -> 결재 반려) 변경 + 결재자 정보 업데이트
		ordersRepository.updateStateWithApprover(ono, "결재 반려", loginEmpCd, loginEname);
		
		// (2) REASON 테이블에 승인 사유 저장
		ReasonDto dto = ReasonDto.builder()
				.rgubun("CFM")										// 구분값 -> 결재 -> CFM
				.rcode(ono)											// 발주번호와 동일
				.rname("반려")										// 결재 이름
				.rdetail(rdetail)									// 사유 선택	
				.rnote(rnote)										// 상세 사유 입력
				.rstate("Y")										// 상태값 defalut = Y
				.rrdate(new Timestamp(System.currentTimeMillis()))	// 현재 시간
				.ordcd(ono)                          				// 발주 코드 연동
                .build();
		
		reasonRepository.save(dto);
	}

	
}

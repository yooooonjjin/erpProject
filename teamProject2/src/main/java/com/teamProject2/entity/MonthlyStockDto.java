package com.teamProject2.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="MONTHLYSTOCK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStockDto {
	

	@Id  // 기본키 설정
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@SequenceGenerator(name="monthlyStock_seq", sequenceName = "MONTHLYSTOCK_SEQ", allocationSize = 1, initialValue = 1001)
	Integer mlyId;
	
	@Column(nullable=false)
	Integer matid;		// 자재 ID
	
	@Column(nullable=false, length=50)
	String yearMonth;		// 재고 기준 년월
	
	@Column(nullable=false, length=20)
	String startQty;		// 이월 수량
	
	@Column(nullable=false, length=20)
	String inQty;		// 입고 수량
	
	@Column(nullable=false, length=20)
	String outQty;	// 출고 수량
	
	@Column(nullable=false, length=20)
	String endQty;	// 잔여 수량
		
	@Column(nullable=false, length=100)
	String note;		// 특이사항
	
	@Column(nullable=false)
	Timestamp rdate;	// 등록일
	
	@Column(nullable=false)
	Timestamp udate;	// 수정일
		
	}


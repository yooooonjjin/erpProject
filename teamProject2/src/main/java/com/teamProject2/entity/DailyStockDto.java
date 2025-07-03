package com.teamProject2.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="DAILYSTOCK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStockDto {
	
	@Id  // 기본키 설정
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;
	
	@Column(nullable=false)
	Integer matid;		// 자재 ID
	
	@Column(nullable=false, length=20)
	String stkDate;		// 재고 일자
	
	@Column(nullable=false, length=20)
	String startQty;		// 전월 이월 수량
	
	@Column(nullable=false, length=20)
	String inQty;		// 당일 입고 수량
	
	@Column(nullable=false, length=20)
	String outQty;	// 당일 출고 수량
	
	@Column(nullable=false, length=20)
	String endQty;	// 당일 최종 수량
		
	@Column(nullable=false, length=100)
	String note;		// 특이사항
	
	@Column(nullable=false)
	Timestamp rdate;	// 등록일
	
	@Column(nullable=false)
	Timestamp udate;	// 수정일
	
	// stockout -> reason
	// monthly -> material
} 

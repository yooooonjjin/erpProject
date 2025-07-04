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
@Table(name="REASON")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReasonDto {
	

	@Id  // 기본키 설정
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@SequenceGenerator(name="reason_seq", sequenceName = "REASON_SEQ", allocationSize = 1, initialValue = 1001)
	Integer rsnCode;
	
	@Column(nullable=false, length=3)
	String cate;		// 구분
	
	@Column(nullable=false, length=50)
	String name;		// 사유명
	
	@Column(nullable=false, length=100)
	String detail;		// 상세 설명
	
	@Column(nullable=false, length=2)
	String state;		// 사용 여부
	
	@Column(nullable=false)
	Timestamp rdate;	// 등록일
	
	@Column
	Timestamp udate;	// 수정일
	

}

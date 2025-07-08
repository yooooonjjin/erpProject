package com.teamProject2.entity;

import java.sql.Date;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EMP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpDto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq_gen")
    @SequenceGenerator(name = "emp_seq_gen", sequenceName = "EMP_SEQ", allocationSize = 1, initialValue = 1001)
    @Column(name = "ECODE")
    private int ecode;  // 사원번호 (PK)

    @Column(name = "ENAME", length = 10, nullable = false)
    private String ename;  // 사원명

    @Column(name = "EEMAIL", length = 50, nullable = false)
    private String eemail;  // 이메일

    @Column(name = "EPHONE", length = 20, nullable = false)
    private String ephone;  // 연락처

    @Column(name = "EBIRTH", nullable = false)
    private Date ebirth;  // 생년월일 

    @CreationTimestamp
    @Column(name = "ERDATE")
    private Timestamp erdate;  // 등록일

    @UpdateTimestamp
    @Column(name = "EUDATE")
    private Timestamp eudate;  // 수정일

    @Column(name = "DPTCD", nullable = false)
    private int dptcd;  // 부서 코드 (FK)

    @Column(name = "POSCD", nullable = false)
    private int poscd;  // 직급 코드 (FK)
}

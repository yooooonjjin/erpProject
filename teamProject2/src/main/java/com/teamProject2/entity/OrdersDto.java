package com.teamProject2.entity;

import java.sql.Timestamp;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ORDERS")
@IdClass(OrdersId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDto {

    @Id
    @Column(name = "ONO")
    private Integer ono;  // 발주/입고 번호

    @Id
    @Column(name = "OGUBUN", length = 3)
    private String ogubun;  // 구분 (ORD, STI)

    @Id
    @Column(name = "OCODE")
    private Integer ocode;  // 발주서/입고서 행번호

    @Column(name = "OUPRC", nullable = false)
    private Integer ouprc;  // 단가

    @Column(name = "OQTY", nullable = false)
    private Integer oqty;   // 수량

    @Column(name = "OTPRC", nullable = false)
    private Integer otprc;  // 총 금액

    @Column(name = "OSTATE", length = 20)
    private String ostate;  // 상태

    @CreationTimestamp
    @Column(name = "ORDATE")
    private Timestamp ordate;  // 발주일자

    @UpdateTimestamp
    @Column(name = "OUDATE")
    private Timestamp oudate;  // 수정일

    @Column(name = "OIDATE")
    private Timestamp oidate;  // 입고처리일

    @Column(name = "ODATE")
    private Timestamp odate;  // 발주납기일

    @Column(name = "OMGR", length = 20)
    private String omgr;  // 담당자

    @Column(name = "OWNM", length = 20)
    private String ownm;  // 창고명

    @Column(name = "SUPCD")
    private Integer supcd;  // 공급처코드 (FK)

    @Column(name = "EMPCD")
    private Integer empcd;  // 사원번호 (FK)

    @Column(name = "MATCD")
    private Integer matcd;  // 원재료코드 (FK)

    @Column(name = "PRDCD")
    private Integer prdcd;  // 완제품코드 (FK)

    @Column(name = "FACCD")
    private Integer faccd;  // 설비품 코드 (FK)
}

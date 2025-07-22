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
    private Integer otprc;  // 한 줄 당 총 금액

    @Column(name = "OSTATE", length = 20)
    private String ostate;  // 상태

    @Column(name = "ORDATE")
    private Timestamp ordate;  // 발주일자

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
    
    @Column(length = 100,name = "ONOTE")
    private String onote;  
    
    @Column(name = "STIQTY")
    private String stiqty;	// 입고 수량
    
    @Column(name = "OSUPRC")
    private Integer osuprc;  // 한 줄 당 공급가액
    
    @Column(name = "OASUPRC")
    private Integer oasuprc;  // 통 발주서 공급가액
    
    @Column(name = "OTAX")
    private Integer otax;  // 한 줄 당 세액
    
    @Column(name = "OATAX")
    private Integer oatax;  // 통 발주서 세액
    
    @Column(name = "OAQTY")
    private Integer oaqty;  // 통 발주서 수량 합계
    
    @Column(name = "OATPRC")
    private Integer oatprc;  // 통 발주서 총금액합계
    
    @Column(name = "DELST", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String delst;	// 삭제 상태

    @PrePersist
    public void prePersist() {
        if (delst == null) {
            delst = "Y";
        }
    }
    
    /**
     * 디테일 화면에 필요한 공급처,자재,사원 조인용
     */
    @Transient
    private String cname;   // 공급처명

    @Transient
    private String cowner;  // 대표자

    @Transient
    private String cphone;  // 공급처 연락처

    @Transient
    private String ename;   // 담당자명

    @Transient
    private String ephone;  // 담당자 연락처

    @Transient
    private String dname;   // 부서명

    @Transient
    private String iname;   // 자재명

    @Transient
    private String iunit;   // 단위
    
    @Transient
    private String igubun;   // 자재구분 (MAT, PRD, FAC 등)

    @Transient
    private Integer icode;    // 자재코드

    
}

package com.teamProject2.entity;

import java.sql.Timestamp;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "REASON")
@IdClass(ReasonId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReasonDto {

    @Id
    @Column(name = "RGUBUN", length = 3, nullable = false)
    private String rgubun;  // 구분 (cfm / str)

    @Id
    @Column(name = "RCODE", nullable = false)
    private int rcode;      // 코드 (시퀀스 자동생성)

    @Column(name = "RNAME", length = 10, nullable = false)
    private String rname;   // 이름

    @Column(name = "RDETAIL", length = 100)
    private String rdetail; // 사유 상세

    @Column(name = "RNOTE", length = 100)
    private String rnote;   // 사유 입력

    @Column(name = "RSTATE", length = 20)
    private String rstate;  // 상태

    @CreationTimestamp
    @Column(name = "RRDATE", updatable = false)
    private Timestamp rrdate;  // 등록일

    @UpdateTimestamp
    @Column(name = "RUDATE")
    private Timestamp rudate;  // 수정일

    @Column(name = "ORDCD")
    private Integer ordcd;    // 발주코드 (외래키, nullable)

    @Column(name = "STICD")
    private Integer sticd;    // 입고코드 (외래키, nullable)
}

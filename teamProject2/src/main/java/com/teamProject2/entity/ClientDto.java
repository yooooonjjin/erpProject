package com.teamProject2.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "CLIENT")
@IdClass(ClientId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {

    @Id
    @Column(length = 3)
    private String cgubun;  // 구분 (clt / sup)

    @Id
    @Column
    private int ccode;      // 코드

    @Column(length = 50, nullable = false)
    private String cname;   // 이름

    @Column(length = 100, nullable = false)
    private String caddr;   // 주소

    @Column(length = 20, nullable = false)
    private String cphone;  // 연락처

    @Column(length = 50, nullable = false)
    private String cemail;  // 이메일

    @Column(length = 10, nullable = false)
    private String cowner;  // 대표자

    @CreationTimestamp
    private Timestamp crdate; // 등록일

    @UpdateTimestamp
    private Timestamp cudate; // 수정일
}

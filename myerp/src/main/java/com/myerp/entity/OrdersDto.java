package com.myerp.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name="ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersDto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq_gen")
    @SequenceGenerator(name="orders_seq", sequenceName = "ORDERS_SEQ", allocationSize = 1, initialValue = 1001)
    private int code;

    @Column(name="CATE", length=10, nullable=false)
    private String cate;

    @Column(name="QTY", nullable=false)
    private int qty;

    @Column(name="UPRC", nullable=false)
    private int uprc;

    @Column(name="TPRC", nullable=false)
    private int tprc;

    @Column(name="STATE", length=20)
    private String state;

    @CreationTimestamp
    @Column(name="RDATE")
    private Timestamp rdate;

    @UpdateTimestamp
    @Column(name="UDATE")
    private Timestamp udate;

    @Column(name="IDATE")
    private Timestamp idate; // 입고처리일

    @Column(name="ODATE")
    private Timestamp odate; // 발주납기일

    @Column(name="NOTE", length=100)
    private String note;

    @Column(name="MGR", length=10)
    private String mgr;

    @Column(name="WNM", length=20)
    private String wnm; // 창고명
}

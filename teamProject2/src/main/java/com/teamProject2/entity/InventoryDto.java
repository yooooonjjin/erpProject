package com.teamProject2.entity;

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
@Table(name="INVENTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_seq_gen")
    @SequenceGenerator(name="inventory_seq_gen", sequenceName = "INVENTORY_SEQ", allocationSize = 1, initialValue = 1001)
    private int code;

    @Column(length=10, nullable=false)
    private String cate;

    @Column(length=50, nullable=false)
    private String name;

    @Column(length=20)
    private String unit;

    @Column(nullable=false)
    private int uprc;

    @Column(nullable=false)
    private int qty;

    @CreationTimestamp
    private Timestamp rdate;

    @UpdateTimestamp
    private Timestamp udate;

    @Column(length=100)
    private String note;

    @Column(length=100)
    private String img;  // 이미지 파일명 등
}

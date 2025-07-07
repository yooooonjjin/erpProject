package com.teamProject2.entity;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "INVENTORY")
@IdClass(InventoryId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto {

    @Id
    @Column(length = 3)
    private String igubun;

    @Id
    @Column
    private int iccode;

    @Column(length = 10, nullable = false)
    private String iname;

    @Column(length = 10, nullable = false)
    private String iunit;

    @Column(nullable = false)
    private int iuprc;

    @Column(nullable = false)
    private int iqty;

    @CreationTimestamp
    private Timestamp irdate;

    @UpdateTimestamp
    private Timestamp iudate;

    @Column(length = 100)
    private String inote;

    @Column(length = 100)
    private String iimg;
}

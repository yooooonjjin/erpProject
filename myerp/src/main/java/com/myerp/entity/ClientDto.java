package com.myerp.entity;

import java.security.Timestamp;

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
@Table(name="CLIENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_seq_gen")
    @SequenceGenerator(name="client_seq_gen", sequenceName = "CLIENT_SEQ", allocationSize = 1, initialValue = 1001)
	private int code;
	
	@Column(length=10,nullable=false)
	private String cate;
	
	@Column(length=50,nullable=false)
	private String name;
	
	@Column(length=100,nullable=false)
	private String addr;
	
	@Column(length=50,nullable=false)
	private String phone;
	
	@Column(length=50,nullable=false)
	private String email;
	
	@Column(length=10,nullable=false)
	private String owner;
	
	@Column(length=10,nullable=false)
	private String mgr;
	
	@CreationTimestamp
	private Timestamp rdate;
	
	@UpdateTimestamp
	private Timestamp udate;
}

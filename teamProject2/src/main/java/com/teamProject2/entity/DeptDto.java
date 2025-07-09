package com.teamProject2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "DEPT")
@IdClass(DeptId.class)
@Data 
public class DeptDto {
	
	 	@Id
	    @Column(length = 3)
	    private String dgubun;

	    @Id
	    @Column
	    private int dcode;

	    @Column(length = 20)
	    private String dname;

}

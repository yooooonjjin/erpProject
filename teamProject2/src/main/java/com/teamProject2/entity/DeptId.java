package com.teamProject2.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptId implements Serializable{
	
	 private String dgubun;  
	 private int dcode;      

}

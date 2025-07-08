package com.teamProject2.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersId implements Serializable {
    private Integer ono;
    private String ogubun;
    private Integer ocode;
}
 
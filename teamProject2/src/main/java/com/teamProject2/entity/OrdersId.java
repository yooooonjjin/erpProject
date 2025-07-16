package com.teamProject2.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class OrdersId implements Serializable {
    private Integer ono;
    private String ogubun;
    private Integer ocode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdersId)) return false;
        OrdersId that = (OrdersId) o;
        return Objects.equals(ono, that.ono) &&
               Objects.equals(ogubun, that.ogubun) &&
               Objects.equals(ocode, that.ocode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ono, ogubun, ocode);
    }
}

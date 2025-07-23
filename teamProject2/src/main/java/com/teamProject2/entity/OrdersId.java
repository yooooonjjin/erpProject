package com.teamProject2.entity;

import java.io.Serializable;
import java.util.Objects;

public class OrdersId implements Serializable {

    private Integer ono;
    private String ogubun;
    private Integer ocode;

    // 반드시 기본 생성자, equals(), hashCode()가 있어야 함

    public OrdersId() {}

    public OrdersId(Integer ono, String ogubun, Integer ocode) {
        this.ono = ono;
        this.ogubun = ogubun;
        this.ocode = ocode;
    }

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

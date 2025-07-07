package com.teamProject2.entity;

import java.io.Serializable;
import java.util.Objects;

public class OrdersId implements Serializable {

    private String ogubun; // 구분
    private int ocode;    // 코드

    // 기본 생성자 (JPA 요구사항)
    public OrdersId() {}

    // 생성자
    public OrdersId(String ogubun, int ocode) {
        this.ogubun = ogubun;
        this.ocode = ocode;
    }

    // Getter, Setter
    public String getOgubun() {
        return ogubun;
    }

    public void setOgubun(String ogubun) {
        this.ogubun = ogubun;
    }

    public int getOcode() {
        return ocode;
    }

    public void setOcode(int ocode) {
        this.ocode = ocode;
    }

    // equals()와 hashCode() 메서드 오버라이드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdersId ordersId = (OrdersId) o;
        return ocode == ordersId.ocode &&
               Objects.equals(ogubun, ordersId.ogubun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ogubun, ocode);
    }
}
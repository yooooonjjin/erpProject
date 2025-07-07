package com.teamProject2.entity;

import java.io.Serializable;
import java.util.Objects;

public class ReasonId implements Serializable {

    private String rgubun; // 구분
    private int rcode;    // 코드

    // 기본 생성자 (JPA 요구사항)
    public ReasonId() {}

    // 생성자
    public ReasonId(String rgubun, int rcode) {
        this.rgubun = rgubun;
        this.rcode = rcode;
    }

    // Getter, Setter
    public String getRgubun() {
        return rgubun;
    }

    public void setRgubun(String rgubun) {
        this.rgubun = rgubun;
    }

    public int getRcode() {
        return rcode;
    }

    public void setRcode(int rcode) {
        this.rcode = rcode;
    }

    // equals()와 hashCode() 메서드 오버라이드
    @Override
    public boolean equals(Object r) {
        if (this == r) return true;
        if (r == null || getClass() != r.getClass()) return false;
        ReasonId reasonId = (ReasonId) r;
        return rcode == reasonId.rcode &&
               Objects.equals(rgubun, reasonId.rgubun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgubun, rcode);
    }
}
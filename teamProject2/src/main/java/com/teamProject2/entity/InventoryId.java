package com.teamProject2.entity;

import java.io.Serializable;
import java.util.Objects;

public class InventoryId implements Serializable {

    private int icode;  // INVCODE
    private String igubun;  // 구분

    // 기본 생성자 (JPA 요구사항)
    public InventoryId() {}

    // 생성자
    public InventoryId(int icode, String igubun) {
        this.icode = icode;
        this.igubun = igubun;
    }

    // Getter, Setter
    public int getIcode() {
        return icode;
    }

    public void setIcode(int icode) {
        this.icode = icode;
    }

    public String getIgubun() {
        return igubun;
    }

    public void setIgubun(String igubun) {
        this.igubun = igubun;
    }

    // equals()와 hashCode() 메서드 오버라이드
    @Override
    public boolean equals(Object i) {
        if (this == i) return true;
        if (i == null || getClass() != i.getClass()) return false;
        InventoryId inventoryId = (InventoryId) i;
        return icode == inventoryId.icode &&
               Objects.equals(igubun, inventoryId.igubun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icode, igubun);
    }
}
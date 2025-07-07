package com.teamProject2.entity;
	
import java.io.Serializable;
import java.util.Objects;

public class ClientId implements Serializable {
    private String cgubun;
    private int ccode;

    // 기본 생성자
    public ClientId() {}

    public ClientId(String cgubun, int ccode) {
        this.cgubun = cgubun;
        this.ccode = ccode;
    }
    
    // Getter, Setter
    public String getCgubun() {
        return cgubun;
    }

    public void setCgubun(String cgubun) {
        this.cgubun = cgubun;
    }

    public int getCcode() {
        return ccode;
    }

    public void setCcode(int ccode) {
        this.ccode = ccode;
    }

    // equals, hashCode 구현 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientId)) return false;
        ClientId clientId = (ClientId) o;
        return ccode == clientId.ccode && Objects.equals(cgubun, clientId.cgubun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cgubun, ccode);
    }


}

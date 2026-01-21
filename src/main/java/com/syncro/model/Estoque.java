package com.syncro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_estoque")
public class Estoque {

    @Id
    @Column(name = "box", length = 2, nullable = false)
    private Long box;

    @Column(name = "placa", length = 7, nullable = false)
    private String placa;

    public Long getBox() {
        return box;
    }

    public void setBox(Long box) {
        this.box = box;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}

package com.syncro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_peca")
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPeca")
    private Long idPeca;

    @Column(name = "placa", length = 7, nullable = true)
    private String placa;

    @Column
    private String descricao;

    @Column(length = 80)
    private String montadora;

    @Column(length = 120)
    private String modelo;

    private Integer ano;

    @Column(name = "subbox")
    private Long subbox;

    @Column(name = "subdescricao")
    private String subdescricao;

    public String getSubdescricao() {
        return subdescricao;
    }

    public void setSubdescricao(String subdescricao) {
        this.subdescricao = subdescricao;
    }

    public Long getSubbox() {
        return subbox;
    }

    public void setSubbox(Long subbox) {
        this.subbox = subbox;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getMontadora() {
        return montadora;
    }

    public void setMontadora(String montadora) {
        this.montadora = montadora;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Long getIdPeca() {
        return idPeca;
    }

    public void setIdPeca(Long idPeca) {
        this.idPeca = idPeca;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

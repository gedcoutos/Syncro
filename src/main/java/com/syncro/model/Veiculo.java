package com.syncro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_veiculo")
public class Veiculo {

    @Id
    @Column(name = "placa", length = 7, nullable = false)
    private String placa;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String montadora;

    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private StatusVeiculo estatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEntrada;

    private LocalDateTime dataSaida;

    @PrePersist
    void prePersist() {
        if (placa != null) {
            placa = placa.trim().toUpperCase();
        }
        dataEntrada = LocalDateTime.now();
        if (estatus == null) {
            estatus = StatusVeiculo.ATIVO;
        }
    }

    @PreUpdate
    void preUpdate() {
        if (placa != null)
            placa = placa.trim().toUpperCase();
    }


    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public StatusVeiculo getEstatus() {
        return estatus;
    }

    public void setEstatus(StatusVeiculo estatus) {
        this.estatus = estatus;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public String getMontadora() {
        return montadora;
    }

    public void setMontadora(String montadora) {
        this.montadora = montadora;
    }
}

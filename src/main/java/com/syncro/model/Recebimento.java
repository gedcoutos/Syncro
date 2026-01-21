package com.syncro.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_recebimento")
public class Recebimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecebimento;

    @ManyToOne
    @JoinColumn(name = "idPeca", nullable = false)
    private Peca peca;

    @ManyToOne
    @JoinColumn(name = "idFuncionario", nullable = true)
    private Funcionario funcionario;

    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime dataRecebimento;

    @Column(nullable = false)
    private int quantidade;

    public Long getIdRecebimento() {
        return idRecebimento;
    }

    public void setIdRecebimento(Long idRecebimento) {
        this.idRecebimento = idRecebimento;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Peca getPeca() {
        return peca;
    }

    public void setPeca(Peca peca) {
        this.peca = peca;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }


    @PrePersist
    void prePersist() {
        if (this.dataRecebimento == null) {
            this.dataRecebimento = LocalDateTime.now();
        }
    }

}

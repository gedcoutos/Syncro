package com.syncro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_foto_veiculo")
public class FotoVeiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFoto")
    private Long idFoto;

    @Column(name = "placa", length = 7, nullable = false)
    private String placa;

    @Column(name = "caminhoFoto", length = 255, nullable = false)
    private String caminhoFoto;

    @Column(name = "descricao", length = 100)
    private String descricao;

    public Long getIdFoto() {
        return idFoto;
    }

    public void setIdFoto(Long idFoto) {
        this.idFoto = idFoto;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}

package com.syncro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_caminho_os")
public class CaminhoOS {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "idArquivo")
   private Long idArquivo;

    @Column(name = "placa", length = 7, nullable = false)
    private String placa;

    @Column(name = "caminhoArquivo", length = 255, nullable = false)
    private String caminhoArquivo;

    @Column(name = "descricao", length = 100)
    private String descricao;

    public Long getIdArquivo() {
        return idArquivo;
    }

    public void setIdArquivo(Long idArquivo) {
        this.idArquivo = idArquivo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

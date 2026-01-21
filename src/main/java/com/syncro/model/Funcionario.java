package com.syncro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_funcionario")

public class Funcionario {
    public Funcionario() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFuncionario")
    private int idFuncionario;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String senha;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 30)
    private String cargo;

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }


}

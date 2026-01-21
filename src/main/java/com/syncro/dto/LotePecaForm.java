package com.syncro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class LotePecaForm {

    @NotBlank
    private String placa;

    @Valid
    private List<ItemPecaForm> itens = new ArrayList<>();

    // getters e setters
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public List<ItemPecaForm> getItens() { return itens; }
    public void setItens(List<ItemPecaForm> itens) { this.itens = itens; }
}

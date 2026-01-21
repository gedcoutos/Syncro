package com.syncro.service;

import com.syncro.dto.PecaInfoDTO;
import com.syncro.dto.VeiculoDetalhes;
import com.syncro.model.Estoque;
import com.syncro.model.Veiculo;
import com.syncro.repository.EstoqueRepository;
import com.syncro.repository.RecebimentoRepository;
import com.syncro.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsultaPlacaService {

    private final VeiculoRepository veiculoRepo;
    private final EstoqueRepository estoqueRepo;
    private final RecebimentoRepository recebimentoRepo;

    public ConsultaPlacaService(VeiculoRepository veiculoRepo,
                                EstoqueRepository estoqueRepo,
                                RecebimentoRepository recebimentoRepo) {
        this.veiculoRepo = veiculoRepo;
        this.estoqueRepo = estoqueRepo;
        this.recebimentoRepo = recebimentoRepo;
    }

    private String normalizarPlaca(String raw) {
        if (raw == null) return null;
        return raw.trim().toUpperCase();
    }

    public Optional<VeiculoDetalhes> consultar(String placaEntrada){
        String placa = normalizarPlaca(placaEntrada);
        if (placa == null || placa.isBlank()) return Optional.empty();

        var opt = veiculoRepo.findByPlaca(placa);
        if (opt.isEmpty()) return Optional.empty();
        var v  = opt.get();

        var boxes = estoqueRepo != null ?
                estoqueRepo.findAllByPlaca(placa).stream()
                        .map(Estoque::getBox)
                        .map(String::valueOf)
                        .toList()
                    : List.<String>of();

        var pecas = List.<PecaInfoDTO>of();

        var dto = new VeiculoDetalhes(
                v.getPlaca(),
                v.getMontadora(),
                v.getModelo(),
                v.getAno(),
                v.getDataEntrada(),
                boxes,
                pecas
        );
        return Optional.of(dto);

    }




}

package com.syncro.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VeiculoDetalhes(
        String placa,
        String montadora,
        String modelo,
        Integer ano,
        LocalDateTime dataEntrada,
        List<String> boxes,
        List<PecaInfoDTO> pecasGuardadas
) {}

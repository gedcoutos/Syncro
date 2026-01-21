package com.syncro.dto;

import java.time.LocalDateTime;

public record PecaInfoDTO(
        Long idPeca,
        String nomePeca,
        LocalDateTime dataChegada
) {}

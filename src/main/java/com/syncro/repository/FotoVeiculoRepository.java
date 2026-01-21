package com.syncro.repository;

import com.syncro.model.FotoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FotoVeiculoRepository extends JpaRepository<FotoVeiculo, Long> {
    List<FotoVeiculo> findByPlaca(String placa);
}

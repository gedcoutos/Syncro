package com.syncro.repository;

import com.syncro.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface VeiculoRepository extends JpaRepository<Veiculo, String> {
    boolean existsByPlaca(String placa);

    Optional<Veiculo> findByPlaca(String placa);

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    long deleteByPlaca(String placa);

}


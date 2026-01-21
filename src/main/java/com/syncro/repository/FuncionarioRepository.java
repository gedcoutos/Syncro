package com.syncro.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.syncro.model.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    Optional<Funcionario> findByUsername(String username);
}

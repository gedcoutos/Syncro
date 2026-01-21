package com.syncro.repository;

import com.syncro.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    boolean existsByBox(Long box);              // evitar dois carros no mesmo box
    List<Estoque> findAllByPlaca(String placa); // listar boxes atuais da placa (0..N)
    long deleteByPlaca(String placa);           // liberar tudo quando o carro sair

}


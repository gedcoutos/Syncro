package com.syncro.repository;

import com.syncro.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PecaRepository
        extends JpaRepository<Peca, Long>, JpaSpecificationExecutor<Peca> {

    List<Peca> findByPlaca(String placa);

    Optional<Peca> findByPlacaAndDescricaoIgnoreCase(String placa, String desc);

    long deleteByPlaca(String placa);

    @Query(value = """
    SELECT *
      FROM tbl_peca p
     WHERE p.placa = 'SPLACA'
       AND (:descricao IS NULL
            OR LOWER(p.subdescricao) LIKE LOWER(CONCAT('%', :descricao, '%'))
            OR SOUNDEX(p.subdescricao) = SOUNDEX(:descricao))
       AND (:montadora IS NULL OR LOWER(p.montadora) LIKE LOWER(CONCAT('%', :montadora, '%')))
       AND (:modelo    IS NULL OR LOWER(p.modelo)    LIKE LOWER(CONCAT('%', :modelo,    '%')))
       AND (:ano       IS NULL OR p.ano = :ano)
     ORDER BY p.subbox ASC, p.subdescricao ASC
    """, nativeQuery = true)
    List<Peca> buscarAvulsasFlex(String descricao, String montadora, String modelo, Integer ano);


}

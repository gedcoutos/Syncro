package com.syncro.repository;

import com.syncro.dto.PecaInfoDTO;
import com.syncro.model.Peca;
import com.syncro.model.Recebimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecebimentoRepository extends JpaRepository<Recebimento, Long>{
    boolean existsByPeca_IdPeca(Long idPeca);

    @Transactional
    long deleteByPecaPlaca(String placa);



    @Query("select r.dataRecebimento from Recebimento r where r.peca.id = :idPeca")
    Optional<java.time.LocalDateTime> findDataByPecaId(@Param("idPeca") Long idPeca);
}

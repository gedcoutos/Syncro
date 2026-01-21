package com.syncro.repository;


import com.syncro.model.CaminhoOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaminhoOSRepository extends JpaRepository<CaminhoOS, Long> {

    List<CaminhoOS> findByPlaca(String placa);

}

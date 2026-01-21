package com.syncro.service;

import com.syncro.model.Peca;
import com.syncro.repository.PecaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PecaService {

    private final PecaRepository pecaRepo;

    public PecaService(PecaRepository pecaRepo) {
        this.pecaRepo = pecaRepo;
    }


   public List<Peca> buscarPecasAvulsas(String descricao, String montadora, String modelo, Integer ano) {

        // Specification interna (não precisa criar o arquivo PecaSpecs)
        Specification<Peca> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // Regras fixas: só peças avulsas (sem placa)
            ps.add(cb.equal(root.get("nome"), "SEMPLACA"));
            ps.add(cb.isNull(root.get("placa")));

            if (descricao != null && !descricao.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("descricao")), "%" + descricao.toLowerCase() + "%"));
            }
            if (montadora != null && !montadora.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("montadora")), "%" + montadora.toLowerCase() + "%"));
            }
            if (modelo != null && !modelo.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("modelo")), "%" + modelo.toLowerCase() + "%"));
            }
            if (ano != null) {
                ps.add(cb.equal(root.get("ano"), ano));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };

        return pecaRepo.findAll(spec, Sort.by("descricao").ascending());
    }
}

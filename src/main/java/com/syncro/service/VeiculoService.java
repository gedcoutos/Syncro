package com.syncro.service;

import com.syncro.model.StatusVeiculo;
import com.syncro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;


@Service
public class VeiculoService {

    @Autowired private VeiculoRepository veiculoRepo;

    @Autowired private EstoqueRepository estoqueRepo;
    @Autowired private RecebimentoRepository recebimentoRepo;
    @Autowired private PecaRepository pecaRepo;

    @Autowired private CaminhoOSRepository caminhoRepo;
    @Autowired private FotoVeiculoRepository fotoVeiculoRepo;

    @Autowired private FotoStorageService fotoStorage;
    @Autowired private OsStorageService osStorage;

    @Transactional
    public void veiculoBaixa(String placa){
        String p = placa.trim().toUpperCase();
        var v =  veiculoRepo.findByPlaca(p)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado" + p));

        estoqueRepo.deleteByPlaca(p);
        recebimentoRepo.deleteByPecaPlaca(p);
        pecaRepo.deleteByPlaca(p);


        var oss = caminhoRepo.findByPlaca(p);

        for(var os : oss){
            try {
                osStorage.deletarOs(os.getCaminhoArquivo());
            } catch (Exception e) {
                System.err.println("⚠️ Falha ao deletar OS: " + os.getCaminhoArquivo() + " — " + e.getMessage());
            }
        }

        caminhoRepo.deleteAll(oss);

        var fotos = fotoVeiculoRepo.findByPlaca(p);

        for(var foto : fotos){
            try{
                fotoStorage.deletarFoto(foto.getCaminhoFoto());
            } catch (Exception e) {
                System.err.println("⚠️ Falha ao deletar Foto: " + foto.getCaminhoFoto() + " — " + e.getMessage());
            }
        }

        fotoVeiculoRepo.deleteAll(fotos);

        v.setEstatus(StatusVeiculo.ENTREGUE);
        v.setDataSaida(LocalDateTime.now());
        veiculoRepo.save(v);
        veiculoRepo.deleteByPlaca(p);
    }

}

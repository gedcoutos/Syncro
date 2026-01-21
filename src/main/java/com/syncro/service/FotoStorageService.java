package com.syncro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FotoStorageService {

    private final Path rootDir;
    private final Path fotosDirPath;

    public FotoStorageService(
            @Value("${syncro.storage-root:uploads}") String root,
            @Value("${syncro.fotos-dir:fotos}") String fotosDir
    ) {
        this.rootDir = Paths.get(root).toAbsolutePath().normalize();
        this.fotosDirPath = this.rootDir.resolve(fotosDir).normalize();
        try {
            Files.createDirectories(this.fotosDirPath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretorio de fotos! " + this.fotosDirPath, e);
        }
    }

    public String salvarFoto(String placa, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio.");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são permitidas.");
        }

        String placaNorm = (placa == null) ? "DESCONHECIDA"
                : placa.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");


        Path pastaPlaca = this.fotosDirPath.resolve(placaNorm);
        try {
            Files.createDirectories(pastaPlaca);
        } catch (IOException e) {
            throw new RuntimeException("Erro criando pasta da placa: " + pastaPlaca, e);
        }

        String original = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "sem_nome" : arquivo.getOriginalFilename()
        );

        if (original.contains("..")) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }


        String extensao = "";
        int i = original.lastIndexOf('.');
        if (i > 0) extensao = original.substring(i).toLowerCase();


        String nomeUnico = "IMG_" + placaNorm + "_" + UUID.randomUUID() + extensao;


        Path destino = pastaPlaca.resolve(nomeUnico);
        try {
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo em disco: " + destino, e);
        }


        return Paths.get("fotos", placaNorm, nomeUnico).toString().replace("\\", "/");
    }

    public boolean deletarFoto(String caminhoRelativo){
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) return false;

        String rel = caminhoRelativo.replace("\\", "/");
        if (rel.startsWith("uploads/")) rel = rel.substring("uploads/".length());

        Path alvo = rootDir.resolve(rel).normalize();
        if (!alvo.startsWith(rootDir)) {
            throw new IllegalArgumentException("Caminho fora da pasta de uploads.");
        }

        try {
            boolean ok = Files.deleteIfExists(alvo);
            try { Files.delete(alvo.getParent()); } catch (Exception ignore) {}
            return ok;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao deletar arquivo em disco: " + alvo, e);
        }
    }
}

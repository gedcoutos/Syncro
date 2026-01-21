package com.syncro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class OsStorageService {

    private final Path rootDir;
    private final Path osDirPath;

    public OsStorageService(
            @Value("${syncro.storage-root:uploads}") String root,
            @Value("${syncro.os-dir:os}") String osDir
    ) {
        this.rootDir   = Paths.get(root).toAbsolutePath().normalize();
        this.osDirPath = this.rootDir.resolve(osDir).normalize();
        try {
            Files.createDirectories(this.osDirPath); // garante /uploads/os
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório de OS: " + this.osDirPath, e);
        }
    }

    public String salvarOs(String placa, MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty())
            throw new IllegalArgumentException("Arquivo vazio.");

        String nome = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename().toLowerCase() : "";
        String tipo = arquivo.getContentType() != null ? arquivo.getContentType().toLowerCase() : "";
        boolean ehPdf = nome.endsWith(".pdf") || "application/pdf".equals(tipo) || tipo.contains("pdf");
        if (!ehPdf) throw new IllegalArgumentException("Apenas PDF é permitido para OS.");

        String placaNorm = normalizarPlaca(placa);

        Path pastaPlaca = osDirPath.resolve(placaNorm);
        Files.createDirectories(pastaPlaca);

        String nomeUnico = "OS_" + placaNorm + "_" + UUID.randomUUID() + ".pdf";

        Path destino = pastaPlaca.resolve(nomeUnico).normalize();
        Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return Paths.get("os", placaNorm, nomeUnico).toString().replace("\\", "/");
    }

    public boolean deletarOs(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) return false;

        String rel = caminhoRelativo.replace("\\", "/");
        if (rel.startsWith("uploads/")) rel = rel.substring("uploads/".length());

        Path alvo = rootDir.resolve(rel).normalize();

        if (!alvo.startsWith(rootDir))
            throw new IllegalArgumentException("Caminho fora da pasta de uploads.");

        try {
            boolean ok = Files.deleteIfExists(alvo);

            try { Files.delete(alvo.getParent()); } catch (Exception ignore) {}

            return ok;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao deletar arquivo de OS: " + alvo, e);
        }
    }

    private String normalizarPlaca(String placa) {
        if (placa == null) return "DESCONHECIDA";
        return placa.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}

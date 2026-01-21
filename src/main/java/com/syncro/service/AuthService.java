package com.syncro.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.syncro.model.Funcionario;
import com.syncro.repository.FuncionarioRepository;

@Service

public class AuthService {
    private final FuncionarioRepository repo;

    public AuthService(FuncionarioRepository repo) {
        this.repo = repo;
    }

    public Optional<Funcionario> autenticar(String username, String senhaDigitada) {
        return repo.findByUsername(username)
                .filter(f  -> f.getSenha().equals(senhaDigitada));

    }

}

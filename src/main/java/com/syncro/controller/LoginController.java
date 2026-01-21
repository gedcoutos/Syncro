package com.syncro.controller;

import com.syncro.model.Funcionario;
import com.syncro.repository.FuncionarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    // Página de login
    @GetMapping("/login")
    public String form(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null)  model.addAttribute("msgErro", "Usuário ou senha inválidos.");
        if (logout != null) model.addAttribute("ok", "Você saiu do sistema.");
        return "login";
    }

    // Processa login
    @PostMapping("/login")
    public String processarLogin(@RequestParam String username,
                                 @RequestParam String senha,
                                 HttpSession session) {

        String u = username.trim();
        Optional<Funcionario> funcOpt = funcionarioRepository.findByUsername(u);

        if (funcOpt.isPresent() && senha.equals(funcOpt.get().getSenha())) {
            Funcionario f = funcOpt.get();

            // Guarda na sessão para autorização e UI
            session.setAttribute("usuarioLogado", f.getUsername());
            session.setAttribute("nomeExibicao", f.getNome());
            session.setAttribute("papel", normalizarPapel(f.getCargo())); // "ADMIN" ou "OPERADOR"

            return "redirect:/home";
        } else {
            return "redirect:/login?error=1";
        }
    }

    // Logout simples
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "redirect:/login?logout=1";
    }

    // Normaliza texto vindo do banco/form
    private String normalizarPapel(String cargo) {
        if (cargo == null) return "OPERADOR";
        String c = cargo.trim().toUpperCase();
        if (c.startsWith("ADM")) return "ADMIN";
        if (c.startsWith("OPE") || c.startsWith("FUN")) return "OPERADOR";
        return c; // já era "ADMIN" ou "OPERADOR"
    }
}

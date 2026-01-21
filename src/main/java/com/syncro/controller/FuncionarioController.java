package com.syncro.controller;

import com.syncro.model.Funcionario;
import com.syncro.repository.FuncionarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository funcionarioRepo;

    // ✅ Exibe o formulário + lista para o modal
    @GetMapping("/funcionarios/novo")
    public String mostrarFormularioFuncionario(Model model) {
        if (!model.containsAttribute("funcionario")) {
            model.addAttribute("funcionario", new Funcionario());
        }
        // manda a lista (ordenada por nome para ficar bonito)
        model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
        return "cadastrar_funcionario";
    }

    // ✅ Salva o funcionário
    @PostMapping("/funcionarios")
    public String salvarFuncionario(@ModelAttribute Funcionario f,
                                    Model model,
                                    RedirectAttributes ra) {

        String nome     = safe(f.getNome());
        String username = safe(f.getUsername()).toLowerCase();
        String senha    = safe(f.getSenha());
        String cargo    = normalizarCargo(f.getCargo());

        if (nome.length() < 3) {
            model.addAttribute("erro", "Nome muito curto.");
            model.addAttribute("funcionario", f);
            model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
            return "cadastrar_funcionario";
        }
        if (username.length() < 3) {
            model.addAttribute("erro", "Usuário muito curto.");
            model.addAttribute("funcionario", f);
            model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
            return "cadastrar_funcionario";
        }
        if (senha.length() < 4) {
            model.addAttribute("erro", "Senha muito curta (mín. 4).");
            model.addAttribute("funcionario", f);
            model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
            return "cadastrar_funcionario";
        }

        Optional<Funcionario> existente = funcionarioRepo.findByUsername(username);
        if (existente.isPresent()) {
            model.addAttribute("erro", "Já existe um usuário com esse login.");
            model.addAttribute("funcionario", f);
            model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
            return "cadastrar_funcionario";
        }

        f.setNome(nome);
        f.setUsername(username);
        f.setSenha(senha);
        f.setCargo(cargo);
        funcionarioRepo.save(f);

        model.addAttribute("ok", "Funcionário cadastrado com sucesso!");
        model.addAttribute("funcionario", new Funcionario());
        model.addAttribute("funcionarios", funcionarioRepo.findAll(Sort.by("nome")));
        return "cadastrar_funcionario";
    }

    // ✅ Excluir funcionário
    @PostMapping("/funcionarios/{id}/excluir")
    public String excluirFuncionario(@PathVariable("id") int id,
                                     HttpSession session,
                                     RedirectAttributes ra) {

        var atual = (String) (session != null ? session.getAttribute("usuarioLogado") : null);

        return funcionarioRepo.findById(id).map(f -> {
            if (atual != null && atual.equalsIgnoreCase(f.getUsername())) {
                ra.addFlashAttribute("erro", "Você não pode excluir o próprio usuário logado.");
                return "redirect:/funcionarios/novo";
            }
            try {
                funcionarioRepo.delete(f);
                ra.addFlashAttribute("ok", "Funcionário removido: " + f.getNome());
            } catch (Exception e) {
                ra.addFlashAttribute("erro", "Não foi possível excluir: " + e.getMessage());
            }
            return "redirect:/funcionarios/novo";
        }).orElseGet(() -> {
            ra.addFlashAttribute("erro", "Funcionário não encontrado.");
            return "redirect:/funcionarios/novo";
        });
    }

    // ---- utilitários
    private String safe(String s) { return s == null ? "" : s.trim(); }

    private String normalizarCargo(String c) {
        if (c == null) return "OPERADOR";
        String v = c.trim().toUpperCase();
        if (v.startsWith("ADM")) return "ADMIN";
        if (v.startsWith("OPE") || v.startsWith("FUN")) return "OPERADOR";
        return v;
    }
}

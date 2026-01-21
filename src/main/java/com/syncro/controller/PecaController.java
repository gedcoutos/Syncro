package com.syncro.controller;

import com.syncro.dto.ItemPecaForm;
import com.syncro.dto.LotePecaForm;
import com.syncro.model.*;
import com.syncro.repository.*;
import com.syncro.service.PecaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;


import jakarta.validation.Valid;

import java.util.*;

@Controller
@RequestMapping("/pecas")
public class PecaController {

    private final PecaRepository pecaRepo;
    private final VeiculoRepository veiculoRepo;
    private final EstoqueRepository estoqueRepo;
    private final RecebimentoRepository recebimentoRepo;
    private final FotoVeiculoRepository fotoRepo;
    private final CaminhoOSRepository caminhoRepo;
    private final PecaService pecaService;

    public PecaController(PecaRepository pecaRepo,
                          VeiculoRepository veiculoRepo,
                          EstoqueRepository estoqueRepo,
                          RecebimentoRepository recebimentoRepo,
                          FotoVeiculoRepository fotoRepo,
                          CaminhoOSRepository caminhoRepo,
                          PecaService pecaService) {
        this.pecaRepo = pecaRepo;
        this.veiculoRepo = veiculoRepo;
        this.estoqueRepo = estoqueRepo;
        this.recebimentoRepo = recebimentoRepo;
        this.fotoRepo = fotoRepo;
        this.caminhoRepo = caminhoRepo;
        this.pecaService = pecaService;
    }

    @GetMapping("/cadastrar")
    public String telaCadastrarPeca(Model model) {
        if (!model.containsAttribute("veiculo")) {
            model.addAttribute("veiculo", new Veiculo());
        }
        return "registro_peca";
    }


    @GetMapping("/novo")
    public String telaCadastro(@RequestParam String placa, Model model) {
        String normalizada = placa.trim().toUpperCase();
        var veiculo = veiculoRepo.findByPlaca(normalizada);
        if (veiculo == null) {
            model.addAttribute("erro", "Placa não encontrada.");
            return "peca_buscar_placa";
        }
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("pecas", pecaRepo.findByPlaca(normalizada));
        return "registro_peca";
    }

    @Transactional
    @PostMapping("/salvar-lote")
    public String salvarLote(@Valid @ModelAttribute LotePecaForm form,
                             BindingResult result,
                             RedirectAttributes ra) {

        List<String> msgsDuplicadas = new ArrayList<>();

        String placa = form.getPlaca() == null ? "" : form.getPlaca().trim().toUpperCase();

        if (placa.isEmpty()) {
            ra.addFlashAttribute("erro", "Placa vazia.");
            return "redirect:/pecas/cadastrar";
        }

        if (form.getItens() == null || form.getItens().stream()
                .allMatch(i -> i == null || i.getDescricao() == null || i.getDescricao().trim().isEmpty())) {
            ra.addFlashAttribute("erro", "Preencha ao menos uma descrição de peça.");
            return "redirect:/pecas/novo?placa=" + placa;
        }

        var veiculo = veiculoRepo.findByPlaca(placa);
        if (veiculo == null) {
            ra.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/pecas/cadastrar";
        }

        List<Recebimento> recebimentos = new ArrayList<>();

        for (ItemPecaForm item : form.getItens()) {
            if (item == null) continue;
            String desc = item.getDescricao() == null ? "" : item.getDescricao().trim();
            if (desc.isEmpty()) continue;

            int qtd = 1;

            Peca peca = pecaRepo.findByPlacaAndDescricaoIgnoreCase(placa, desc)
                    .orElseGet(() -> {
                        Peca p = new Peca();
                        p.setPlaca(placa);
                        p.setDescricao(desc);
                        return pecaRepo.save(p);
                    });

            if (recebimentoRepo.existsByPeca_IdPeca(peca.getIdPeca())) {
                msgsDuplicadas.add("Peça já recebida: " + desc);
                continue;
            }

            Recebimento r = new Recebimento();
            r.setPeca(peca);
            r.setQuantidade(1);
            recebimentos.add(r);
        }

        if (recebimentos.isEmpty()) {
            ra.addFlashAttribute("erro", "Nenhum item válido para salvar.");
            return "redirect:/pecas/novo?placa=" + placa;
        }

        recebimentoRepo.saveAll(recebimentos);

        if (!msgsDuplicadas.isEmpty()) {
            ra.addFlashAttribute("info", String.join(" | ", msgsDuplicadas));
        }


        ra.addFlashAttribute("ok", recebimentos.size() + " recebimento(s) lançado(s) para " + placa + ".");

        var boxesDaPlaca = estoqueRepo.findAllByPlaca(placa);
        if (!boxesDaPlaca.isEmpty()) {
            String lista = boxesDaPlaca.stream()
                    .map(e -> String.valueOf(e.getBox()))
                    .sorted()
                    .reduce((a,b) -> a + ", " + b)
                    .orElse("-");
            ra.addFlashAttribute("info", "Veículo já está alocado no box: " + lista + ".");
            return "redirect:/pecas/novo?placa=" + placa;   // <-- fica na página de cadastro de peças
        }

        return "redirect:/pecas/escolher-box?placa=" + placa.toUpperCase();
    }

    @GetMapping("/api/veiculos/{placa}")
    @ResponseBody
    public ResponseEntity<?> apiVeiculo(@PathVariable String placa) {
        String p = placa.trim().toUpperCase();
        var opt = veiculoRepo.findByPlaca(p);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var v = opt.get();

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("placa",  v.getPlaca());
        body.put("modelo", v.getModelo());
        body.put("ano",    v.getAno());
        body.put("montadora", v.getMontadora());

        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/veiculos/{placa}/fotos")
    @ResponseBody
    public List<String> listarFotos(@PathVariable String placa) {
        String p = placa == null ? "" : placa.trim().toUpperCase();
        var fotos = fotoRepo.findByPlaca(p);
        System.out.println("[FOTOS] placa=" + p + " qtd=" + fotos.size());
        return fotos.stream()
                .map(FotoVeiculo::getCaminhoFoto)
                .map(u -> u.startsWith("/") ? u : "/" + u)
                .toList();
    }

    @GetMapping("/api/veiculos/{placa}/os")
    @ResponseBody
    public List<String> listarOS(@PathVariable String placa) {
        String p = placa == null ? "" : placa.trim().toUpperCase();

        return caminhoRepo.findByPlaca(p).stream()
                .map(CaminhoOS::getCaminhoArquivo)
                .map(u -> {
                    if (u == null) return null;
                    String s = u.replace("\\", "/");

                    if (s.startsWith("http://") || s.startsWith("https://")) return s;

                    int up = s.indexOf("uploads/");
                    if (up >= 0) {
                        s = s.substring(up + "uploads/".length());
                    }

                    if (s.startsWith("os/")) s = "/" + s;

                    if (s.startsWith("/os/")) return s;

                    return "/os/" + p + "/" + s;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @PostMapping("/{id}/editar")
    public String salvarEdicao(@PathVariable Long id, @RequestParam("descricao") String descricao,RedirectAttributes ra) {
        return pecaRepo.findById(id).map(peca ->{
          String nova = descricao == null ? "" : descricao.trim();
          if (nova.isBlank()){
              ra.addFlashAttribute("erro", "Descrição não pode ficar em branco.");
              return "redirect:/veiculos/" + peca.getPlaca();
          }

          boolean duplicada = pecaRepo.findByPlacaAndDescricaoIgnoreCase(peca.getPlaca(), nova)
                  .filter(p -> !p.getIdPeca().equals(id)).isPresent();
          if (duplicada) {
              ra.addFlashAttribute("erro", "Já existe uma peça com essa descrição para esta placa.");
              return "redirect:/veiculos/" + peca.getPlaca();
          }

          peca.setDescricao(nova);
          pecaRepo.save(peca);

            ra.addFlashAttribute("ok", "Peça atualizada com sucesso.");
            return "redirect:/veiculos/" + peca.getPlaca();

        }).orElseGet(() -> {
            ra.addFlashAttribute("erro", "Peça não encontrada.");
            return "redirect:/consulta-placa";
        });
    }

    @GetMapping("/estoque/consulta")
    public String telaConsultaEstoque() {
        return "consulta_estoque";
    }

    @GetMapping("/estoque/consulta/buscar")
    public String buscarEstoqueAvulsas(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String montadora,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer ano,
            Model model) {

        var resultados = pecaRepo.buscarAvulsasFlex(descricao, montadora, modelo, ano);

        model.addAttribute("resultados", resultados);
        model.addAttribute("descricao", descricao);
        model.addAttribute("montadora", montadora);
        model.addAttribute("modelo", modelo);
        model.addAttribute("ano", ano);

        return "consulta_estoque";
    }

    @GetMapping("/avulsas/cadastrar")
    public String telaCadastrarPecaAvulsa() {
        return "cadastrar_peca"; // nome exato do arquivo HTML (sem .html)
    }

    @PostMapping("/avulsas/salvar")
    public String salvarPecaAvulsa(
            @RequestParam String descricao,   // <- “nome” digitado
            @RequestParam String montadora,
            @RequestParam String modelo,
            @RequestParam Integer ano,
            @RequestParam Integer subbox,
            RedirectAttributes ra) {

        String desc = descricao == null ? "" : descricao.trim();
        String mont = montadora == null ? "" : montadora.trim();
        String mod  = modelo == null ? "" : modelo.trim();

        if (desc.isEmpty() || mont.isEmpty() || mod.isEmpty() || ano == null || subbox == null) {
            ra.addFlashAttribute("erro", "Preencha todos os campos obrigatórios.");
            return "redirect:/pecas/avulsas/cadastrar";
        }
        if (subbox < 56 || subbox > 60) {
            ra.addFlashAttribute("erro", "Subbox inválido (use 56 a 60).");
            return "redirect:/pecas/avulsas/cadastrar";
        }

        Peca p = new Peca();
        p.setPlaca("SPLACA");
        p.setDescricao(null);                  // <- chave: não participa da UNIQUE
        p.setSubdescricao(desc);               // <- “nome” da peça avulsa
        p.setMontadora(mont);
        p.setModelo(mod);
        p.setAno(ano);
        p.setSubbox(subbox.longValue());

        pecaRepo.save(p);
        ra.addFlashAttribute("ok", "Peça salva no box " + subbox + "!");
        return "redirect:/pecas/avulsas/cadastrar";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPeca(@PathVariable Long id, RedirectAttributes ra) {
        return pecaRepo.findById(id).map(peca -> {

            // segurança: esta tela só exclui peças avulsas
            if (peca.getPlaca() == null || !peca.getPlaca().equalsIgnoreCase("SPLACA")) {
                ra.addFlashAttribute("erro", "Só é permitido excluir peças do estoque avulso por esta tela.");
                return "redirect:/pecas/estoque/consulta";
            }

            try {
                pecaRepo.deleteById(id);  // não há recebimentos -> pode apagar direto

                String nome = (peca.getSubdescricao() != null && !peca.getSubdescricao().isBlank())
                        ? peca.getSubdescricao()
                        : peca.getDescricao();

                ra.addFlashAttribute("ok", "Peça excluída: " + (nome == null ? "(sem nome)" : nome));
            } catch (Exception e) {
                ra.addFlashAttribute("erro", "Não foi possível excluir a peça: " + e.getMessage());
            }

            return "redirect:/pecas/estoque/consulta";
        }).orElseGet(() -> {
            ra.addFlashAttribute("erro", "Peça não encontrada.");
            return "redirect:/pecas/estoque/consulta";
        });
    }

    @PostMapping("/vincular-placa/{id}")
    public String vincularPlaca(@PathVariable Long id,
                                @RequestParam String placa,
                                @RequestParam(required = false) String confirmar,
                                RedirectAttributes ra) {

        String novaPlaca = placa == null ? "" : placa.trim().toUpperCase();

        return pecaRepo.findById(id).map(p -> {

            // Somente peças avulsas podem ser vinculadas
            if (!"SPLACA".equalsIgnoreCase(p.getPlaca())) {
                ra.addFlashAttribute("erro", "Somente peças avulsas podem ser vinculadas por aqui.");
                return "redirect:/pecas/estoque/consulta";
            }

            if (novaPlaca.isBlank()) {
                ra.addFlashAttribute("erro", "Informe uma placa válida.");
                return "redirect:/pecas/estoque/consulta";
            }

            // Buscar veículo (Optional)
            var optVeic = veiculoRepo.findByPlaca(novaPlaca);
            if (optVeic.isEmpty()) {
                ra.addFlashAttribute("erro", "Veículo não encontrado para a placa " + novaPlaca + ".");
                return "redirect:/pecas/estoque/consulta";
            }
            var veic = optVeic.get(); // agora é um Veiculo real

            // Nome da peça (subdescrição)
            String novaDescricao = (p.getSubdescricao() == null ? "" : p.getSubdescricao().trim());
            if (novaDescricao.isBlank()) {
                ra.addFlashAttribute("erro", "Peça avulsa sem subdescrição. Não é possível vincular.");
                return "redirect:/pecas/estoque/consulta";
            }

            // Checar duplicidade no veículo
            boolean duplicada = pecaRepo.findByPlacaAndDescricaoIgnoreCase(novaPlaca, novaDescricao).isPresent();
            if (duplicada) {
                ra.addFlashAttribute("erro", "Já existe no veículo (" + novaPlaca + ") uma peça \"" + novaDescricao + "\".");
                return "redirect:/pecas/estoque/consulta";
            }

            // ---------------------------------------------------------
            // VALIDAR MONTADORA / MODELO – versão simples e correta
            // ---------------------------------------------------------
            String montPeca = p.getMontadora();
            String modPeca  = p.getModelo();

            String montVeic = veic.getMontadora();
            String modVeic  = veic.getModelo();

            boolean montadoraOk = montPeca != null && montPeca.equalsIgnoreCase(montVeic);
            boolean modeloOk    = modPeca  != null && modPeca.equalsIgnoreCase(modVeic);

            // Se diferente e ainda NÃO confirmou → pedir confirmação
            if ((!montadoraOk || !modeloOk) && !"true".equals(confirmar)) {

                String msg = "MONTADORA e MODELO da peça não correspondem ao veículo "
                        + montVeic + " " + modVeic + ". Deseja continuar?";

                ra.addFlashAttribute("confirmacao", msg);
                ra.addFlashAttribute("confirmarVinculoLink",
                        "/pecas/vincular-placa/" + id + "?placa=" + novaPlaca + "&confirmar=true");

                return "redirect:/pecas/estoque/consulta";
            }
            // ---------------------------------------------------------

            try {
                // mover do avulso para o veículo
                p.setPlaca(novaPlaca);
                p.setDescricao(novaDescricao);
                p.setSubdescricao(null);

                // limpar dados de avulsa
                p.setSubbox(null);
                p.setMontadora(null);
                p.setModelo(null);
                p.setAno(null);

                pecaRepo.save(p);

                ra.addFlashAttribute("ok", "Peça vinculada à placa " + novaPlaca + " com sucesso.");
                return "redirect:/pecas/estoque/consulta";

            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                ra.addFlashAttribute("erro",
                        "Não foi possível vincular: já existe no veículo (" + novaPlaca + ") uma peça \"" + novaDescricao + "\".");
                return "redirect:/pecas/estoque/consulta";

            } catch (Exception e) {
                ra.addFlashAttribute("erro", "Erro ao vincular: " + e.getMessage());
                return "redirect:/pecas/estoque/consulta";
            }

        }).orElseGet(() -> {
            ra.addFlashAttribute("erro", "Peça não encontrada.");
            return "redirect:/pecas/estoque/consulta";
        });
    }


}
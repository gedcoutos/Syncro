package com.syncro.controller;

import com.syncro.dto.BoxInfoDTO;
import com.syncro.dto.PecaInfoDTO;
import com.syncro.model.StatusVeiculo;
import com.syncro.repository.EstoqueRepository;
import com.syncro.repository.PecaRepository;
import com.syncro.repository.RecebimentoRepository;
import com.syncro.repository.VeiculoRepository;
import com.syncro.service.ConsultaPlacaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConsultaPlacaController {

    private final ConsultaPlacaService service;
    private final PecaRepository pecaRepo;
    private final RecebimentoRepository recebimentoRepo;
    private final EstoqueRepository estoqueRepo;
    private final VeiculoRepository veiculoRepo;

    public ConsultaPlacaController(ConsultaPlacaService service,
                                   PecaRepository pecaRepo,
                                   RecebimentoRepository recebimentoRepo,
                                   EstoqueRepository estoqueRepo,
                                   VeiculoRepository veiculoRepo) {
        this.service = service;
        this.pecaRepo = pecaRepo;
        this.recebimentoRepo = recebimentoRepo;
        this.estoqueRepo = estoqueRepo;
        this.veiculoRepo = veiculoRepo;
    }

    // 1) Tela de busca
    @GetMapping("/consulta-placa")
    public String form() {
        // se seu arquivo é "buscar_veiculo.html", troque para "buscar_veiculo"
        return "consulta_placa";
    }

    // 2) Recebe a placa e decide o destino
    @PostMapping("/consulta-placa")
    public String consultar(@RequestParam("placa") String placa, RedirectAttributes ra) {
        String normalizada = placa == null ? "" : placa.trim().toUpperCase();
        if (normalizada.isBlank()) {
            ra.addFlashAttribute("erro", "Informe uma placa.");
            return "redirect:/consulta-placa";
        }

        // use sempre a normalizada
        var v = veiculoRepo.findByPlaca(normalizada).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("info", "Veículo não encontrado para a placa " + normalizada + ".");
            return "redirect:/consulta-placa";
        }
        if (v.getEstatus() == StatusVeiculo.ENTREGUE) {
            ra.addFlashAttribute("info", "Esta placa está marcada como ENTREGUE.");
            return "redirect:/consulta-placa";
        }

        return "redirect:/veiculos/" + normalizada;
    }

    // 3) Detalhes do veículo
    @GetMapping("/veiculos/{placa}")
    public String detalhes(@PathVariable String placa, Model model, RedirectAttributes ra) {
        String normalizada = placa == null ? "" : placa.trim().toUpperCase();
        String canonico = normalizada.replace("-", "");

        return service.consultar(canonico).map(v -> {
            model.addAttribute("v", v);

            var pecas = pecaRepo.findByPlaca(canonico);
            var pecasData = pecas.stream()
                    .map(p -> new PecaInfoDTO(
                            p.getIdPeca(),
                            p.getDescricao(),
                            recebimentoRepo.findDataByPecaId(p.getIdPeca()).orElse(null)
                    ))
                    .toList();
            model.addAttribute("pecasData", pecasData);

            var boxInfo = estoqueRepo.findAllByPlaca(canonico).stream()
                    .map(e -> new BoxInfoDTO(e.getBox()))
                    .toList();
            model.addAttribute("boxes", boxInfo);

            return "veiculo_detalhes";
        }).orElseGet(() -> {
            ra.addFlashAttribute("info", "Veículo não encontrado para a placa " + normalizada + ".");
            return "redirect:/consulta-placa";
        });
    }
}

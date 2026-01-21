package com.syncro.controller;

import com.syncro.model.Estoque;
import com.syncro.repository.EstoqueRepository;
import com.syncro.repository.VeiculoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EstoqueController {

    private final EstoqueRepository estoqueRepo;
    private final VeiculoRepository veiculoRepo;

    public EstoqueController(EstoqueRepository estoqueRepo,
                             VeiculoRepository veiculoRepo) {
        this.estoqueRepo = estoqueRepo;
        this.veiculoRepo = veiculoRepo;
    }

    private static final int QTD_BOXES = 60;

    private List<Long> todosBoxes() {
        List<Long> lista = new ArrayList<>();
        for (long i = 1; i <= QTD_BOXES; i++) lista.add(i);
        return lista;
    }

    private String prepararBoxView(String placa, Model model, boolean mobileFlag) {
        String p = placa.trim().toUpperCase();

        Set<Long> meusBoxes = estoqueRepo.findAllByPlaca(p).stream()
                .map(Estoque::getBox)
                .collect(Collectors.toSet());

        Map<Long, String> ocupados = new HashMap<>();
        estoqueRepo.findAll().forEach(e -> {
            String pl = e.getPlaca();
            String modelo = veiculoRepo.findByPlaca(pl)
                    .map(v -> v.getModelo() == null ? "" : v.getModelo())
                    .orElse("");
            String label = (modelo.isBlank() ? pl : (pl + " - " + modelo));
            ocupados.put(e.getBox(), label);
        });

        model.addAttribute("placa", p);
        model.addAttribute("todos", todosBoxes());   // 1..60
        model.addAttribute("ocupados", ocupados);    // Map<Long,String>
        model.addAttribute("meusBoxes", meusBoxes);  // Set<Long>
        model.addAttribute("mobile", mobileFlag);    // para o template adaptar layout

        return "escolher_box";
    }

    @GetMapping("/pecas/escolher-box")
    public String escolherBox(@RequestParam String placa,
                              @RequestParam(name = "mobile", required = false, defaultValue = "false") boolean mobile,
                              Model model) {
        return prepararBoxView(placa, model, mobile);
    }
    @PostMapping("/pecas/vincular-box")
    @Transactional
    public String vincularBox(@RequestParam String placa,
                              @RequestParam Long box,
                              RedirectAttributes ra) {
        String p = placa.trim().toUpperCase();

        if (box == null || box < 1 || box > QTD_BOXES) {
            ra.addFlashAttribute("erro", "Box inválido (1.." + QTD_BOXES + ").");
            return "redirect:/pecas/escolher-box?placa=" + p;
        }
        if (estoqueRepo.existsByBox(box)) {
            ra.addFlashAttribute("erro", "Box " + box + " já ocupado.");
            return "redirect:/pecas/escolher-box?placa=" + p;
        }

        var e = new Estoque();
        e.setPlaca(p);
        e.setBox(box);
        estoqueRepo.save(e);

        ra.addFlashAttribute("ok", "Box " + box + " vinculado à placa " + p + ".");

        return "redirect:/pecas/novo?placa=" + p;
    }

    @PostMapping("/pecas/liberar-placa")
    @Transactional
    public String liberarPlaca(@RequestParam String placa, RedirectAttributes ra) {
        String p = placa.trim().toUpperCase();
        long qtd = estoqueRepo.deleteByPlaca(p);
        ra.addFlashAttribute("ok", "Liberados " + qtd + " box(es) da placa " + p + ".");
        return "redirect:/pecas/escolher-box?placa=" + p;
    }

    @GetMapping("/mobile/box")
    public String mobileBoxForm(@RequestParam(required = false) String placa, Model model) {
        if (placa != null && !placa.isBlank()) {
            model.addAttribute("placa", placa.trim().toUpperCase());
        }
        return "mobile_escolher_placa";
    }

    @PostMapping("/mobile/box")
    public String mobileBoxSubmit(@RequestParam String placa, RedirectAttributes ra) {
        String p = placa == null ? "" : placa.trim().toUpperCase();

        if (p.isBlank()) {
            ra.addFlashAttribute("erro", "Digite a placa.");
            ra.addFlashAttribute("placa", placa);
            return "redirect:/mobile/box";
        }

        if (p.length() != 7) {
            ra.addFlashAttribute("erro", "Placa inválida. Use 7 caracteres.");
            ra.addFlashAttribute("placa", placa);
            return "redirect:/mobile/box";
        }

        boolean existe = veiculoRepo.findByPlaca(p).isPresent();
        if (!existe) {
            ra.addFlashAttribute("erro", "Placa não cadastrada.");
            ra.addFlashAttribute("placa", placa);
            return "redirect:/mobile/box";
        }

        return "redirect:/pecas/escolher-box?placa=" + p + "&mobile=1";
    }

    @PostMapping("/mobile/vincular-box")
    @Transactional
    public String vincularBoxMobile(@RequestParam String placa,
                                    @RequestParam Long box,
                                    RedirectAttributes ra) {
        String p = placa == null ? "" : placa.trim().toUpperCase();

        if (p.isBlank()) {
            ra.addFlashAttribute("erro", "Placa vazia.");
            return "redirect:/mobile/box";
        }
        if (box == null || box < 1 || box > QTD_BOXES) {
            ra.addFlashAttribute("erro", "Box inválido (1.." + QTD_BOXES + ").");
            return "redirect:/pecas/escolher-box?placa=" + p;
        }
        if (estoqueRepo.existsByBox(box)) {
            ra.addFlashAttribute("erro", "Box " + box + " já ocupado.");
            return "redirect:/pecas/escolher-box?placa=" + p;
        }

        var e = new Estoque();
        e.setPlaca(p);
        e.setBox(box);
        estoqueRepo.save(e);

        ra.addFlashAttribute("ok", "Box " + box + " vinculado à placa " + p + ".");
        return "redirect:/home"; // << volta pra Home só nesse fluxo
    }

    @GetMapping("/estoque/visualizar")
    public String visualizarBoxes(Model model) {
        List<Long> todos = java.util.stream.LongStream.rangeClosed(1, 60).boxed().toList();

        var ocupadosBrutos = estoqueRepo.findAll().stream()
                .filter(e -> e.getPlaca() != null && !e.getPlaca().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getBox(),   // Long
                        e -> e.getPlaca()
                ));

        // Mapa de rótulos finais
        java.util.Map<Long,String> ocupadosLabel = new java.util.HashMap<>();
        ocupadosBrutos.forEach((box, placa) -> {
            if (box >= 56 && box <= 60 && "SPLACA".equalsIgnoreCase(placa)) {
                ocupadosLabel.put(box, "ESTOQUE DA OFICINA");
            } else {
                ocupadosLabel.put(box, placa);
            }
        });

        model.addAttribute("todos", todos);
        model.addAttribute("ocupados", ocupadosLabel); // já “traduzido”
        return "ver_estoque"; // ou o nome do seu template
    }



}

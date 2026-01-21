package com.syncro.controller;

import com.syncro.model.CaminhoOS;
import com.syncro.model.FotoVeiculo;
import com.syncro.model.StatusVeiculo;
import com.syncro.model.Veiculo;
import com.syncro.repository.CaminhoOSRepository;
import com.syncro.repository.FotoVeiculoRepository;
import com.syncro.repository.PecaRepository;
import com.syncro.repository.VeiculoRepository;
import com.syncro.service.FotoStorageService;
import com.syncro.service.OsStorageService;
import com.syncro.service.VeiculoService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoRepository repo;
    private final FotoStorageService storage;
    private final FotoVeiculoRepository fotoRepo;
    private final CaminhoOSRepository caminhoRepo;
    private final OsStorageService osStorage;
    private final PecaRepository pecaRepo;
    private final VeiculoRepository veiculoRepo;
    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoRepository repo,
                             FotoStorageService storage,
                             FotoVeiculoRepository fotoRepo,
                             CaminhoOSRepository caminhoRepo,
                             OsStorageService osStorage,
                             PecaRepository pecaRepo,
                             VeiculoService veiculoService,
                             VeiculoRepository veiculoRepo) {
        this.repo = repo;
        this.storage = storage;
        this.fotoRepo = fotoRepo;
        this.caminhoRepo = caminhoRepo;
        this.osStorage = osStorage;
        this.pecaRepo = pecaRepo;
        this.veiculoService = veiculoService;
        this.veiculoRepo = veiculoRepo;
    }

    @GetMapping("/registro")
    public String novoForm(Model model) {
        if (!model.containsAttribute("veiculo")) {
            model.addAttribute("veiculo", new Veiculo());
        }
        model.addAttribute("modo", "criar");
        return "registro_veiculo";
    }

    @PostMapping
    @Transactional
    public String salvar(@ModelAttribute("veiculo") Veiculo veiculo,
                         BindingResult result,
                         @RequestParam(value = "arquivos", required = false) MultipartFile[] arquivos,
                         @RequestParam(value = "arquivoOs", required = false) MultipartFile[] arquivoOs,
                         Model model,
                         RedirectAttributes ra) {


        if (veiculo.getPlaca() != null) {
            String p = veiculo.getPlaca().toUpperCase().replaceAll("[^A-Z0-9]", "");
            veiculo.setPlaca(p);
        }

        if (veiculo.getPlaca() != null && repo.existsByPlaca(veiculo.getPlaca())) {
            result.rejectValue("placa", "", "Placa já cadastrada!");
        }

        if (result.hasErrors()) {
            model.addAttribute("modo", "criar"); // mantém o template em modo criar
            return "registro_veiculo";
        }

        veiculo.setDataEntrada(LocalDateTime.now());
        veiculo.setEstatus(StatusVeiculo.ATIVO);
        repo.save(veiculo);

        if (arquivos != null) {
            for (MultipartFile arquivo : arquivos) {
                if (arquivo == null || arquivo.isEmpty()) continue;

                String caminhoRel = storage.salvarFoto(veiculo.getPlaca(), arquivo);

                var foto = new FotoVeiculo();
                foto.setPlaca(veiculo.getPlaca());
                foto.setCaminhoFoto(caminhoRel);
                fotoRepo.save(foto);
            }
        }

        if (arquivoOs != null) {
            for (MultipartFile os : arquivoOs) {
                if (os == null || os.isEmpty()) continue;
                try {
                    String caminhoRel = osStorage.salvarOs(veiculo.getPlaca(), os);

                    var cos = new CaminhoOS();
                    cos.setPlaca(veiculo.getPlaca());
                    cos.setCaminhoArquivo(caminhoRel);
                    caminhoRepo.save(cos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ra.addFlashAttribute("msgSucesso", "Veículo cadastrado com sucesso!");

        return "redirect:/veiculos/registro";
    }

    @GetMapping("/{placa}/editar")
    public String editarForm(@PathVariable String placa, Model model, RedirectAttributes ra) {
        var v = repo.findByPlaca(placa.toUpperCase()).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("msgErro", "Veículo não encontrado!");
            return "redirect:/consulta-placa";
        }
        model.addAttribute("veiculo", v);
        model.addAttribute("modo", "editar");
        return "registro_veiculo";
    }

    @PostMapping("/{placa}/editar")
    public String editarSalvar(@PathVariable String placa,
                               @ModelAttribute("veiculo") Veiculo veiculoAtualizado,
                               RedirectAttributes ra) {
        var v = repo.findByPlaca(placa.toUpperCase()).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("msgErro", "Veículo não encontrado!");
            return "redirect:/consulta-placa";
        }

        v.setMontadora(veiculoAtualizado.getMontadora());
        v.setModelo(veiculoAtualizado.getModelo());
        v.setAno(veiculoAtualizado.getAno());

        repo.save(v);

        ra.addFlashAttribute("msgSucesso", "Veículo atualizado com sucesso!");
        return "redirect:/veiculos/" + v.getPlaca();
    }

    @PostMapping("/{placa}/entregar")
    public String entregar(@PathVariable String placa, RedirectAttributes ra) {
        var v = repo.findByPlaca(placa.toUpperCase()).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("erro", "Veículo não encontrado!");
            return "redirect:/consulta-placa";
        }
        if (v.getEstatus() == StatusVeiculo.ENTREGUE) {
            ra.addFlashAttribute("info", "Este veículo já está ENTREGUE.");
            return "redirect:/consulta-placa";
        }
        veiculoService.veiculoBaixa(v.getPlaca());
        ra.addFlashAttribute("ok", "Veículo ENTREGUE.");
        return "redirect:/consulta-placa";
    }


    @GetMapping("/{placa}/fotos")
    public String verFotos(@PathVariable String placa, Model model) {
        var fotos = fotoRepo.findByPlaca(placa);
        model.addAttribute("placa", placa);
        model.addAttribute("fotos", fotos);
        return "veiculo/fotos";
    }

    @GetMapping("/{placa}/os")
    public String listarOs(@PathVariable String placa, Model model) {
        var osList = caminhoRepo.findByPlaca(placa);
        model.addAttribute("placa", placa);
        model.addAttribute("osList", osList);
        return "veiculo/os";
    }

    @GetMapping("/os/{id}")
    public ResponseEntity<Resource> abrirOsPorId(@PathVariable Long id) throws MalformedURLException {
        var osOpt = caminhoRepo.findById(id);
        if (osOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var caminho = osOpt.get().getCaminhoArquivo();
        var resource = new UrlResource(Paths.get(caminho).toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

}


package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.ConfiguracaoDto;
import br.com.senac.sp.pi.galarraga.backend.service.ConfiguracoesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracoesController {
    private final ConfiguracoesService service;

    public ConfiguracoesController(ConfiguracoesService service) {
        this.service = service;
    }

    @GetMapping
    public ConfiguracaoDto buscar() {
        return service.buscar();
    }

    @PutMapping
    public ConfiguracaoDto salvar(@Valid @RequestBody ConfiguracaoDto dados) {
        return service.salvar(dados);
    }
}

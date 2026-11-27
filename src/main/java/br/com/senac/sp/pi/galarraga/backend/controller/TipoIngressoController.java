package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import br.com.senac.sp.pi.galarraga.backend.service.TipoIngressoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-ingresso")
public class TipoIngressoController {
    private final TipoIngressoService service;

    public TipoIngressoController(TipoIngressoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoIngressoDto> listar(@RequestParam(required = false) Integer idEvento) {
        return service.listar(idEvento);
    }

    @GetMapping("/{id}")
    public TipoIngressoDto buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoIngressoDto criar(@Valid @RequestBody TipoIngressoEntrada dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public TipoIngressoDto editar(@PathVariable Integer id, @Valid @RequestBody TipoIngressoEntrada dados) {
        return service.editar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}

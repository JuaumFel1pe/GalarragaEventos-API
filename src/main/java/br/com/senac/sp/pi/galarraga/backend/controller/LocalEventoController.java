package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import br.com.senac.sp.pi.galarraga.backend.service.LocalEventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locais")
public class LocalEventoController {
    private final LocalEventoService service;

    public LocalEventoController(LocalEventoService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocalEventoDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public LocalEventoDto buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalEventoDto criar(@Valid @RequestBody LocalEventoEntrada dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public LocalEventoDto editar(@PathVariable Integer id, @Valid @RequestBody LocalEventoEntrada dados) {
        return service.editar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}

package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaDto;
import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import br.com.senac.sp.pi.galarraga.backend.service.CategoriaService;
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
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaDto buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDto criar(@Valid @RequestBody CategoriaEntrada dados) {
        return service.criar(dados);
    }

    @PutMapping("/{id}")
    public CategoriaDto editar(@PathVariable Integer id, @Valid @RequestBody CategoriaEntrada dados) {
        return service.editar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}

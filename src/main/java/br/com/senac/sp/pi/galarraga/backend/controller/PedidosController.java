package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.IngressoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.ValidarIngresso;
import br.com.senac.sp.pi.galarraga.backend.dto.VendaDto;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.service.PedidosService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class PedidosController {
    private final PedidosService pedidos;

    public PedidosController(PedidosService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping("/api/pedidos")
    public List<PedidoDto> meusPedidos(@AuthenticationPrincipal Usuario usuario) {
        return pedidos.listar(usuario);
    }

    @GetMapping("/api/pedidos/admin")
    public List<VendaDto> listagemAdmin() {
        return pedidos.vendas();
    }

    @GetMapping("/api/pedidos/{id}")
    public PedidoDto consultar(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        return pedidos.consultar(id, usuario);
    }

    @PostMapping("/api/pedidos")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDto criar(@AuthenticationPrincipal Usuario usuario, @Valid @RequestBody PedidoEntrada dados) {
        return pedidos.criar(usuario, dados);
    }

    @PatchMapping("/api/pedidos/{id}/cancelar")
    public PedidoDto cancelar(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuario) {
        return pedidos.cancelar(id, usuario);
    }

    @PostMapping("/api/ingressos/validar")
    public IngressoDto validar(@Valid @RequestBody ValidarIngresso dados) {
        return pedidos.validar(dados.codigoQr());
    }
}

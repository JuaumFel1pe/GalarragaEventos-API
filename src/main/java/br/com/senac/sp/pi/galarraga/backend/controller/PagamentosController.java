package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.ItemEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoEntrada;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.service.PagamentosService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagamentos/mercado-pago")
public class PagamentosController {
    private final PagamentosService pagamentos;

    public PagamentosController(PagamentosService pagamentos) {
        this.pagamentos = pagamentos;
    }

    @GetMapping("/configuracao")
    public Map<String, Boolean> configuracao() {
        return pagamentos.configuracao();
    }

    @PostMapping
    public PedidoDto iniciar(@AuthenticationPrincipal Usuario usuario, @Valid @RequestBody CheckoutEntrada dados) {
        return pagamentos.reservar(usuario, new PedidoEntrada(dados.itens()), dados.chave().toString());
    }

    @PostMapping("/{id}/sincronizar")
    public PedidoDto sincronizar(@AuthenticationPrincipal Usuario usuario, @PathVariable int id) {
        return pagamentos.consultarOuRetomar(usuario, id);
    }

    public record CheckoutEntrada(@NotNull UUID chave, @NotEmpty @Size(max = 50) List<@NotNull @Valid ItemEntrada> itens) {}
}

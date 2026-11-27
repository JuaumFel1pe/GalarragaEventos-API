package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.PedidoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoEntrada;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.Pedido;
import br.com.senac.sp.pi.galarraga.backend.model.StatusPedido;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.repository.ItemPedidoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PagamentosService {
    private final PedidoRepository pedidos;
    private final ItemPedidoRepository itens;
    private final PedidosService pedidosService;
    private final MercadoPagoClient mercadoPago;
    private final boolean modoTeste;
    private final boolean tokenConfigurado;

    public PagamentosService(PedidoRepository pedidos, ItemPedidoRepository itens, PedidosService pedidosService,
                             MercadoPagoClient mercadoPago,
                             @Value("${app.mercado-pago.teste:false}") boolean modoTeste,
                             @Value("${app.mercado-pago.token:}") String token) {
        this.pedidos = pedidos;
        this.itens = itens;
        this.pedidosService = pedidosService;
        this.mercadoPago = mercadoPago;
        this.modoTeste = modoTeste;
        this.tokenConfigurado = !token.isBlank();
    }

    public Map<String, Boolean> configuracao() {
        return Map.of("modoTeste", modoTeste, "configurado", modoTeste && tokenConfigurado);
    }

    public PedidoDto reservar(Usuario usuario, PedidoEntrada dados, String chave) {
        exigirDisponivel();
        return pedidosService.criarReserva(usuario, dados, chave);
    }

    @Transactional
    public PedidoDto consultarOuRetomar(Usuario usuario, int id) {
        exigirDisponivel();
        var pedido = pedidos.bloquear(id).orElseThrow(() -> ErrosApi.naoEncontrado("Pedido não encontrado"));
        if (!pedido.getUsuario().getId().equals(usuario.getId()) || pedido.getChavePagamento() == null) {
            throw ErrosApi.naoEncontrado("Pedido não encontrado");
        }
        if (pedido.getStatus() != StatusPedido.PENDENTE_PAGAMENTO) {
            return pedidosService.consultar(id, usuario);
        }
        if (pedido.getOrdemMercadoPago() == null) {
            criarOrdem(pedido, usuario);
            return pedidosService.consultar(id, usuario);
        }

        var ordem = mercadoPago.consultar(pedido.getOrdemMercadoPago());
        conferirOrdem(pedido, ordem);
        boolean aprovado = "processed".equals(ordem.status()) && "accredited".equals(ordem.detalhe())
                && valorIgual(pedido.getValorTotal(), ordem.pago());
        boolean cancelado = ("canceled".equals(ordem.status()) || "failed".equals(ordem.status()))
                && valorIgual(BigDecimal.ZERO, ordem.pago());
        return pedidosService.atualizarPagamento(pedido, aprovado, cancelado);
    }

    private void criarOrdem(Pedido pedido, Usuario usuario) {
        var produtos = itens.findByPedidoIdOrderById(pedido.getId()).stream()
                .map(item -> Map.<String, Object>of("title", "Ingresso #" + item.getTipoIngresso().getId(),
                        "quantity", item.getQuantidade(), "unit_price", item.getPrecoUnitario().toPlainString()))
                .toList();
        // A chave salva no pedido também evita duplicar a ordem se a resposta externa se perder.
        var ordem = mercadoPago.criar(pedido.getChavePagamento(), usuario.getEmail(),
                pedido.getValorTotal().toPlainString(), produtos, pedido.getId());
        conferirOrdem(pedido, ordem);
        if (!MercadoPagoClient.urlValida(ordem.url())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O Mercado Pago não retornou um link de teste válido.");
        }
        pedido.setOrdemMercadoPago(ordem.id());
        pedido.setUrlPagamento(ordem.url());
    }

    private void conferirOrdem(Pedido pedido, MercadoPagoClient.Ordem ordem) {
        if ((pedido.getOrdemMercadoPago() != null && !pedido.getOrdemMercadoPago().equals(ordem.id()))
                || !pedido.getChavePagamento().equals(ordem.referencia()) || !"BRL".equals(ordem.currency())
                || !valorIgual(pedido.getValorTotal(), ordem.total())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A ordem do Mercado Pago diverge do pedido local.");
        }
    }

    private boolean valorIgual(BigDecimal esperado, String valor) {
        if (valor == null) return false;
        try {
            return esperado.compareTo(new BigDecimal(valor)) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void exigirDisponivel() {
        if (!modoTeste || !tokenConfigurado) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Configure o Mercado Pago no perfil de teste.");
        }
    }
}

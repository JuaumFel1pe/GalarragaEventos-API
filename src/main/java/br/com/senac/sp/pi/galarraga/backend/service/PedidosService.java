package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.IngressoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.ItemEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.ItemPedidoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.VendaDto;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.Evento;
import br.com.senac.sp.pi.galarraga.backend.model.Ingresso;
import br.com.senac.sp.pi.galarraga.backend.model.ItemPedido;
import br.com.senac.sp.pi.galarraga.backend.model.Pedido;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import br.com.senac.sp.pi.galarraga.backend.model.StatusIngresso;
import br.com.senac.sp.pi.galarraga.backend.model.StatusPedido;
import br.com.senac.sp.pi.galarraga.backend.model.TipoIngresso;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.repository.IngressoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.ItemPedidoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.PedidoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.TipoIngressoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
@Transactional
public class PedidosService {
    private static final int LIMITE_INGRESSOS = 50;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    private final PedidoRepository pedidos;
    private final ItemPedidoRepository itens;
    private final IngressoRepository ingressos;
    private final TipoIngressoRepository tipos;
    private final EntityManager entityManager;
    private final boolean pagamentoTeste;

    public PedidosService(PedidoRepository pedidos, ItemPedidoRepository itens, IngressoRepository ingressos,
                   TipoIngressoRepository tipos, EntityManager entityManager,
                   @Value("${app.mercado-pago.teste:false}") boolean pagamentoTeste) {
        this.pedidos = pedidos;
        this.itens = itens;
        this.ingressos = ingressos;
        this.tipos = tipos;
        this.entityManager = entityManager;
        this.pagamentoTeste = pagamentoTeste;
    }

    public PedidoDto criar(Usuario usuario, PedidoEntrada dados) {
        if (pagamentoTeste) {
            throw ErrosApi.conflito("Use o checkout Mercado Pago no perfil de teste.");
        }
        return criarPedido(usuario, dados, null);
    }

    public PedidoDto criarReserva(Usuario usuario, PedidoEntrada dados, String chave) {
        var existente = pedidos.findByChavePagamento(chave).orElse(null);
        if (existente != null) {
            if (!existente.getUsuario().getId().equals(usuario.getId())) {
                throw ErrosApi.conflito("A tentativa já foi utilizada.");
            }
            return dto(existente);
        }
        return criarPedido(usuario, dados, chave);
    }

    private PedidoDto criarPedido(Usuario usuario, PedidoEntrada dados, String chave) {
        long quantidadeTotal = dados.itens().stream().mapToLong(ItemEntrada::quantidade).sum();
        if (quantidadeTotal > LIMITE_INGRESSOS) {
            throw ErrosApi.invalido("Limite de 50 ingressos por pedido");
        }
        // Usa a ordem dos IDs para bloquear o estoque em compras e cancelamentos.
        var quantidades = new TreeMap<Integer, Integer>();
        dados.itens().forEach(item -> quantidades.merge(item.idTipoIngresso(), item.quantidade(), Integer::sum));

        var pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDataPedido(LocalDateTime.now());
        // Usa BigDecimal para calcular o total com precisão decimal.
        pedido.setValorTotal(BigDecimal.ZERO.setScale(2));
        var novosItens = new ArrayList<ItemPedido>();

        for (var entrada : quantidades.entrySet()) {
            TipoIngresso tipo = bloquearTipo(entrada.getKey());
            int quantidade = entrada.getValue();
            Evento evento = tipo.getEvento();
            if (evento.getStatus() != StatusEvento.ATIVO || !pedido.getDataPedido().isBefore(evento.getDataFim())) {
                throw ErrosApi.conflito("Evento indisponível para compra");
            }
            if (quantidade > tipo.getQuantidadeDisponivel()) {
                throw ErrosApi.conflito("Estoque insuficiente para " + tipo.getNome());
            }
            tipo.setQuantidadeDisponivel(tipo.getQuantidadeDisponivel() - quantidade);

            var item = new ItemPedido();
            item.setPedido(pedido);
            item.setTipoIngresso(tipo);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(tipo.getPreco());
            pedido.setValorTotal(pedido.getValorTotal().add(tipo.getPreco().multiply(BigDecimal.valueOf(quantidade))));
            novosItens.add(item);
        }
        if (pedido.getValorTotal().compareTo(VALOR_MAXIMO) > 0) {
            throw ErrosApi.invalido("Valor total excede o limite do pedido");
        }
        if (chave != null) {
            if (pedido.getValorTotal().signum() <= 0) {
                throw ErrosApi.invalido("O Mercado Pago exige um valor maior que zero.");
            }
            pedido.setChavePagamento(chave);
            pedido.setStatus(StatusPedido.PENDENTE_PAGAMENTO);
            pedidos.save(pedido);
            return dto(pedido, itens.saveAll(novosItens).stream().map(item -> dtoItem(item, List.of())).toList());
        }
        // Na compra simulada, confirma o pedido e emite os ingressos na mesma operação.
        pedidos.save(pedido);
        return dto(pedido, itens.saveAll(novosItens).stream().map(this::emitir).toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> listar(Usuario usuario) {
        return pedidos.findByUsuarioIdOrderByIdDesc(usuario.getId()).stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<VendaDto> vendas() {
        return pedidos.findAllByOrderByIdDesc().stream().map(pedido -> {
            var itensDoPedido = itens.findByPedidoIdOrderById(pedido.getId());
            int quantidade = itensDoPedido.stream().mapToInt(item -> item.getQuantidade()).sum();
            boolean cancelavel = pedido.getChavePagamento() == null &&
                    pedido.getStatus() == StatusPedido.CONFIRMADO && itensDoPedido.stream()
                    .flatMap(item -> ingressos.findByItemIdOrderById(item.getId()).stream())
                    .noneMatch(ingresso -> ingresso.getStatus() == StatusIngresso.UTILIZADO);
            return new VendaDto(pedido.getId(), pedido.getUsuario().getNome(), pedido.getDataPedido(), pedido.getValorTotal(), pedido.getStatus(),
                    quantidade, cancelavel);
        }).toList();
    }

    @Transactional(readOnly = true)
    public PedidoDto consultar(Integer id, Usuario usuario) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> ErrosApi.naoEncontrado("Pedido não encontrado"));
        verificarTitular(pedido, usuario);
        return dto(pedido);
    }

    public PedidoDto cancelar(Integer id, Usuario usuario) {
        Pedido pedido = pedidos.bloquear(id).orElseThrow(() -> ErrosApi.naoEncontrado("Pedido não encontrado"));
        verificarTitular(pedido, usuario);
        // Se o pedido já foi cancelado, devolve o resultado sem repor o estoque de novo.
        if (pedido.getStatus() == StatusPedido.CANCELADO) return dto(pedido);
        if (pedido.getChavePagamento() != null) {
            throw ErrosApi.conflito("Pedidos Mercado Pago precisam ser tratados no provedor de pagamento.");
        }

        var quantidades = new TreeMap<Integer, Integer>();
        var cancelados = new ArrayList<ItemPedidoDto>();
        for (ItemPedido item : itens.findByPedidoIdOrderById(id)) {
            var ingressosDoItem = ingressos.findByItemIdOrderById(item.getId());
            if (ingressosDoItem.stream().anyMatch(ingresso -> ingresso.getStatus() == StatusIngresso.UTILIZADO)) {
                throw ErrosApi.conflito("O pedido tem ingresso já utilizado");
            }
            ingressosDoItem.forEach(ingresso -> ingresso.setStatus(StatusIngresso.CANCELADO));
            cancelados.add(dtoItem(item, ingressosDoItem));
            quantidades.merge(item.getTipoIngresso().getId(), item.getQuantidade(), Integer::sum);
        }
        reporEstoque(quantidades);
        pedido.setStatus(StatusPedido.CANCELADO);
        return dto(pedido, cancelados);
    }

    public PedidoDto atualizarPagamento(Pedido pedido, boolean aprovado, boolean cancelado) {
        var itensDoPedido = itens.findByPedidoIdOrderById(pedido.getId());
        if (aprovado) {
            // A ordem local só libera ingressos após a confirmação consultada no provedor.
            pedido.setStatus(StatusPedido.CONFIRMADO);
            return dto(pedido, itensDoPedido.stream().map(this::emitir).toList());
        }
        if (cancelado) {
            var quantidades = new TreeMap<Integer, Integer>();
            itensDoPedido.forEach(item -> quantidades.merge(
                    item.getTipoIngresso().getId(), item.getQuantidade(), Integer::sum));
            reporEstoque(quantidades);
            pedido.setStatus(StatusPedido.CANCELADO);
        }
        return dto(pedido);
    }

    private void reporEstoque(TreeMap<Integer, Integer> quantidades) {
        // Relê o estoque após o bloqueio para somar a devolução ao saldo atual.
        quantidades.forEach((idTipo, quantidade) -> {
            var tipo = bloquearTipo(idTipo);
            entityManager.refresh(tipo, LockModeType.PESSIMISTIC_WRITE);
            tipo.setQuantidadeDisponivel(tipo.getQuantidadeDisponivel() + quantidade);
        });
    }

    public IngressoDto validar(String codigoQr) {
        Integer idPedido = ingressos.idPedidoPorCodigo(codigoQr)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Ingresso não encontrado"));
        Pedido pedido = pedidos.bloquear(idPedido)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Ingresso não encontrado"));
        Ingresso ingresso = ingressos.findByCodigoQr(codigoQr)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Ingresso não encontrado"));
        if (pedido.getStatus() != StatusPedido.CONFIRMADO || ingresso.getStatus() != StatusIngresso.VALIDO) {
            throw ErrosApi.conflito("Ingresso já utilizado ou cancelado");
        }
        LocalDateTime agora = LocalDateTime.now();
        Evento evento = ingresso.getItem().getTipoIngresso().getEvento();
        if (evento.getStatus() != StatusEvento.ATIVO || agora.isBefore(evento.getDataInicio()) || !agora.isBefore(evento.getDataFim())) {
            throw ErrosApi.conflito("Check-in indisponível fora do horário do evento ativo");
        }
        // Marca o ingresso como utilizado para impedir um segundo check-in.
        ingresso.setStatus(StatusIngresso.UTILIZADO);
        ingresso.setDataUso(agora);
        return IngressoDto.de(ingresso);
    }

    private void verificarTitular(Pedido pedido, Usuario usuario) {
        if (usuario.getRole() != UserRole.ADMIN && !pedido.getUsuario().getId().equals(usuario.getId())) {
            throw ErrosApi.naoEncontrado("Pedido não encontrado");
        }
    }

    private TipoIngresso bloquearTipo(Integer id) {
        return tipos.bloquear(id).orElseThrow(() -> ErrosApi.naoEncontrado("Tipo de ingresso não encontrado"));
    }

    private ItemPedidoDto emitir(ItemPedido item) {
        var novosIngressos = new ArrayList<Ingresso>();
        for (int contador = 0; contador < item.getQuantidade(); contador++) {
            var ingresso = new Ingresso();
            ingresso.setItem(item);
            ingresso.setCodigoQr(UUID.randomUUID().toString());
            novosIngressos.add(ingresso);
        }
        return dtoItem(item, ingressos.saveAll(novosIngressos));
    }

    private PedidoDto dto(Pedido pedido) {
        return dto(pedido, itens.findByPedidoIdOrderById(pedido.getId()).stream()
                .map(item -> dtoItem(item, ingressos.findByItemIdOrderById(item.getId()))).toList());
    }

    private PedidoDto dto(Pedido pedido, List<ItemPedidoDto> itensDoPedido) {
        return new PedidoDto(pedido.getId(), pedido.getUsuario().getId(), pedido.getDataPedido(),
                pedido.getValorTotal(), pedido.getStatus(), itensDoPedido, pedido.getChavePagamento() != null,
                pedido.getStatus() == StatusPedido.PENDENTE_PAGAMENTO ? pedido.getUrlPagamento() : null);
    }

    private ItemPedidoDto dtoItem(ItemPedido item, List<Ingresso> ingressosDoItem) {
        return new ItemPedidoDto(item.getId(), item.getTipoIngresso().getId(), item.getQuantidade(), item.getPrecoUnitario(),
                ingressosDoItem.stream().map(IngressoDto::de).toList());
    }
}

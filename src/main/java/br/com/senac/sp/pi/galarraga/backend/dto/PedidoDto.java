package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDto(Integer id, Integer idUsuario, LocalDateTime dataPedido, BigDecimal valorTotal,
                 StatusPedido status, List<ItemPedidoDto> itens, boolean mercadoPago, String urlPagamento) {}

package br.com.senac.sp.pi.galarraga.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ItemPedidoDto(Integer id, Integer idTipoIngresso, int quantidade, BigDecimal precoUnitario,
                     List<IngressoDto> ingressos) {}

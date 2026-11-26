package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendaDto(Integer id, String cliente, LocalDateTime dataPedido, BigDecimal valorTotal,
                StatusPedido status, int quantidade, boolean cancelavel) {}

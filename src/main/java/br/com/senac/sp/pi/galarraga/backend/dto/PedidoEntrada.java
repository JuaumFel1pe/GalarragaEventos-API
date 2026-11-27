package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoEntrada(@NotEmpty @Size(max = 50) List<@Valid @NotNull ItemEntrada> itens) {}

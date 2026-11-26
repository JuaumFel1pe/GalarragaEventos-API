package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemEntrada(@NotNull @Positive Integer idTipoIngresso, @NotNull @Positive Integer quantidade) {}

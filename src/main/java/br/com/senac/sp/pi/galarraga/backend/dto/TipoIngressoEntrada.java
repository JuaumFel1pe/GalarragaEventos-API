package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TipoIngressoEntrada(@NotNull @Positive Integer idEvento, @NotBlank @Size(max = 50) String nome,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal preco,
        @NotNull @Positive Integer quantidadeTotal) {}

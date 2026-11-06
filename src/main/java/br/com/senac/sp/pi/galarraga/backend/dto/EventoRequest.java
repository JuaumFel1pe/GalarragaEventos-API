package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventoRequest(
        @NotBlank String urlImage,
        @NotBlank String nome,
        @NotBlank String descricao,
        @NotBlank String endereco,
        @PastOrPresent LocalDate dataHora,
        @NotNull @Positive BigDecimal preco,
        @NotNull @Positive Integer quantidade,
        @NotNull boolean isAtivo,
        @NotNull boolean isDestaque
        ) {
}

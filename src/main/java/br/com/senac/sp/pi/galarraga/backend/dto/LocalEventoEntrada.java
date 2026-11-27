package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LocalEventoEntrada(@NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 200) String endereco, @NotBlank @Size(max = 50) String cidade,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String estado, @NotNull @Positive Integer capacidade) {}

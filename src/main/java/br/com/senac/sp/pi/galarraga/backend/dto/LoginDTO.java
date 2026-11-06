package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank String login,
        @NotBlank String senha
) {
}

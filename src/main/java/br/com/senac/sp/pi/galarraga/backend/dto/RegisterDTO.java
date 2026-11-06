package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotBlank String login,
        @NotBlank String senha,
        @NotNull UserRole role
        ) {
}

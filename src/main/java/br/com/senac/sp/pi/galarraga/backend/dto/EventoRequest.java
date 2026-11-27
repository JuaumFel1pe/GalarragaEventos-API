package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventoRequest(@NotNull @Positive Integer idLocal, @NotNull @Positive Integer idCategoria,
        @NotBlank @Size(max = 150) String titulo, String descricao, @Size(max = 255) String bannerUrl,
        @NotNull LocalDateTime dataInicio, @NotNull LocalDateTime dataFim, StatusEvento status) {}

package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidarIngresso(@NotBlank @Size(max = 255) String codigoQr) {}

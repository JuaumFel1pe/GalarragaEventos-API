package br.com.senac.sp.pi.galarraga.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(@JsonAlias("login") @NotBlank @Email @Size(max = 150) String email,
                @NotBlank @Size(max = 72) String senha) {}

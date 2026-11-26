package br.com.senac.sp.pi.galarraga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaEntrada(@NotBlank @Size(max = 50) String nome, String descricao) {}

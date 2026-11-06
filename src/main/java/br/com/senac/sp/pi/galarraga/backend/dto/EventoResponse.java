package br.com.senac.sp.pi.galarraga.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventoResponse(
        Long id,
        String urlImage,
        String nome,
        String descricao,
        String endereco,
        LocalDate dataHora,
        BigDecimal preco,
        Integer quantidade,
        boolean isAtivo,
        boolean isDestaque
) {
}

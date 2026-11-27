package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.TipoIngresso;

import java.math.BigDecimal;

public record TipoIngressoDto(Integer id, Integer idEvento, String nome, BigDecimal preco, int quantidadeTotal, int quantidadeDisponivel) {
    public static TipoIngressoDto de(TipoIngresso item) {
        return new TipoIngressoDto(item.getId(), item.getEvento().getId(), item.getNome(), item.getPreco(), item.getQuantidadeTotal(), item.getQuantidadeDisponivel());
    }
}

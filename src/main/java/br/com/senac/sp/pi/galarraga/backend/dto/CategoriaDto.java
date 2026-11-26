package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.Categoria;

public record CategoriaDto(Integer id, String nome, String descricao) {
    public static CategoriaDto de(Categoria item) {
        return new CategoriaDto(item.getId(), item.getNome(), item.getDescricao());
    }
}

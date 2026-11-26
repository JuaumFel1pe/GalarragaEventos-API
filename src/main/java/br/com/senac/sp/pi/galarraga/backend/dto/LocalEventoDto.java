package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.LocalEvento;

public record LocalEventoDto(Integer id, String nome, String endereco, String cidade, String estado, int capacidade) {
    public static LocalEventoDto de(LocalEvento item) {
        return new LocalEventoDto(item.getId(), item.getNome(), item.getEndereco(), item.getCidade(), item.getEstado(), item.getCapacidade());
    }
}

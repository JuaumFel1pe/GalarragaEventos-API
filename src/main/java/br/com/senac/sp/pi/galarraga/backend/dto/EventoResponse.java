package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.Evento;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;

import java.time.LocalDateTime;

public record EventoResponse(Integer id, Integer idLocal, Integer idCategoria, String titulo, String descricao, String bannerUrl,
        LocalDateTime dataInicio, LocalDateTime dataFim, StatusEvento status) {
    public static EventoResponse de(Evento item) {
        return new EventoResponse(item.getId(), item.getLocal().getId(), item.getCategoria().getId(), item.getTitulo(), item.getDescricao(),
                item.getBannerUrl(), item.getDataInicio(), item.getDataFim(), item.getStatus());
    }
}

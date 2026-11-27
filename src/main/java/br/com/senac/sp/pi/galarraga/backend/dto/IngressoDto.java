package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.Ingresso;
import br.com.senac.sp.pi.galarraga.backend.model.StatusIngresso;

import java.time.LocalDateTime;

public record IngressoDto(Integer id, Integer idItem, String codigoQr, StatusIngresso status, LocalDateTime dataUso) {
    public static IngressoDto de(Ingresso ingresso) {
        return new IngressoDto(ingresso.getId(), ingresso.getItem().getId(), ingresso.getCodigoQr(), ingresso.getStatus(), ingresso.getDataUso());
    }
}

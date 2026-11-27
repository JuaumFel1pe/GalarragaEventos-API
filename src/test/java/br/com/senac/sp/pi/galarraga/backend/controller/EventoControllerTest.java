package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.EventoRequest;
import br.com.senac.sp.pi.galarraga.backend.model.Evento;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventoControllerTest extends TesteController {
    private static final String ROTA = "/api/eventos";

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriar() throws Exception {
        var dados = dadosEvento("Show acústico");
        enviar(post(ROTA), dados)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Show acústico"));
    }

    @Test
    void deveListarSemLogin() throws Exception {
        evento();
        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Música ao vivo"));
    }

    @Test
    void deveConsultarPorId() throws Exception {
        var salvo = evento();
        mvc.perform(get(ROTA + "/{id}", salvo.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.titulo").value("Música ao vivo"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizar() throws Exception {
        var salvo = evento();
        EventoRequest dados = dadosEvento("Novo título");
        enviar(put(ROTA + "/{id}", salvo.id()), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.titulo").value("Novo título"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveExcluir() throws Exception {
        var salvo = evento();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isNoContent());
        mvc.perform(get(ROTA + "/{id}", salvo.id())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarDadosInvalidos() throws Exception {
        enviar(post(ROTA), new EventoRequest(1, 1, "", null, null, null, null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").isString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveImpedirQueClienteExcluaRegistro() throws Exception {
        var salvo = evento();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarDataFinalAnteriorAoInicio() throws Exception {
        var dados = dadosEvento("Evento inválido");
        var invertido = new EventoRequest(dados.idLocal(), dados.idCategoria(), dados.titulo(), null, null,
                dados.dataFim(), dados.dataInicio(), StatusEvento.ATIVO);
        enviar(post(ROTA), invertido).andExpect(status().isBadRequest());
    }
}


package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LocalEventoControllerTest extends TesteController {
    private static final String ROTA = "/api/locais";

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriar() throws Exception {
        var dados = new LocalEventoEntrada("Arena", "Rua A", "São Paulo", "sp", 200);
        enviar(post(ROTA), dados)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Arena"))
                .andExpect(jsonPath("$.estado").value("SP"));
    }

    @Test
    void deveListarSemLogin() throws Exception {
        local();
        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Auditório"));
    }

    @Test
    void deveConsultarPorId() throws Exception {
        var salvo = local();
        mvc.perform(get(ROTA + "/{id}", salvo.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Auditório"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizar() throws Exception {
        var salvo = local();
        LocalEventoEntrada dados = new LocalEventoEntrada("Casa de shows", "Rua B", "São Paulo", "SP", 80);
        enviar(put(ROTA + "/{id}", salvo.id()), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Casa de shows"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveExcluir() throws Exception {
        var salvo = local();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isNoContent());
        mvc.perform(get(ROTA + "/{id}", salvo.id())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarDadosInvalidos() throws Exception {
        enviar(post(ROTA), new LocalEventoEntrada("Arena", "Rua A", "São Paulo", "XYZ", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.capacidade").isString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveImpedirQueClienteExcluaRegistro() throws Exception {
        var salvo = local();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isForbidden());
    }
}


package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoriaControllerTest extends TesteController {
    private static final String ROTA = "/api/categorias";

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriar() throws Exception {
        var dados = new CategoriaEntrada("Gastronomia", "Jantares");
        enviar(post(ROTA), dados)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Gastronomia"));
    }

    @Test
    void deveListarSemLogin() throws Exception {
        categoria();
        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Shows"));
    }

    @Test
    void deveConsultarPorId() throws Exception {
        var salvo = categoria();
        mvc.perform(get(ROTA + "/{id}", salvo.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Shows"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizar() throws Exception {
        var salvo = categoria();
        CategoriaEntrada dados = new CategoriaEntrada("Teatro", "Apresentações");
        enviar(put(ROTA + "/{id}", salvo.id()), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Teatro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveExcluir() throws Exception {
        var salvo = categoria();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isNoContent());
        mvc.perform(get(ROTA + "/{id}", salvo.id())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarDadosInvalidos() throws Exception {
        enviar(post(ROTA), new CategoriaEntrada("", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").isString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveImpedirQueClienteExcluaRegistro() throws Exception {
        var salvo = categoria();
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isForbidden());
    }
}


package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TipoIngressoControllerTest extends TesteController {
    private static final String ROTA = "/api/tipos-ingresso";

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriar() throws Exception {
        var dados = new TipoIngressoEntrada(evento().id(), "Meia", new BigDecimal("25.00"), 20);
        enviar(post(ROTA), dados)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Meia"))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(20));
    }

    @Test
    void deveListarSemLogin() throws Exception {
        var salvo = ingresso(12);
        ingresso(5);
        mvc.perform(get(ROTA).param("idEvento", salvo.idEvento().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Inteira"));
    }

    @Test
    void deveConsultarPorId() throws Exception {
        var salvo = ingresso(12);
        mvc.perform(get(ROTA + "/{id}", salvo.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Inteira"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizar() throws Exception {
        var salvo = ingresso(12);
        TipoIngressoEntrada dados = new TipoIngressoEntrada(salvo.idEvento(), "Meia promocional", new BigDecimal("20.00"), 10);
        enviar(put(ROTA + "/{id}", salvo.id()), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.id()))
                .andExpect(jsonPath("$.nome").value("Meia promocional"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveExcluir() throws Exception {
        var salvo = ingresso(12);
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isNoContent());
        mvc.perform(get(ROTA + "/{id}", salvo.id())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarDadosInvalidos() throws Exception {
        enviar(post(ROTA), new TipoIngressoEntrada(1, "Meia", new BigDecimal("-1.00"), 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.preco").isString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveImpedirQueClienteExcluaRegistro() throws Exception {
        var salvo = ingresso(12);
        mvc.perform(delete(ROTA + "/{id}", salvo.id())).andExpect(status().isForbidden());
    }
}


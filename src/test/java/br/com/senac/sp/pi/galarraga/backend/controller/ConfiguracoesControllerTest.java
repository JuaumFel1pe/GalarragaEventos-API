package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.ConfiguracaoDto;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConfiguracoesControllerTest extends TesteController {
    private static final String ROTA = "/api/configuracoes";
    private static final String EMAIL = "contato@example.test";
    private final ConfiguracaoDto dados = new ConfiguracaoDto("Rua A, 10", "11912345678",
            EMAIL, "", "https://instagram.com/galarraga_eventos", "", "");

    @Test
    void deveConsultarConfiguracoesSemLogin() throws Exception {
        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").isString())
                .andExpect(jsonPath("$.email").isString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveSalvarEConsultarConfiguracoesAtualizadas() throws Exception {
        enviar(put(ROTA), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
        mvc.perform(get(ROTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value(dados.telefone()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveImpedirQueClienteAltereConfiguracoes() throws Exception {
        enviar(put(ROTA), dados).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarEmailInvalido() throws Exception {
        var invalido = new ConfiguracaoDto("Rua A, 10", "11912345678", "email-invalido", "", "", "", "");
        enviar(put(ROTA), invalido)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").isString());
    }
}


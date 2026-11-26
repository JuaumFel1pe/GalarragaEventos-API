package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.LoginDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuariosControllerTest extends TesteController {
    private static final String EMAIL = "cliente@example.test";
    private static final String CPF = "11144477735";

    @Test
    void deveCadastrarApenasComoClienteMesmoSeEnviaramRoleAdmin() throws Exception {
        var dados = Map.of("nome", "Cliente", "email", EMAIL, "senha", SENHA, "cpf", CPF, "role", "ADMIN");
        enviar(post("/auth/register"), dados)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRejeitarEmailDuplicado() throws Exception {
        usuarios.cadastrar(cadastro(EMAIL, CPF), UserRole.USER);
        enviar(post("/auth/register"), cadastro(EMAIL, "22233344455"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarSenhaCurtaNoCadastro() throws Exception {
        var dados = new RegisterDTO("Cliente", EMAIL, "curta", CPF, null);
        enviar(post("/auth/register"), dados)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").isString());
    }

    @Test
    void deveRetornarTokenEIdentificarAdministrador() throws Exception {
        String token = autenticar(EMAIL, CPF, UserRole.ADMIN);
        mvc.perform(comToken(get("/api/usuarios/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deveRejeitarSenhaIncorreta() throws Exception {
        usuarios.cadastrar(cadastro(EMAIL, CPF), UserRole.USER);
        enviar(post("/auth/login"), new LoginDTO(EMAIL, "incorreta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveExigirTokenParaConsultarPerfil() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
        mvc.perform(comToken(get("/api/usuarios/me"), "invalido")).andExpect(status().isUnauthorized());
    }
}


package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaDto;
import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.EventoRequest;
import br.com.senac.sp.pi.galarraga.backend.dto.EventoResponse;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.LoginDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.service.AutenticacaoService;
import br.com.senac.sp.pi.galarraga.backend.service.CategoriaService;
import br.com.senac.sp.pi.galarraga.backend.service.EventoService;
import br.com.senac.sp.pi.galarraga.backend.service.LocalEventoService;
import br.com.senac.sp.pi.galarraga.backend.service.TipoIngressoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {"app.admin.email=", "app.admin.senha=", "app.admin.cpf=", "app.demo.catalogo=false"})
@AutoConfigureMockMvc
@Transactional
abstract class TesteController {
    static final String SENHA = "SenhaDeTeste123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CategoriaService categorias;
    @Autowired LocalEventoService locais;
    @Autowired EventoService eventos;
    @Autowired TipoIngressoService tipos;
    @Autowired AutenticacaoService usuarios;

    ResultActions enviar(MockHttpServletRequestBuilder requisicao, Object corpo) throws Exception {
        return mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(corpo)));
    }

    JsonNode resposta(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsByteArray());
    }

    CategoriaDto categoria() {
        return categorias.criar(new CategoriaEntrada("Shows", null));
    }

    LocalEventoDto local() {
        return locais.criar(new LocalEventoEntrada("Auditório", "Rua A, 100", "São Paulo", "SP", 120));
    }

    EventoRequest dadosEvento(String titulo) {
        var agora = LocalDateTime.now().withNano(0);
        return new EventoRequest(local().id(), categoria().id(), titulo, null, null,
                agora.minusHours(1), agora.plusHours(2), StatusEvento.ATIVO);
    }

    EventoResponse evento() {
        return eventos.criar(dadosEvento("Música ao vivo"));
    }

    TipoIngressoDto ingresso(int estoque) {
        return tipos.criar(new TipoIngressoEntrada(evento().id(), "Inteira", new BigDecimal("45.50"), estoque));
    }

    RegisterDTO cadastro(String email, String cpf) {
        return new RegisterDTO("Pessoa Teste", email, SENHA, cpf, null);
    }

    String autenticar(String email, String cpf, UserRole role) throws Exception {
        usuarios.cadastrar(cadastro(email, cpf), role);
        return resposta(enviar(post("/auth/login"), new LoginDTO(email, SENHA))
                .andExpect(status().isOk())).get("token").asText();
    }

    MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header("Authorization", "Bearer " + token);
    }
}


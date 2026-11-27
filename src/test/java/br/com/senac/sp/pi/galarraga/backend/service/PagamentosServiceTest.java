package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.EventoRequest;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.LoginDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.mercado-pago.teste=true", "app.mercado-pago.token=token-ficticio",
        "spring.datasource.url=jdbc:h2:mem:galarraga-pagamento-tests", "app.admin.email=", "app.admin.senha=",
        "app.admin.cpf=", "app.demo.catalogo=false"})
@AutoConfigureMockMvc
@Transactional
class PagamentosServiceTest {
    private static final String EMAIL = "teste@example.test";
    private static final String URL = "https://www.mercadopago.com.br/checkout/v1/redirect?order_id=ORDTST123";
    private static final String API = "/api/pagamentos/mercado-pago";
    private final String chave = UUID.randomUUID().toString();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AutenticacaoService autenticacao;
    @Autowired CategoriaService categorias;
    @Autowired LocalEventoService locais;
    @Autowired EventoService eventos;
    @Autowired TipoIngressoService ingressos;

    @MockitoBean MercadoPagoClient mercadoPago;

    private String token;
    private int ingresso;

    @BeforeEach
    void preparar() {
        autenticacao.cadastrar(new RegisterDTO("Cliente", EMAIL, "SenhaDeTeste123", "11144477735", null), UserRole.USER);
        token = autenticacao.login(new LoginDTO(EMAIL, "SenhaDeTeste123"));
        var local = locais.criar(new LocalEventoEntrada("Auditório", "Rua A", "São Paulo", "SP", 100));
        var categoria = categorias.criar(new CategoriaEntrada("Música", null));
        var agora = LocalDateTime.now();
        var evento = eventos.criar(new EventoRequest(local.id(), categoria.id(), "Show", null, null,
                agora.minusHours(1), agora.plusHours(2), StatusEvento.ATIVO));
        ingresso = ingressos.criar(new TipoIngressoEntrada(evento.id(), "Inteira", new BigDecimal("45.50"), 10)).id();
    }

    @Test
    void deveReusarReservaEEmitirIngressosUmaVezDepoisDeAprovado() throws Exception {
        int id = reservar();
        iniciar().andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        when(mercadoPago.criar(eq(chave), eq(EMAIL), eq("91.00"), anyList(), eq(id)))
                .thenReturn(ordem("created", "created", "0.00"));
        sincronizar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.urlPagamento").value(URL))
                .andExpect(jsonPath("$.itens[0].ingressos").isEmpty());
        when(mercadoPago.consultar("ORDTST123")).thenReturn(ordem("processed", "accredited", "91.00"));
        sincronizar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.itens[0].ingressos.length()").value(2));
        sincronizar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].ingressos.length()").value(2));
        verify(mercadoPago, times(1)).consultar("ORDTST123");
        estoque(8);
    }

    @Test
    void deveReporReservaSomenteAposCancelamentoNoMercadoPago() throws Exception {
        int id = reservar();
        when(mercadoPago.criar(eq(chave), eq(EMAIL), eq("91.00"), anyList(), eq(id)))
                .thenReturn(ordem("created", "created", "0.00"));
        sincronizar(id).andExpect(status().isOk());
        when(mercadoPago.consultar("ORDTST123")).thenReturn(ordem("canceled", "canceled", "0.00"));
        sincronizar(id).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        estoque(10);
    }

    @Test
    void deveImpedirEmissaoSeValorPagoForDiferente() throws Exception {
        int id = reservar();
        when(mercadoPago.criar(eq(chave), eq(EMAIL), eq("91.00"), anyList(), eq(id)))
                .thenReturn(ordem("created", "created", "0.00"));
        sincronizar(id).andExpect(status().isOk());
        when(mercadoPago.consultar("ORDTST123")).thenReturn(ordem("processed", "accredited", "22.00"));
        sincronizar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_PAGAMENTO"))
                .andExpect(jsonPath("$.itens[0].ingressos").isEmpty());
        estoque(8);
    }

    private int reservar() throws Exception {
        var corpo = iniciar().andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_PAGAMENTO"))
                .andReturn().getResponse().getContentAsByteArray();
        return json.readTree(corpo).get("id").asInt();
    }

    private ResultActions iniciar() throws Exception {
        var dados = Map.of("chave", chave, "itens", List.of(Map.of("idTipoIngresso", ingresso, "quantidade", 2)));
        return mvc.perform(post(API).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(dados)));
    }

    private ResultActions sincronizar(int id) throws Exception {
        return mvc.perform(post(API + "/{id}/sincronizar", id).header("Authorization", "Bearer " + token));
    }

    private void estoque(int unidades) throws Exception {
        mvc.perform(get("/api/tipos-ingresso/{id}", ingresso))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(unidades));
    }

    private MercadoPagoClient.Ordem ordem(String status, String detalhe, String pago) {
        return new MercadoPagoClient.Ordem("ORDTST123", status, "BRL", detalhe, chave, "91.00", pago, URL);
    }
}

package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.ItemEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.PedidoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.ValidarIngresso;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tools.jackson.databind.JsonNode;

class PedidosControllerTest extends TesteController {
    private static final String ROTA = "/api/pedidos";
    private static final String VALIDAR = "/api/ingressos/validar";

    @Test
    void deveConfirmarPedidoCalcularTotalEmitirIngressosEBaixarEstoque() throws Exception {
        String token = cliente();
        var tipo = ingresso(5);

        enviar(comToken(post(ROTA), token), compra(tipo.id(), 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.valorTotal").value(91.0))
                .andExpect(jsonPath("$.itens[0].ingressos.length()").value(2))
                .andExpect(jsonPath("$.itens[0].ingressos[0].codigoQr").isString());

        deveTerEstoque(tipo.id(), 3);
    }

    @Test
    void deveListarEConsultarApenasPedidosDoCliente() throws Exception {
        String cliente = cliente();
        String outro = autenticar("outro@example.test", "22233344455", UserRole.USER);
        int id = criarPedido(cliente, ingresso(5).id()).get("id").asInt();

        mvc.perform(comToken(get(ROTA), cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(comToken(get(ROTA), outro))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(comToken(get(ROTA + "/{id}", id), cliente)).andExpect(status().isOk());
        mvc.perform(comToken(get(ROTA + "/{id}", id), outro)).andExpect(status().isNotFound());
    }

    @Test
    void devePermitirQueApenasAdminListeVendas() throws Exception {
        String cliente = cliente();
        int id = criarPedido(cliente, ingresso(3).id()).get("id").asInt();

        mvc.perform(comToken(get(ROTA + "/admin"), cliente)).andExpect(status().isForbidden());
        mvc.perform(comToken(get(ROTA + "/admin"), admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].quantidade").value(1));
    }

    @Test
    void deveCancelarEReporEstoqueApenasUmaVez() throws Exception {
        String token = cliente();
        var tipo = ingresso(5);
        int id = criarPedido(token, tipo.id()).get("id").asInt();

        mvc.perform(comToken(patch(ROTA + "/{id}/cancelar", id), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.itens[0].ingressos[0].status").value("CANCELADO"));
        mvc.perform(comToken(patch(ROTA + "/{id}/cancelar", id), token)).andExpect(status().isOk());

        deveTerEstoque(tipo.id(), 5);
    }

    @Test
    void deveRegistrarCheckInApenasUmaVezEImpedirCancelamentoDepois() throws Exception {
        String cliente = cliente();
        String admin = admin();
        var pedido = criarPedido(cliente, ingresso(5).id());
        var dados = new ValidarIngresso(pedido.at("/itens/0/ingressos/0/codigoQr").asText());

        enviar(comToken(post(VALIDAR), cliente), dados).andExpect(status().isForbidden());
        enviar(comToken(post(VALIDAR), admin), dados)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UTILIZADO"))
                .andExpect(jsonPath("$.dataUso").isString());
        enviar(comToken(post(VALIDAR), admin), dados).andExpect(status().isConflict());
        mvc.perform(comToken(patch(ROTA + "/{id}/cancelar", pedido.get("id").asInt()), cliente))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarCompraAcimaDoEstoque() throws Exception {
        String token = cliente();
        var tipo = ingresso(1);

        enviar(comToken(post(ROTA), token), compra(tipo.id(), 2)).andExpect(status().isConflict());
        deveTerEstoque(tipo.id(), 1);
    }

    @Test
    void deveExigirAutenticacaoParaComprar() throws Exception {
        enviar(post(ROTA), compra(1, 1)).andExpect(status().isUnauthorized());
    }

    private PedidoEntrada compra(int idTipo, int quantidade) {
        return new PedidoEntrada(List.of(new ItemEntrada(idTipo, quantidade)));
    }

    private JsonNode criarPedido(String token, int idTipo) throws Exception {
        return resposta(enviar(comToken(post(ROTA), token), compra(idTipo, 1))
                .andExpect(status().isCreated()));
    }

    private void deveTerEstoque(int idTipo, int quantidade) throws Exception {
        mvc.perform(get("/api/tipos-ingresso/{id}", idTipo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeDisponivel").value(quantidade));
    }

    private String cliente() throws Exception {
        return autenticar("cliente@example.test", "11144477735", UserRole.USER);
    }

    private String admin() throws Exception {
        return autenticar("admin@example.test", "12345678909", UserRole.ADMIN);
    }
}


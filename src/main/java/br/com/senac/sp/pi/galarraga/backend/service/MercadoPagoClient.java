package br.com.senac.sp.pi.galarraga.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoClient {
    private final RestClient http;
    private final String retorno;

    public MercadoPagoClient(@Value("${app.mercado-pago.token:}") String token,
                             @Value("${app.mercado-pago.retorno:}") String retorno) {
        var cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().baseUrl("https://api.mercadopago.com")
                .requestFactory(fabrica).defaultHeaders(headers -> headers.setBearerAuth(token)).build();
        this.retorno = retorno;
    }

    Ordem criar(String chave, String email, String total, List<Map<String, Object>> itens, int idPedido) {
        var dados = new java.util.LinkedHashMap<String, Object>();
        dados.put("type", "online");
        dados.put("processing_mode", "manual");
        dados.put("total_amount", total);
        dados.put("external_reference", chave);
        dados.put("payer", Map.of("email", email));
        dados.put("items", itens);

        var online = new java.util.LinkedHashMap<String, Object>();
        online.put("retries", Map.of("allowed", false));
        if (!retorno.isBlank()) {
            String perfil = retorno.replaceAll("/+$", "") + "/perfil?pedido=" + idPedido;
            online.put("success_url", perfil);
            online.put("failure_url", perfil);
            online.put("pending_url", perfil);
            online.put("auto_return", "all");
        }
        dados.put("config", Map.of("online", online));
        try {
            return conferirTeste(http.post().uri("/v1/orders").header("X-Idempotency-Key", chave)
                    .body(dados).retrieve().body(Ordem.class));
        } catch (RestClientException e) {
            throw indisponivel();
        }
    }

    Ordem consultar(String id) {
        try {
            return conferirTeste(http.get().uri("/v1/orders/{id}", id).retrieve().body(Ordem.class));
        } catch (RestClientException e) {
            throw indisponivel();
        }
    }

    static boolean urlValida(String url) {
        if (url == null) return false;
        try {
            URI uri = URI.create(url);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getUserInfo() == null &&
                    ("www.mercadopago.com.br".equalsIgnoreCase(uri.getHost()) ||
                     "mercadopago.com.br".equalsIgnoreCase(uri.getHost()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Ordem conferirTeste(Ordem ordem) {
        if (ordem == null || ordem.id() == null || !ordem.id().startsWith("ORDTST")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "A API retornou uma ordem fora do ambiente de teste. Confira as credenciais.");
        }
        return ordem;
    }

    private ResponseStatusException indisponivel() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "O Mercado Pago não respondeu como esperado. Confira o token de teste e retome em Meus pedidos.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Ordem(String id, String status, String currency,
                 @JsonProperty("status_detail") String detalhe,
                 @JsonProperty("external_reference") String referencia,
                 @JsonProperty("total_amount") String total,
                 @JsonProperty("total_paid_amount") String pago,
                 @JsonProperty("checkout_url") String url) {}
}

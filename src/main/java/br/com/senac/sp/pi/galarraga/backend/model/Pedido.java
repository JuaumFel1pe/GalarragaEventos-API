package br.com.senac.sp.pi.galarraga.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    @Column(nullable = false)
    private LocalDateTime dataPedido;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.CONFIRMADO;

    @Column(unique = true, length = 36)
    private String chavePagamento;
    @Column(length = 80)
    private String ordemMercadoPago;
    @Column(length = 1500)
    private String urlPagamento;

    public String getChavePagamento() { return chavePagamento; }
    public void setChavePagamento(String chavePagamento) { this.chavePagamento = chavePagamento; }
    public String getOrdemMercadoPago() { return ordemMercadoPago; }
    public void setOrdemMercadoPago(String ordemMercadoPago) { this.ordemMercadoPago = ordemMercadoPago; }
    public String getUrlPagamento() { return urlPagamento; }
    public void setUrlPagamento(String urlPagamento) { this.urlPagamento = urlPagamento; }

    public Integer getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}

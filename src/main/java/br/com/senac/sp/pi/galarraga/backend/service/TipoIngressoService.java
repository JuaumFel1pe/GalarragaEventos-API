package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.TipoIngresso;
import br.com.senac.sp.pi.galarraga.backend.repository.EventoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.TipoIngressoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoIngressoService {
    private final TipoIngressoRepository tipos;
    private final EventoRepository eventos;

    public TipoIngressoService(TipoIngressoRepository tipos, EventoRepository eventos) {
        this.tipos = tipos;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<TipoIngressoDto> listar(Integer idEvento) {
        if (idEvento != null && !eventos.existsById(idEvento)) {
            throw ErrosApi.naoEncontrado("Evento não encontrado");
        }
        var encontrados = idEvento == null ? tipos.findAll() : tipos.findByEventoIdOrderById(idEvento);
        return encontrados.stream().map(TipoIngressoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public TipoIngressoDto buscar(Integer id) {
        return TipoIngressoDto.de(tipos.findById(id).orElseThrow(() -> ErrosApi.naoEncontrado("Tipo de ingresso não encontrado")));
    }

    public TipoIngressoDto criar(TipoIngressoEntrada dto) {
        var item = new TipoIngresso();
        preencher(item, dto);
        return TipoIngressoDto.de(tipos.save(item));
    }

    public TipoIngressoDto editar(Integer id, TipoIngressoEntrada dto) {
        var item = buscarParaAlterar(id);
        preencher(item, dto);
        return TipoIngressoDto.de(item);
    }

    public void excluir(Integer id) {
        tipos.delete(buscarParaAlterar(id));
    }

    private TipoIngresso buscarParaAlterar(Integer id) {
        return tipos.bloquear(id)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Tipo de ingresso não encontrado"));
    }

    private void preencher(TipoIngresso item, TipoIngressoEntrada dto) {
        if (item.getId() != null && !item.getEvento().getId().equals(dto.idEvento())) {
            throw ErrosApi.conflito("O evento de um tipo de ingresso não pode ser alterado");
        }
        // Ao ajustar o total, desconta do novo estoque os ingressos já vendidos.
        int vendidos = item.getQuantidadeTotal() - item.getQuantidadeDisponivel();
        if (dto.quantidadeTotal() < vendidos) {
            throw ErrosApi.conflito("Quantidade total menor que a já vendida");
        }

        item.setEvento(eventos.findById(dto.idEvento()).orElseThrow(() -> ErrosApi.naoEncontrado("Evento não encontrado")));
        item.setNome(dto.nome().trim());
        item.setPreco(dto.preco());
        item.setQuantidadeTotal(dto.quantidadeTotal());
        item.setQuantidadeDisponivel(dto.quantidadeTotal() - vendidos);
    }
}

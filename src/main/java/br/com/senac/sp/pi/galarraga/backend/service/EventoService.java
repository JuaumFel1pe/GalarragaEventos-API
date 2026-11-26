package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.EventoRequest;
import br.com.senac.sp.pi.galarraga.backend.dto.EventoResponse;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.Categoria;
import br.com.senac.sp.pi.galarraga.backend.model.Evento;
import br.com.senac.sp.pi.galarraga.backend.repository.CategoriaRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.EventoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.LocalEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EventoService {
    private final EventoRepository eventos;
    private final LocalEventoRepository locais;
    private final CategoriaRepository categorias;

    public EventoService(EventoRepository eventos, LocalEventoRepository locais, CategoriaRepository categorias) {
        this.eventos = eventos;
        this.locais = locais;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventos.findAll().stream().map(EventoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscar(Integer id) {
        return EventoResponse.de(eventos.findById(id).orElseThrow(() -> ErrosApi.naoEncontrado("Evento não encontrado")));
    }

    public EventoResponse criar(EventoRequest dto) {
        var item = new Evento();
        preencher(item, dto);
        return EventoResponse.de(eventos.save(item));
    }

    public EventoResponse editar(Integer id, EventoRequest dto) {
        var item = buscarParaAlterar(id);
        preencher(item, dto);
        return EventoResponse.de(item);
    }

    public void excluir(Integer id) {
        eventos.delete(buscarParaAlterar(id));
    }

    private Evento buscarParaAlterar(Integer id) {
        return eventos.findById(id)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Evento não encontrado"));
    }

    private void preencher(Evento item, EventoRequest dto) {
        if (!dto.dataFim().isAfter(dto.dataInicio())) {
            throw ErrosApi.invalido("A data final deve ser posterior à inicial");
        }
        if (dto.bannerUrl() != null && !dto.bannerUrl().isBlank()
                && !dto.bannerUrl().startsWith("https://") && !dto.bannerUrl().startsWith("http://")
                && (!dto.bannerUrl().startsWith("/") || dto.bannerUrl().startsWith("//"))) {
            throw ErrosApi.invalido("Informe uma URL http/https ou um caminho local para o banner");
        }

        item.setLocal(locais.findById(dto.idLocal()).orElseThrow(() -> ErrosApi.naoEncontrado("Local não encontrado")));
        item.setCategoria(categorias.findById(dto.idCategoria()).orElseThrow(() -> ErrosApi.naoEncontrado("Categoria não encontrada")));
        item.setTitulo(dto.titulo().trim());
        item.setDescricao(dto.descricao());
        item.setBannerUrl(dto.bannerUrl());
        item.setDataInicio(dto.dataInicio());
        item.setDataFim(dto.dataFim());
        if (dto.status() != null) item.setStatus(dto.status());
    }
}

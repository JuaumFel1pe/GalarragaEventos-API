package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoDto;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.LocalEvento;
import br.com.senac.sp.pi.galarraga.backend.repository.LocalEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class LocalEventoService {
    private final LocalEventoRepository locais;

    public LocalEventoService(LocalEventoRepository locais) {
        this.locais = locais;
    }

    @Transactional(readOnly = true)
    public List<LocalEventoDto> listar() {
        return locais.findAll().stream().map(LocalEventoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public LocalEventoDto buscar(Integer id) {
        return LocalEventoDto.de(locais.findById(id).orElseThrow(() -> ErrosApi.naoEncontrado("Local não encontrado")));
    }

    public LocalEventoDto criar(LocalEventoEntrada dto) {
        var item = new LocalEvento();
        preencher(item, dto);
        return LocalEventoDto.de(locais.save(item));
    }

    public LocalEventoDto editar(Integer id, LocalEventoEntrada dto) {
        var item = buscarParaAlterar(id);
        preencher(item, dto);
        return LocalEventoDto.de(item);
    }

    public void excluir(Integer id) {
        locais.delete(buscarParaAlterar(id));
    }

    private LocalEvento buscarParaAlterar(Integer id) {
        return locais.findById(id)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Local não encontrado"));
    }

    private void preencher(LocalEvento item, LocalEventoEntrada dto) {
        item.setNome(dto.nome().trim());
        item.setEndereco(dto.endereco().trim());
        item.setCidade(dto.cidade().trim());
        item.setEstado(dto.estado().toUpperCase(Locale.ROOT));
        item.setCapacidade(dto.capacidade());
    }
}

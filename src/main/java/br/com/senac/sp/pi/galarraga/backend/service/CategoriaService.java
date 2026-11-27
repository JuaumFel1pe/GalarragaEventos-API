package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaDto;
import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.Categoria;
import br.com.senac.sp.pi.galarraga.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar() {
        return categorias.findAll().stream().map(CategoriaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDto buscar(Integer id) {
        return CategoriaDto.de(categorias.findById(id).orElseThrow(() -> ErrosApi.naoEncontrado("Categoria não encontrada")));
    }

    public CategoriaDto criar(CategoriaEntrada dto) {
        var item = new Categoria();
        preencher(item, dto);
        return CategoriaDto.de(categorias.save(item));
    }

    public CategoriaDto editar(Integer id, CategoriaEntrada dto) {
        var item = buscarParaAlterar(id);
        preencher(item, dto);
        return CategoriaDto.de(item);
    }

    public void excluir(Integer id) {
        categorias.delete(buscarParaAlterar(id));
    }

    private Categoria buscarParaAlterar(Integer id) {
        return categorias.findById(id)
                .orElseThrow(() -> ErrosApi.naoEncontrado("Categoria não encontrada"));
    }

    private void preencher(Categoria item, CategoriaEntrada dto) {
        item.setNome(dto.nome().trim());
        item.setDescricao(dto.descricao());
    }
}

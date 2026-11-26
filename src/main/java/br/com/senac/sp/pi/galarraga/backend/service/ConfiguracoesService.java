package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.ConfiguracaoDto;
import br.com.senac.sp.pi.galarraga.backend.model.Configuracao;
import br.com.senac.sp.pi.galarraga.backend.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracoesService {
    private final ConfiguracaoRepository configuracoes;

    public ConfiguracoesService(ConfiguracaoRepository configuracoes) { this.configuracoes = configuracoes; }

    @Transactional(readOnly = true)
    public ConfiguracaoDto buscar() {
        return configuracoes.findById(1).map(ConfiguracaoDto::de)
                .orElseGet(() -> new ConfiguracaoDto("Av. das Nações Unidas, 5.777, São Paulo - SP",
                        "+55 11 98323-7642", "eventos.galarraga@veneven.com", "https://facebook.com/galarraga_eventos",
                        "https://instagram.com/galarraga_eventos", "https://linkedin.com/galarraga_eventos",
                        "https://twitter.com/galarraga_eventos"));
    }

    @Transactional
    public ConfiguracaoDto salvar(ConfiguracaoDto dados) {
        var config = configuracoes.findById(1).orElseGet(Configuracao::new);
        config.setId(1);
        config.setEndereco(dados.endereco().trim());
        config.setTelefone(dados.telefone().trim());
        config.setEmail(dados.email().trim());
        config.setFacebook(dados.facebook());
        config.setInstagram(dados.instagram());
        config.setLinkedin(dados.linkedin());
        config.setTwitter(dados.twitter());
        return ConfiguracaoDto.de(configuracoes.save(config));
    }
}

package br.com.senac.sp.pi.galarraga.backend.config;

import br.com.senac.sp.pi.galarraga.backend.dto.CategoriaEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.EventoRequest;
import br.com.senac.sp.pi.galarraga.backend.dto.LocalEventoEntrada;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.TipoIngressoEntrada;
import br.com.senac.sp.pi.galarraga.backend.model.StatusEvento;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.repository.EventoRepository;
import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;
import br.com.senac.sp.pi.galarraga.backend.service.AutenticacaoService;
import br.com.senac.sp.pi.galarraga.backend.service.CategoriaService;
import br.com.senac.sp.pi.galarraga.backend.service.EventoService;
import br.com.senac.sp.pi.galarraga.backend.service.LocalEventoService;
import br.com.senac.sp.pi.galarraga.backend.service.TipoIngressoService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DadosIniciais {
    @Bean
    ApplicationRunner administrador(UsuarioRepository repositorio, AutenticacaoService service, Validator validator,
            @Value("${app.admin.email:}") String email, @Value("${app.admin.senha:}") String senha,
            @Value("${app.admin.cpf:}") String cpf) {
        return args -> {
            if (email.isBlank() && senha.isBlank() && cpf.isBlank()) return;
            var detalhes = new RegisterDTO("Administrador", email, senha, cpf, null);
            if (!validator.validate(detalhes).isEmpty()) {
                throw new IllegalStateException("Confira ADMIN_EMAIL, ADMIN_SENHA e ADMIN_CPF");
            }
            var existente = repositorio.findByEmail(AutenticacaoService.normalizarEmail(email));
            // Cria o administrador só se a conta ainda não existir.
            if (existente.isEmpty()) {
                service.cadastrar(detalhes, UserRole.ADMIN);
            } else if (existente.get().getRole() != UserRole.ADMIN) {
                throw new IllegalStateException("O e-mail de administração já pertence a um cliente");
            }
        };
    }

    @Bean
    ApplicationRunner demonstracao(@Value("${app.demo.catalogo:false}") boolean habilitada,
            EventoRepository eventos, CategoriaService categorias, LocalEventoService locais,
            EventoService cadastroEvento, TipoIngressoService ingressos) {
        return args -> {
            if (!habilitada || eventos.count() > 0) return;
            var gastronomia = categorias.criar(new CategoriaEntrada("Gastronomia", "Encontros e experiências gastronômicas."));
            var musica = categorias.criar(new CategoriaEntrada("Música", "Shows e apresentações ao vivo."));
            var local = locais.criar(new LocalEventoEntrada("Espaço Galarraga", "Av. das Nações Unidas, 5.777",
                    "São Paulo", "SP", 180));
            var agora = LocalDateTime.now().withSecond(0).withNano(0);
            var jantar = cadastroEvento.criar(new EventoRequest(local.id(), gastronomia.id(), "Jantar Galarraga",
                    "Uma noite de gastronomia em São Paulo. Confira os ingressos disponíveis e reserve seu lugar.",
                    "/restaurantegalarraga.jpeg", agora.plusDays(7).withHour(19), agora.plusDays(7).withHour(22), StatusEvento.ATIVO));
            var show = cadastroEvento.criar(new EventoRequest(local.id(), musica.id(), "Música ao vivo",
                    "Uma apresentação para reunir amigos. Este evento está em andamento para testar o check-in.",
                    "/fundo_eventos_disponiveis.png", agora.minusHours(1), agora.plusDays(1), StatusEvento.ATIVO));
            ingressos.criar(new TipoIngressoEntrada(jantar.id(), "Mesa individual", new BigDecimal("95.00"), 80));
            ingressos.criar(new TipoIngressoEntrada(show.id(), "Inteira", new BigDecimal("50.00"), 60));
            ingressos.criar(new TipoIngressoEntrada(show.id(), "Meia", new BigDecimal("25.00"), 40));
        };
    }
}

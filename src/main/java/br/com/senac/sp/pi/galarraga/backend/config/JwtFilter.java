package br.com.senac.sp.pi.galarraga.backend.config;

import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;
import br.com.senac.sp.pi.galarraga.backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final UsuarioRepository usuarios;

    public JwtFilter(TokenService tokens, UsuarioRepository usuarios) {
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain cadeia)
            throws ServletException, IOException {
        String cabecalho = req.getHeader("Authorization");
        if (cabecalho != null && cabecalho.regionMatches(true, 0, "Bearer ", 0, 7)) {
            // Valida o token e busca no banco as permissões atuais do usuário.
            tokens.verificar(cabecalho.substring(7)).flatMap(usuarios::findByEmail)
                    .filter(usuario -> usuario.getAtivo()).ifPresent(usuario -> {
                        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
                        var autenticacao = UsernamePasswordAuthenticationToken.authenticated(usuario, null, autoridades);
                        SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    });
        }
        cadeia.doFilter(req, res);
    }
}

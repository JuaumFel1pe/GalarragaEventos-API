package br.com.senac.sp.pi.galarraga.backend.config;

import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;
import br.com.senac.sp.pi.galarraga.backend.service.TokenService;
import com.auth0.jwt.JWT;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfigurations {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain rotas(HttpSecurity http, TokenService tokens, UsuarioRepository usuarios) throws Exception {
        String[] catalogo = {"/api/eventos/**", "/api/categorias/**", "/api/locais/**", "/api/tipos-ingresso/**",
                "/api/configuracoes/**"};
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((req, res, erro) -> res.sendError(401))
                        .accessDeniedHandler((req, res, erro) -> res.sendError(403)))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, catalogo).permitAll()
                        .requestMatchers(catalogo).hasRole("ADMIN")
                        .requestMatchers("/api/pedidos/admin", "/api/ingressos/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(tokens, usuarios), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

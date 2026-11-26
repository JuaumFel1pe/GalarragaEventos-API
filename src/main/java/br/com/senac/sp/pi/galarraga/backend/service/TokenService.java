package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {
    private static final String EMISSOR = "galarraga-eventos";
    private final Algorithm assinatura;
    private final JWTVerifier verificador;

    public TokenService(@Value("${api.security.token.secret}") String segredo) {
        assinatura = Algorithm.HMAC256(segredo);
        verificador = JWT.require(assinatura).withIssuer(EMISSOR).withClaimPresence("exp").build();
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return JWT.create().withIssuer(EMISSOR).withSubject(usuario.getEmail())
                .withIssuedAt(agora).withExpiresAt(agora.plusSeconds(7200)).sign(assinatura);
    }

    public Optional<String> verificar(String token) {
        try {
            // Verifica a assinatura e a validade do JWT antes de obter o e-mail.
            return Optional.ofNullable(verificador.verify(token).getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }
}

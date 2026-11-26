package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.LoginDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.exception.ErrosApi;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final TokenService tokens;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder senhas, TokenService tokens) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
    }

    @Transactional
    public Usuario cadastrar(RegisterDTO dados, UserRole role) {
        String email = normalizarEmail(dados.email());
        String cpf = dados.cpf().replaceAll("[.-]", "");
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ErrosApi.invalido("A senha deve ter até 72 bytes");
        }
        if (usuarios.existsByEmail(email) || usuarios.existsByCpf(cpf)) {
            throw ErrosApi.conflito("E-mail ou CPF já cadastrado");
        }

        var usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        // Guarda o hash BCrypt no banco para conferir a senha no login.
        usuario.setSenhaHash(senhas.encode(dados.senha()));
        usuario.setTelefone(dados.telefone() == null || dados.telefone().isBlank() ? null : dados.telefone().trim());
        usuario.setRole(role);
        usuario.setDataCadastro(LocalDateTime.now());
        return usuarios.save(usuario);
    }

    public String login(LoginDTO dados) {
        var usuario = usuarios.findByEmail(normalizarEmail(dados.email())).orElse(null);
        if (usuario == null || !usuario.getAtivo() || dados.senha().getBytes(StandardCharsets.UTF_8).length > 72
                || !senhas.matches(dados.senha(), usuario.getSenhaHash())) {
            throw ErrosApi.naoAutorizado("E-mail ou senha inválidos");
        }
        // Se a senha estiver correta, gera o JWT que o cliente usa nas próximas requisições.
        return tokens.gerar(usuario);
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

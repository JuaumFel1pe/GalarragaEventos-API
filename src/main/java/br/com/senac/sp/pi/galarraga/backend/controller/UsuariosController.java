package br.com.senac.sp.pi.galarraga.backend.controller;

import br.com.senac.sp.pi.galarraga.backend.dto.LoginDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.TokenDTO;
import br.com.senac.sp.pi.galarraga.backend.dto.UsuarioResponseDTO;
import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import br.com.senac.sp.pi.galarraga.backend.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuariosController {
    private final AutenticacaoService usuarios;

    public UsuariosController(AutenticacaoService usuarios) {
        this.usuarios = usuarios;
    }

    // O cadastro público sempre cria uma conta de cliente.
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO cadastrar(@Valid @RequestBody RegisterDTO dados) {
        return UsuarioResponseDTO.de(usuarios.cadastrar(dados, UserRole.USER));
    }

    @PostMapping("/auth/login")
    public TokenDTO login(@Valid @RequestBody LoginDTO dados) {
        return new TokenDTO(usuarios.login(dados));
    }

    @GetMapping("/api/usuarios/me")
    public UsuarioResponseDTO perfil(@AuthenticationPrincipal Usuario usuario) {
        return UsuarioResponseDTO.de(usuario);
    }
}

package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(Integer id, String nome, String email, String cpf, String telefone,
                  LocalDateTime dataCadastro, boolean ativo, UserRole role) {
    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCpf(), usuario.getTelefone(),
                usuario.getDataCadastro(), usuario.getAtivo(), usuario.getRole());
    }
}

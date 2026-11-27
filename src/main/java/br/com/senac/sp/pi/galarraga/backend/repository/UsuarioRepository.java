package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.UserRole;
import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByRole(UserRole role);
}

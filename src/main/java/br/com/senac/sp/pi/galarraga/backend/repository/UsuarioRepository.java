package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByLogin(String login);
    Optional<UserDetails> findByLogin(String login);
}

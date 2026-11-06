package br.com.senac.sp.pi.galarraga.backend.service;

import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository repository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void Registrar(RegisterDTO dto){
        if(repository.existsByLogin(dto.login())){
            return;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}

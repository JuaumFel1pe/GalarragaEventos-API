package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long>{
}

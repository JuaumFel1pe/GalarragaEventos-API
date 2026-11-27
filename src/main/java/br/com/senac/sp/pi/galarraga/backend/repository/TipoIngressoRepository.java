package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.TipoIngresso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TipoIngressoRepository extends JpaRepository<TipoIngresso, Integer> {
    List<TipoIngresso> findByEventoIdOrderById(Integer idEvento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TipoIngresso t where t.id = :id")
    Optional<TipoIngresso> bloquear(@Param("id") Integer id);
}

package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByIdDesc(Integer idUsuario);
    List<Pedido> findAllByOrderByIdDesc();
    Optional<Pedido> findByChavePagamento(String chavePagamento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> bloquear(@Param("id") Integer id);
}

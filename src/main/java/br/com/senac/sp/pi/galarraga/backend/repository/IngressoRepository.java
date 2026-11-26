package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.Ingresso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngressoRepository extends JpaRepository<Ingresso, Integer> {
    List<Ingresso> findByItemIdOrderById(Integer idItem);
    Optional<Ingresso> findByCodigoQr(String codigoQr);

    @Query("select i.item.pedido.id from Ingresso i where i.codigoQr = :codigo")
    Optional<Integer> idPedidoPorCodigo(@Param("codigo") String codigo);
}

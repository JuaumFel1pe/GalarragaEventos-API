package br.com.senac.sp.pi.galarraga.backend.repository;

import br.com.senac.sp.pi.galarraga.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

}

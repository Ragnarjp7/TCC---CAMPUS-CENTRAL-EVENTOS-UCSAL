package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository
        extends JpaRepository<Evento, Long> {

    List<Evento> findByStatusIgnoreCase(String status);

    List<Evento> findByCategoriaId(Long categoriaId);
}

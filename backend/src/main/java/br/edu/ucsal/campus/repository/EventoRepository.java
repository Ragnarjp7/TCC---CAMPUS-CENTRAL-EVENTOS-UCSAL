package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventoRepository
        extends JpaRepository<Evento, Long> {


    @Query("SELECT e FROM Evento e JOIN FETCH e.categoria") 
    List<Evento> findAllComCategoria();
    
    List<Evento> findByStatus(Evento.StatusEvento status);

    List<Evento> findByCategoriaId(Long categoriaId);

    Long countById(Long id);
}

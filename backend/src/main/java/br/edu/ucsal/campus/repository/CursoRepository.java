package br.edu.ucsal.campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ucsal.campus.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    
}

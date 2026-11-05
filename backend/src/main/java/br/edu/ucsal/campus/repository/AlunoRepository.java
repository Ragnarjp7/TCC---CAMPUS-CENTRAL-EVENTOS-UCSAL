package br.edu.ucsal.campus.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.ucsal.campus.model.Aluno;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByMatricula(String matricula);

    @Query("SELECT a FROM Aluno a JOIN FETCH a.usuario JOIN FETCH a.curso")
    List<Aluno> findAllCompleto();
    
    Optional<Aluno> findByUsuarioId(Long usuarioId);

}

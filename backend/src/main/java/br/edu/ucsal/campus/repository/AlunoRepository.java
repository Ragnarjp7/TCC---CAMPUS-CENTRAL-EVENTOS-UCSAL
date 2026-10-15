package br.edu.ucsal.campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ucsal.campus.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMatricula(String matricula);

}

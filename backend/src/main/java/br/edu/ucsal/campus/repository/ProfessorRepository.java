package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
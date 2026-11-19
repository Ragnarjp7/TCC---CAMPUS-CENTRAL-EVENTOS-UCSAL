package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembroLigaRepository extends JpaRepository<MembroLiga, Long> {

    Optional<MembroLiga> findByAlunoIdAndLigaId(Long alunoId, Long ligaId);
    List<MembroLiga> findByLigaId(Long ligaId);

    // Busca membros de uma liga por situação
    List<MembroLiga> findByLigaIdAndStatus(
        Long ligaId,
        StatusMembro status
);

    @Query("""
        SELECT m FROM MembroLiga m
        JOIN m.aluno a
        JOIN a.usuario u
        WHERE u.id = :usuarioId
        AND m.liga.id = :ligaId
        AND m.status = :status
    """)
    Optional<MembroLiga> buscarMembroAtivo(
            @Param("usuarioId") Long usuarioId,
            @Param("ligaId") Long ligaId,
            @Param("status") StatusMembro status
    );
}
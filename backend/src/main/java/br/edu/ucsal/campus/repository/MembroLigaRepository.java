package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.enums.Permissao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface MembroLigaRepository extends JpaRepository<MembroLiga, Long> {

    @EntityGraph(attributePaths = { "aluno", "aluno.usuario", "liga", "cargo" })
    Optional<MembroLiga> findByAlunoIdAndLigaId(Long alunoId, Long ligaId);

    @Override
    @EntityGraph(attributePaths = { "aluno", "aluno.usuario", "liga", "cargo" })
    Optional<MembroLiga> findById(Long id);

    @EntityGraph(attributePaths = { "aluno", "aluno.usuario", "liga", "cargo" })
    List<MembroLiga> findByLigaId(Long ligaId);

    // Busca membros de uma liga por situação
    @EntityGraph(attributePaths = { "aluno", "aluno.usuario", "liga", "cargo" })
    List<MembroLiga> findByLigaIdAndStatus(
            Long ligaId,
            StatusMembro status);

    @EntityGraph(attributePaths = { "aluno", "aluno.usuario", "liga", "cargo" })
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
            @Param("status") StatusMembro status);

    // Verifica se existe algum vínculo histórico com o cargo
    boolean existsByCargoId(Long cargoId);

    // Conta membros ativos que possuem determinada permissão na liga
    @Query("""
            SELECT COUNT(m)
            FROM MembroLiga m
            JOIN m.cargo c
            JOIN c.permissoes p
            WHERE m.liga.id = :ligaId
            AND m.status = :status
            AND p = :permissao
            """)
    long contarMembrosAtivosComPermissao(
            @Param("ligaId") Long ligaId,
            @Param("status") StatusMembro status,
            @Param("permissao") Permissao permissao);

    // Conta membros ativos com determinada permissão em um cargo específico
    @Query("""
            SELECT COUNT(m)
            FROM MembroLiga m
            JOIN m.cargo c
            JOIN c.permissoes p
            WHERE m.liga.id = :ligaId
            AND c.id = :cargoId
            AND m.status = :status
            AND p = :permissao
            """)
    long contarMembrosAtivosComPermissaoNoCargo(
            @Param("ligaId") Long ligaId,
            @Param("cargoId") Long cargoId,
            @Param("status") StatusMembro status,
            @Param("permissao") Permissao permissao);

    // Vínculos do usuário (com liga e cargo) para montar o perfil logado
    @EntityGraph(attributePaths = { "liga", "cargo" })
    @Query("""
            SELECT m FROM MembroLiga m
            JOIN m.aluno a
            WHERE a.usuario.id = :usuarioId
            AND m.status = :status
            """)
    List<MembroLiga> buscarVinculosDoUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("status") StatusMembro status);
}

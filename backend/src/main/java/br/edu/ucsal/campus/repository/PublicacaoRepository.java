package br.edu.ucsal.campus.repository;

import br.edu.ucsal.campus.model.Publicacao;
import br.edu.ucsal.campus.model.enums.PublicoPublicacao;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.enums.TipoAutorPublicacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicacaoRepository extends JpaRepository<Publicacao, Long> {

    // Feed institucional:
    // 1. Publicações gerais e específicas do curso.
    // 2. Publicações do coordenador do curso aparecem primeiro.
    // 3. Dentro de cada prioridade, as mais recentes aparecem primeiro.
    @Query("""
            SELECT p
            FROM Publicacao p
            WHERE p.ativa = true
              AND (
                  p.publico = :publicoGeral
                  OR (
                      p.publico = :publicoEspecifico
                      AND p.cursoDestino.id = :cursoId
                  )
              )
            ORDER BY
                CASE
                    WHEN p.tipoAutor = :tipoCoordenador
                     AND (
                         p.cursoDestino.id = :cursoId
                         OR (
                             p.publico = :publicoGeral
                             AND EXISTS (
                                 SELECT c.id
                                 FROM Curso c
                                 WHERE c.id = :cursoId
                                   AND c.coordenador.usuario.id = p.autor.id
                             )
                         )
                     )
                    THEN 0
                    ELSE 1
                END,
                p.dataCriacao DESC,
                p.id DESC
            """)
    Page<Publicacao> buscarFeedPorCurso(
            @Param("cursoId") Long cursoId,
            @Param("tipoCoordenador") TipoAutorPublicacao tipoCoordenador,
            @Param("publicoGeral") PublicoPublicacao publicoGeral,
            @Param("publicoEspecifico") PublicoPublicacao publicoEspecifico,
            Pageable pageable);

    Optional<Publicacao> findByIdAndAtivaTrue(Long id);

    // Publicações próprias e publicações das ligas
    // nas quais o usuário possui vínculo ativo.
    @Query("""
            SELECT p
            FROM Publicacao p
            WHERE p.ativa = true
              AND (
                  p.autor.id = :usuarioId
                  OR EXISTS (
                      SELECT m.id
                      FROM MembroLiga m
                      WHERE m.liga.id = p.ligaAutora.id
                        AND m.aluno.usuario.id = :usuarioId
                        AND m.status = :statusAtivo
                  )
              )
            ORDER BY p.dataCriacao DESC, p.id DESC
            """)
    Page<Publicacao> buscarMinhasPublicacoes(
            @Param("usuarioId") Long usuarioId,
            @Param("statusAtivo") StatusMembro statusAtivo,
            Pageable pageable);

    // Recupera o curso vinculado ao coordenador.
    @Query("""
            SELECT c.id
            FROM Curso c
            WHERE c.coordenador.usuario.id = :usuarioId
            """)
    Optional<Long> buscarCursoIdDoCoordenador(
            @Param("usuarioId") Long usuarioId);

    // Consulta administrativa geral, utilizada pelo CAEX.
    @Query("""
            SELECT p
            FROM Publicacao p
            WHERE p.ativa = true
              AND (:texto IS NULL
                   OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.conteudo) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:tipoAutor IS NULL OR p.tipoAutor = :tipoAutor)
              AND (:publico IS NULL OR p.publico = :publico)
              AND (:cursoId IS NULL OR p.cursoDestino.id = :cursoId)
              AND (:autorId IS NULL OR p.autor.id = :autorId)
            ORDER BY p.dataCriacao DESC, p.id DESC
            """)
    Page<Publicacao> pesquisarAdministrativamente(
            @Param("texto") String texto,
            @Param("tipoAutor") TipoAutorPublicacao tipoAutor,
            @Param("publico") PublicoPublicacao publico,
            @Param("cursoId") Long cursoId,
            @Param("autorId") Long autorId,
            Pageable pageable);

    // Consulta administrativa do coordenador.
    // Exibe publicações gerais de todos os autores
    // e publicações específicas do seu próprio curso.
    @Query("""
            SELECT p
            FROM Publicacao p
            WHERE p.ativa = true
              AND (:texto IS NULL
                   OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.conteudo) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:tipoAutor IS NULL OR p.tipoAutor = :tipoAutor)
              AND (:publico IS NULL OR p.publico = :publico)
              AND (
                  (
                      p.publico = :publicoGeral
                  )
                  OR (
                      p.cursoDestino.id = :cursoId
                  )
              )
              AND (:autorId IS NULL OR p.autor.id = :autorId)
            ORDER BY
                CASE
                    WHEN p.tipoAutor = :tipoCoordenador
                    THEN 0
                    ELSE 1
                END,
                p.dataCriacao DESC,
                p.id DESC
            """)
    Page<Publicacao> pesquisarPorCurso(
            @Param("texto") String texto,
            @Param("tipoAutor") TipoAutorPublicacao tipoAutor,
            @Param("publico") PublicoPublicacao publico,
            @Param("cursoId") Long cursoId,
            @Param("publicoGeral") PublicoPublicacao publicoGeral,
            @Param("tipoCoordenador") TipoAutorPublicacao tipoCoordenador,
            @Param("autorId") Long autorId,
            Pageable pageable);
}

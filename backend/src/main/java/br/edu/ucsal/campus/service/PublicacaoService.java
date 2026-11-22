package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOPublicacaoRequest;
import br.edu.ucsal.campus.dto.DTOPublicacaoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.*;
import br.edu.ucsal.campus.model.enums.*;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.repository.*;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PublicacaoService {

    private final PublicacaoRepository publicacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final LigaRepository ligaRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final AuditoriaService auditoriaService;

    public PublicacaoService(
            PublicacaoRepository publicacaoRepository,
            UsuarioRepository usuarioRepository,
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            CursoRepository cursoRepository,
            LigaRepository ligaRepository,
            MembroLigaRepository membroLigaRepository,
            AuditoriaService auditoriaService) {

        this.publicacaoRepository = publicacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.ligaRepository = ligaRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.auditoriaService = auditoriaService;
    }

    // RF12, RF13 e RF14 - Criar publicação
    @Transactional
    public DTOPublicacaoResponse criar(
            DTOPublicacaoRequest request,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);
        validarConteudo(request);

        Usuario autor = buscarUsuario(usuario.id());

        TipoAutorPublicacao tipoAutor =
                identificarTipoAutor(usuario);

        Publicacao publicacao = new Publicacao();

        publicacao.setAutor(autor);
        publicacao.setTipoAutor(tipoAutor);

        if (tipoAutor == TipoAutorPublicacao.LIGA) {

            Liga liga = validarPublicacaoLiga(
                    request.ligaId(),
                    usuario
            );

            publicacao.setLigaAutora(liga);

        } else if (request.ligaId() != null) {
            throw new IllegalArgumentException(
                    "Somente publicações de ligas podem informar uma liga autora");
        }

        aplicarPublico(publicacao, request);

        publicacao.setTitulo(request.titulo().trim());
        publicacao.setConteudo(request.conteudo().trim());
        publicacao.setDataCriacao(LocalDateTime.now());
        publicacao.setAtiva(true);

        Publicacao salva = publicacaoRepository.save(publicacao);

        auditoriaService.registrar(
                usuario,
                "CRIAR_PUBLICACAO",
                "PUBLICACAO",
                salva.getId(),
                null,
                "titulo=" + salva.getTitulo()
                        + "; tipoAutor=" + tipoAutor
                        + "; publico=" + salva.getPublico(),
                "Publicação criada"
        );

        return DTOPublicacaoResponse.de(salva);
    }

    // RF15 - Editar publicação
    @Transactional
    public DTOPublicacaoResponse atualizar(
            Long id,
            DTOPublicacaoRequest request,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);
        validarConteudo(request);

        Publicacao publicacao = buscarPublicacao(id);

        if (!podeEditar(publicacao, usuario)) {
            throw new AccessDeniedException(
                    "Você não tem permissão para editar esta publicação");
        }

        String tituloAnterior = publicacao.getTitulo();
        String conteudoAnterior = publicacao.getConteudo();

        publicacao.setTitulo(request.titulo().trim());
        publicacao.setConteudo(request.conteudo().trim());

        aplicarPublico(publicacao, request);

        publicacao.setDataAtualizacao(LocalDateTime.now());

        Publicacao salva = publicacaoRepository.save(publicacao);

        auditoriaService.registrar(
                usuario,
                "EDITAR_PUBLICACAO",
                "PUBLICACAO",
                salva.getId(),
                "titulo=" + tituloAnterior
                        + "; conteudo=" + conteudoAnterior,
                "titulo=" + salva.getTitulo()
                        + "; conteudo=" + salva.getConteudo(),
                "Publicação atualizada"
        );

        return DTOPublicacaoResponse.de(salva);
    }

    // RF16 - Exclusão lógica de publicação
    @Transactional
    public void excluir(
            Long id,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);
        validarPermissaoAdministrativa(usuario);

        Publicacao publicacao = buscarPublicacao(id);

        publicacao.setAtiva(false);
        publicacao.setDataExclusao(LocalDateTime.now());

        publicacaoRepository.save(publicacao);

        auditoriaService.registrar(
                usuario,
                "EXCLUIR_PUBLICACAO",
                "PUBLICACAO",
                publicacao.getId(),
                "ativa=true",
                "ativa=false",
                "Publicação removida da visualização do sistema"
        );
    }

    // RF11 e RF17 - Feed institucional do aluno
    @Transactional(readOnly = true)
    public Page<DTOPublicacaoResponse> listarFeed(
            UsuarioAutenticado usuario,
            Pageable pageable) {

        validarUsuario(usuario);

        if (usuario.papeis() == null
                || !usuario.papeis().contains(Papel.ALUNO)) {

            throw new AccessDeniedException(
                    "O feed institucional desta operação é destinado aos alunos");
        }

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new AccessDeniedException(
                        "Não existe cadastro de aluno vinculado ao usuário"));

        return publicacaoRepository.buscarFeedPorCurso(
                        aluno.getCurso().getId(),
                        TipoAutorPublicacao.COORDENADOR,
                        pageable
                )
                .map(DTOPublicacaoResponse::de);
    }

    // Consulta as publicações criadas pelo usuário autenticado
    @Transactional(readOnly = true)
    public Page<DTOPublicacaoResponse> listarMinhas(
            UsuarioAutenticado usuario,
            Pageable pageable) {

        validarUsuario(usuario);

        return publicacaoRepository
                .findByAutorIdAndAtivaTrueOrderByDataCriacaoDesc(
                        usuario.id(),
                        pageable
                )
                .map(DTOPublicacaoResponse::de);
    }

    // Busca uma publicação ativa
    @Transactional(readOnly = true)
    public DTOPublicacaoResponse buscarPorId(
            Long id,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);

        Publicacao publicacao = buscarPublicacao(id);

        if (!podeVisualizar(publicacao, usuario)) {
            throw new AccessDeniedException(
                    "Você não tem permissão para visualizar esta publicação");
        }

        return DTOPublicacaoResponse.de(publicacao);
    }

    private TipoAutorPublicacao identificarTipoAutor(
            UsuarioAutenticado usuario) {

        if (usuario.papeis().contains(Papel.CAEX)) {
            return TipoAutorPublicacao.CAEX;
        }

        if (usuario.papeis().contains(Papel.COORDENADOR)) {
            professorRepository.findByUsuarioId(usuario.id())
                    .orElseThrow(() -> new AccessDeniedException(
                            "Coordenador sem cadastro de professor vinculado"));

            return TipoAutorPublicacao.COORDENADOR;
        }

        if (usuario.papeis().contains(Papel.PROFESSOR)) {
            professorRepository.findByUsuarioId(usuario.id())
                    .orElseThrow(() -> new AccessDeniedException(
                            "Professor não cadastrado"));

            return TipoAutorPublicacao.PROFESSOR;
        }

        if (usuario.papeis().contains(Papel.ALUNO)) {
            return TipoAutorPublicacao.LIGA;
        }

        throw new AccessDeniedException(
                "Seu perfil não pode criar publicações");
    }

    private Liga validarPublicacaoLiga(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (ligaId == null) {
            throw new IllegalArgumentException(
                    "Informe a liga responsável pela publicação");
        }

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Liga não encontrada"));

        if (liga.getStatus() != StatusLiga.APROVADA) {
            throw new ConflitoException(
                    "Somente ligas aprovadas podem publicar conteúdo");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                ligaId,
                StatusMembro.ATIVO
        ).orElseThrow(() -> new AccessDeniedException(
                "Você não é membro ativo desta liga"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.PUBLICAR)) {

            throw new AccessDeniedException(
                    "Seu cargo não possui permissão para publicar pela liga");
        }

        return liga;
    }

    private void aplicarPublico(
            Publicacao publicacao,
            DTOPublicacaoRequest request) {

        if (request.publico() == null) {
            throw new IllegalArgumentException(
                    "O público da publicação é obrigatório");
        }

        publicacao.setPublico(request.publico());

        if (request.publico() == PublicoPublicacao.GERAL) {

            if (request.cursoDestinoId() != null) {
                throw new IllegalArgumentException(
                        "Publicações gerais não devem informar curso de destino");
            }

            publicacao.setCursoDestino(null);
            return;
        }

        if (request.cursoDestinoId() == null) {
            throw new IllegalArgumentException(
                    "Selecione um curso para a publicação específica");
        }

        Curso curso = cursoRepository.findById(
                request.cursoDestinoId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Curso de destino não encontrado"));

        publicacao.setCursoDestino(curso);
    }

    private boolean podeEditar(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        return publicacao.getAutor().getId().equals(usuario.id())
                || possuiPermissaoAdministrativa(usuario);
    }

    private boolean podeVisualizar(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        if (publicacao.getPublico() == PublicoPublicacao.GERAL) {
            return true;
        }

        if (usuario.papeis() != null
                && usuario.papeis().contains(Papel.ALUNO)) {

            return alunoRepository.findByUsuarioId(usuario.id())
                    .map(aluno -> aluno.getCurso().getId()
                            .equals(publicacao.getCursoDestino().getId()))
                    .orElse(false);
        }

        return possuiPermissaoAdministrativa(usuario)
                || publicacao.getAutor().getId().equals(usuario.id());
    }

    private void validarPermissaoAdministrativa(
            UsuarioAutenticado usuario) {

        if (!possuiPermissaoAdministrativa(usuario)) {
            throw new AccessDeniedException(
                    "Somente CAEX ou coordenadores podem executar esta operação");
        }
    }

    private boolean possuiPermissaoAdministrativa(
            UsuarioAutenticado usuario) {

        return usuario != null
                && usuario.papeis() != null
                && (usuario.papeis().contains(Papel.CAEX)
                || usuario.papeis().contains(Papel.COORDENADOR));
    }

    private void validarUsuario(UsuarioAutenticado usuario) {

        if (usuario == null || usuario.id() == null
                || usuario.papeis() == null) {

            throw new AccessDeniedException(
                    "Usuário não autenticado");
        }
    }

    private void validarConteudo(DTOPublicacaoRequest request) {

        if (request == null
                || request.titulo() == null
                || request.titulo().isBlank()
                || request.conteudo() == null
                || request.conteudo().isBlank()) {

            throw new IllegalArgumentException(
                    "Título e conteúdo são obrigatórios");
        }

        if (request.titulo().trim().length() > 200) {
            throw new IllegalArgumentException(
                    "O título não pode ultrapassar 200 caracteres");
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado"));
    }

    private Publicacao buscarPublicacao(Long id) {

        return publicacaoRepository.findByIdAndAtivaTrue(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Publicação não encontrada"));
    }
}

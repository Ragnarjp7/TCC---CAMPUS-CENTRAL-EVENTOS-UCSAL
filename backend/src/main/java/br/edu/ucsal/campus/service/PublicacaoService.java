package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOPublicacaoFiltro;
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
        TipoAutorPublicacao tipoAutor = identificarTipoAutor(usuario);

        Publicacao publicacao = new Publicacao();
        publicacao.setAutor(autor);
        publicacao.setTipoAutor(tipoAutor);

        if (tipoAutor == TipoAutorPublicacao.LIGA) {
            Liga liga = validarPublicacaoLiga(request.ligaId(), usuario);
            publicacao.setLigaAutora(liga);
        } else if (request.ligaId() != null) {
            throw new IllegalArgumentException(
                    "Somente publicações de ligas podem informar uma liga autora");
        }

        aplicarPublico(publicacao, request);
        validarAlcanceDoCoordenador(publicacao, usuario);

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
                "Publicação criada");

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

        validarVinculoDaLiga(publicacao, request, usuario);

        String tituloAnterior = publicacao.getTitulo();
        String conteudoAnterior = publicacao.getConteudo();

        publicacao.setTitulo(request.titulo().trim());
        publicacao.setConteudo(request.conteudo().trim());

        aplicarPublico(publicacao, request);
        validarAlcanceDoCoordenador(publicacao, usuario);

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
                "Publicação atualizada");

        return DTOPublicacaoResponse.de(salva);
    }

    // RF16 - Exclusão lógica de publicação
    @Transactional
    public void excluir(Long id, UsuarioAutenticado usuario) {

        validarUsuario(usuario);

        Publicacao publicacao = buscarPublicacao(id);

        if (!podeExcluir(publicacao, usuario)) {
            throw new AccessDeniedException(
                    "Você não tem permissão para excluir esta publicação");
        }

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
                "Publicação removida da visualização do sistema");
    }

    // RF11 e RF17 - Feed institucional do aluno
    @Transactional(readOnly = true)
    public Page<DTOPublicacaoResponse> listarFeed(
            UsuarioAutenticado usuario,
            Pageable pageable) {

        validarUsuario(usuario);

        if (!usuario.papeis().contains(Papel.ALUNO)) {
            throw new AccessDeniedException(
                    "O feed institucional é destinado aos alunos");
        }

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new AccessDeniedException(
                        "Não existe cadastro de aluno vinculado ao usuário"));

        return publicacaoRepository.buscarFeedPorCurso(
                aluno.getCurso().getId(),
                TipoAutorPublicacao.COORDENADOR,
                PublicoPublicacao.GERAL,
                PublicoPublicacao.CURSO_ESPECIFICO,
                pageable)
                .map(DTOPublicacaoResponse::de);
    }

    // Publicações criadas pelo usuário ou pelas ligas das quais participa
    @Transactional(readOnly = true)
    public Page<DTOPublicacaoResponse> listarMinhas(
            UsuarioAutenticado usuario,
            Pageable pageable) {

        validarUsuario(usuario);

        return publicacaoRepository.buscarMinhasPublicacoes(
                usuario.id(),
                StatusMembro.ATIVO,
                pageable)
                .map(DTOPublicacaoResponse::de);
    }

    // Consulta uma publicação ativa respeitando seu público
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

    // Consulta administrativa com filtros
    @Transactional(readOnly = true)
    public Page<DTOPublicacaoResponse> pesquisarAdministrativamente(
            DTOPublicacaoFiltro filtro,
            UsuarioAutenticado usuario,
            Pageable pageable) {

        validarUsuario(usuario);

        if (filtro == null) {
            filtro = new DTOPublicacaoFiltro(
                    null, null, null, null, null);
        }

        String texto = normalizarTexto(filtro.texto());

        if (usuario.papeis().contains(Papel.CAEX)) {
            return publicacaoRepository.pesquisarAdministrativamente(
                    texto,
                    filtro.tipoAutor(),
                    filtro.publico(),
                    filtro.cursoId(),
                    filtro.autorId(),
                    pageable)
                    .map(DTOPublicacaoResponse::de);
        }

        if (usuario.papeis().contains(Papel.COORDENADOR)) {
            Long cursoId = buscarCursoDoCoordenador(usuario.id());

            if (filtro.cursoId() != null
                    && !filtro.cursoId().equals(cursoId)) {
                throw new AccessDeniedException(
                        "O coordenador só pode consultar publicações do próprio curso");
            }

            return publicacaoRepository.pesquisarPorCurso(
                    texto,
                    filtro.tipoAutor(),
                    filtro.publico(),
                    cursoId,
                    PublicoPublicacao.GERAL,
                    TipoAutorPublicacao.COORDENADOR,
                    filtro.autorId(),
                    pageable)
                    .map(DTOPublicacaoResponse::de);
        }

        throw new AccessDeniedException(
                "Somente CAEX ou coordenadores podem consultar publicações administrativamente");
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
                StatusMembro.ATIVO).orElseThrow(
                        () -> new AccessDeniedException(
                                "Você não é membro ativo desta liga"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.PUBLICAR)) {
            throw new AccessDeniedException(
                    "Seu cargo não possui permissão para publicar pela liga");
        }

        return liga;
    }

    private void validarVinculoDaLiga(
            Publicacao publicacao,
            DTOPublicacaoRequest request,
            UsuarioAutenticado usuario) {

        if (publicacao.getTipoAutor() == TipoAutorPublicacao.LIGA) {

            if (publicacao.getLigaAutora() == null
                    || request.ligaId() == null
                    || !publicacao.getLigaAutora().getId()
                            .equals(request.ligaId())) {
                throw new IllegalArgumentException(
                        "A liga autora da publicação não pode ser alterada");
            }

            boolean caex = usuario.papeis().contains(Papel.CAEX);
            boolean coordenador = usuario.papeis().contains(Papel.COORDENADOR);
            boolean autorOriginal = publicacao.getAutor() != null
                    && publicacao.getAutor().getId().equals(usuario.id());

            if (caex || coordenador) {
                return;
            }

            if (autorOriginal) {
                validarPublicacaoLiga(request.ligaId(), usuario);
                return;
            }

            throw new AccessDeniedException(
                    "Você não tem permissão para editar esta publicação de Liga");
        }

        if (request.ligaId() != null) {
            throw new IllegalArgumentException(
                    "Esta publicação não pertence a uma liga");
        }
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

        Curso curso = cursoRepository.findById(request.cursoDestinoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Curso de destino não encontrado"));

        publicacao.setCursoDestino(curso);
    }

    private boolean podeEditar(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        if (usuario.papeis().contains(Papel.CAEX)) {
            return true;
        }

        if (publicacao.getAutor().getId().equals(usuario.id())) {
            return true;
        }

        if (usuario.papeis().contains(Papel.COORDENADOR)) {
            Long cursoId = buscarCursoDoCoordenador(usuario.id());

            return publicacao.getPublico() == PublicoPublicacao.CURSO_ESPECIFICO
                    && publicacao.getCursoDestino() != null
                    && publicacao.getCursoDestino().getId().equals(cursoId);
        }

        return false;
    }

    private boolean podeExcluir(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        // CAEX pode excluir qualquer publicação.
        if (usuario.papeis().contains(Papel.CAEX)) {
            return true;
        }

        // O autor pode excluir a própria publicação.
        if (publicacao.getAutor().getId().equals(usuario.id())) {

            // Para publicações de Liga, o autor precisa continuar
            // sendo membro ativo e possuir permissão PUBLICAR.
            if (publicacao.getTipoAutor() == TipoAutorPublicacao.LIGA) {
                if (publicacao.getLigaAutora() == null) {
                    return false;
                }

                validarPublicacaoLiga(
                        publicacao.getLigaAutora().getId(),
                        usuario);
            }

            return true;
        }

        // Coordenador pode excluir publicações específicas
        // destinadas ao seu próprio curso.
        if (usuario.papeis().contains(Papel.COORDENADOR)) {
            Long cursoId = buscarCursoDoCoordenador(usuario.id());

            return publicacao.getPublico() == PublicoPublicacao.CURSO_ESPECIFICO
                    && publicacao.getCursoDestino() != null
                    && publicacao.getCursoDestino().getId().equals(cursoId);
        }

        return false;
    }

    private boolean podeVisualizar(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        if (publicacao.getPublico() == PublicoPublicacao.GERAL) {
            return true;
        }

        if (usuario.papeis().contains(Papel.CAEX)) {
            return true;
        }

        if (publicacao.getCursoDestino() == null) {
            return false;
        }

        if (usuario.papeis().contains(Papel.ALUNO)) {
            return alunoRepository.findByUsuarioId(usuario.id())
                    .map(aluno -> aluno.getCurso() != null
                            && aluno.getCurso().getId()
                                    .equals(publicacao.getCursoDestino().getId()))
                    .orElse(false);
        }

        if (usuario.papeis().contains(Papel.COORDENADOR)) {
            return publicacao.getCursoDestino().getId()
                    .equals(buscarCursoDoCoordenador(usuario.id()));
        }

        return publicacao.getAutor() != null
                && publicacao.getAutor().getId().equals(usuario.id());
    }

    private void validarAlcanceDoCoordenador(
            Publicacao publicacao,
            UsuarioAutenticado usuario) {

        if (!usuario.papeis().contains(Papel.COORDENADOR)
                || publicacao.getPublico() == PublicoPublicacao.GERAL) {
            return;
        }

        Long cursoId = buscarCursoDoCoordenador(usuario.id());

        if (!publicacao.getCursoDestino().getId().equals(cursoId)) {
            throw new AccessDeniedException(
                    "O coordenador só pode direcionar publicações ao próprio curso");
        }
    }

    private Long buscarCursoDoCoordenador(Long usuarioId) {
        return publicacaoRepository.buscarCursoIdDoCoordenador(usuarioId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Não existe curso vinculado a este coordenador"));
    }

    private String normalizarTexto(String texto) {
        return texto == null || texto.isBlank()
                ? null
                : texto.trim();
    }

    private void validarUsuario(UsuarioAutenticado usuario) {
        if (usuario == null || usuario.id() == null
                || usuario.papeis() == null) {
            throw new AccessDeniedException("Usuário não autenticado");
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

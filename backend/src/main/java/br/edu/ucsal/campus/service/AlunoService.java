package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOAlunoRequest;
import br.edu.ucsal.campus.dto.DTOAlunoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Curso;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.SituacaoAcademica;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CursoRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AlunoService(
            AlunoRepository alunoRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService) {

        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<DTOAlunoResponse> listarTodos() {
        return alunoRepository.findAllCompleto()
                .stream()
                .map(DTOAlunoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public DTOAlunoResponse buscarPorId(Long id) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() ->
                        new NaoEncontradoException("Aluno não encontrado"));

        return DTOAlunoResponse.de(aluno);
    }

    @Transactional
    public DTOAlunoResponse criar(DTOAlunoRequest aluno) {

        if (vazio(aluno.nome())
                || vazio(aluno.email())
                || vazio(aluno.matricula())
                || aluno.cursoId() == null
                || vazio(aluno.senha())) {

            throw new IllegalArgumentException(
                    "Nome, email, matrícula, curso e senha são obrigatórios"
            );
        }

        if (!aluno.email().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        if (aluno.senha().length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo 6 caracteres"
            );
        }

        if (usuarioRepository.existsByEmailIgnoreCase(
                aluno.email().trim())) {

            throw new ConflitoException(
                    "Já existe um usuário com esse email"
            );
        }

        if (alunoRepository.existsByMatricula(
                aluno.matricula().trim())) {

            throw new ConflitoException(
                    "Já existe um aluno com essa matrícula"
            );
        }

        Curso curso = cursoRepository.findById(aluno.cursoId())
                .orElseThrow(() ->
                        new NaoEncontradoException("Curso não encontrado"));

        Usuario usuario = new Usuario();

        usuario.setNome(aluno.nome().trim());
        usuario.setEmail(aluno.email().trim());

        usuario.setSenhaHash(
                passwordEncoder.encode(aluno.senha())
        );

        usuario.setPapeis(Set.of(Papel.ALUNO));

        usuario = usuarioRepository.save(usuario);

        Aluno novoAluno = new Aluno();

        novoAluno.setUsuario(usuario);
        novoAluno.setMatricula(aluno.matricula().trim());
        novoAluno.setCurso(curso);

        return DTOAlunoResponse.de(
                alunoRepository.save(novoAluno)
        );
    }

    // Atualiza a situação acadêmica do aluno.
    // Operação restrita aos perfis ADMIN e CAEX.
    @Transactional
    public DTOAlunoResponse atualizarSituacaoAcademica(
            Long alunoId,
            SituacaoAcademica novaSituacao,
            UsuarioAutenticado usuario) {

        validarPermissaoAtualizarSituacao(usuario);

        if (alunoId == null) {
            throw new IllegalArgumentException(
                    "O ID do aluno é obrigatório"
            );
        }

        if (novaSituacao == null) {
            throw new IllegalArgumentException(
                    "A nova situação acadêmica é obrigatória"
            );
        }

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() ->
                        new NaoEncontradoException("Aluno não encontrado"));

        SituacaoAcademica situacaoAnterior = aluno.getSituacao();

        if (situacaoAnterior == novaSituacao) {
            throw new ConflitoException(
                    "O aluno já possui essa situação acadêmica"
            );
        }

        aluno.setSituacao(novaSituacao);

        Aluno alunoAtualizado = alunoRepository.save(aluno);

        auditoriaService.registrar(
                usuario,
                "ATUALIZAR_SITUACAO_ACADEMICA",
                "ALUNO",
                aluno.getId(),
                "Situação acadêmica: " + situacaoAnterior,
                "Situação acadêmica: " + novaSituacao,
                "Atualização manual da situação acadêmica do aluno "
                        + aluno.getMatricula()
        );

        return DTOAlunoResponse.de(alunoAtualizado);
    }

    private void validarPermissaoAtualizarSituacao(
            UsuarioAutenticado usuario) {

        if (usuario == null
                || usuario.papeis() == null
                || (!usuario.papeis().contains(Papel.ADMIN)
                && !usuario.papeis().contains(Papel.CAEX))) {

            throw new AccessDeniedException(
                    "Você não tem permissão para atualizar a situação acadêmica"
            );
        }
    }

    @Transactional(readOnly = true)
    public Long buscarIdPorUsuario(Long usuarioId) {

        return alunoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Perfil de aluno não encontrado"
                        ))
                .getId();
    }

    @Transactional(readOnly = true)
    public DTOAlunoResponse buscarPorUsuarioId(Long usuarioId) {

        Aluno aluno = alunoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Perfil de aluno não encontrado"
                        ));

        return DTOAlunoResponse.de(aluno);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}

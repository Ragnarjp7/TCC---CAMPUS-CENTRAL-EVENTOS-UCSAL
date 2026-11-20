package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOAlunoRequest;
import br.edu.ucsal.campus.dto.DTOAlunoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Curso;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CursoRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(
            AlunoRepository alunoRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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

        // Validação dos campos obrigatórios
        if (vazio(aluno.nome())
                || vazio(aluno.email())
                || vazio(aluno.matricula())
                || aluno.cursoId() == null
                || vazio(aluno.senha())) {

            throw new IllegalArgumentException(
                    "Nome, email, matrícula, curso e senha são obrigatórios"
            );
        }

        // Validação do email
        if (!aluno.email().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Validação do tamanho da senha
        if (aluno.senha().length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve ter no mínimo 6 caracteres"
            );
        }

        // Verifica se o email já está cadastrado
        if (usuarioRepository.existsByEmailIgnoreCase(
                aluno.email().trim())) {

            throw new ConflitoException(
                    "Já existe um usuário com esse email"
            );
        }

        // Verifica se a matrícula já está cadastrada
        if (alunoRepository.existsByMatricula(
                aluno.matricula().trim())) {

            throw new ConflitoException(
                    "Já existe um aluno com essa matrícula"
            );
        }

        // Busca o curso informado
        Curso curso = cursoRepository.findById(aluno.cursoId())
                .orElseThrow(() ->
                        new NaoEncontradoException("Curso não encontrado"));

        // Cria o usuário vinculado ao aluno
        Usuario usuario = new Usuario();

        usuario.setNome(aluno.nome().trim());
        usuario.setEmail(aluno.email().trim());

        // Criptografa a senha antes de salvar
        usuario.setSenhaHash(
                passwordEncoder.encode(aluno.senha())
        );

        // Define o papel de aluno
        usuario.setPapeis(Set.of(Papel.ALUNO));

        // Salva o usuário no banco
        usuario = usuarioRepository.save(usuario);

        // Cria o perfil de aluno
        Aluno novoAluno = new Aluno();

        novoAluno.setUsuario(usuario);
        novoAluno.setMatricula(aluno.matricula().trim());
        novoAluno.setCurso(curso);

        // Salva e retorna o aluno
        return DTOAlunoResponse.de(
                alunoRepository.save(novoAluno)
        );
    }

    // Busca o ID do aluno pelo ID do usuário autenticado
    @Transactional(readOnly = true)
    public Long buscarIdPorUsuario(Long usuarioId) {

        return alunoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Perfil de aluno não encontrado"
                        ))
                .getId();
    }

    // Busca o perfil completo do aluno pelo ID do usuário
    @Transactional(readOnly = true)
    public DTOAlunoResponse buscarPorUsuarioId(Long usuarioId) {

        Aluno aluno = alunoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Perfil de aluno não encontrado"
                        ));

        return DTOAlunoResponse.de(aluno);
    }

    // Verifica se uma String está vazia ou contém apenas espaços
    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}

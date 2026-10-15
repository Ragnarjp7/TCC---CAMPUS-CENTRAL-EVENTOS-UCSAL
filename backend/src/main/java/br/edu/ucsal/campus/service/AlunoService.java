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

import java.util.List;
import java.util.Set;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public AlunoService(AlunoRepository alunoRepository, CursoRepository cursoRepository, UsuarioRepository usuarioRepository) {
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }


    @Transactional(readOnly = true)
    public List<DTOAlunoResponse> listarTodos() {
        return alunoRepository.findAllCompleto().stream().map(DTOAlunoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public DTOAlunoResponse buscarPorId(Long id) {
        return DTOAlunoResponse.de(alunoRepository.findById(id).orElseThrow(() -> new NaoEncontradoException("Aluno não encontrado")));
    }

    @Transactional
    public DTOAlunoResponse criar(DTOAlunoRequest aluno) {
        if (vazio(aluno.nome()) || vazio(aluno.email()) || vazio(aluno.matricula()) || aluno.cursoId() == null) {
            throw new IllegalArgumentException("Nome, email, matrícula e curso são obrigatórios");
        }
        if (!aluno.email().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(aluno.email())) {
            throw new ConflitoException("Já existe um usuário com esse email");
        }
        if (alunoRepository.existsByMatricula(aluno.matricula())) {
            throw new ConflitoException("Já existe um aluno com essa matrícula");
        }

        Curso curso = cursoRepository.findById(aluno.cursoId())
                .orElseThrow(() -> new NaoEncontradoException("Curso não encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNome(aluno.nome().trim());
        usuario.setEmail(aluno.email().trim());
        usuario.setPapeis(Set.of(Papel.ALUNO));
        usuario = usuarioRepository.save(usuario);

        Aluno a = new Aluno();
        a.setUsuario(usuario);
        a.setMatricula(aluno.matricula().trim());
        a.setCurso(curso);

        return DTOAlunoResponse.de(alunoRepository.save(a));
    }
    

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
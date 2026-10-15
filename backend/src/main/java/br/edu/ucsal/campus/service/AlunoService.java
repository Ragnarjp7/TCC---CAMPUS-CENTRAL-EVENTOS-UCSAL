package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id).orElseThrow(() -> new NaoEncontradoException("Aluno não encontrado"));
    }

    @Transactional
    public Aluno criar(Aluno aluno) {
        if (vazio(aluno.getNome()) || vazio(aluno.getEmail()) || vazio(aluno.getMatricula())) {
            throw new IllegalArgumentException("Nome, email e matrícula são obrigatórios");
        }
        if (!aluno.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (alunoRepository.existsByEmailIgnoreCase(aluno.getEmail())) {
            throw new ConflitoException("Já existe um aluno com esse email");
        }
        if (alunoRepository.existsByMatricula(aluno.getMatricula())) {
            throw new ConflitoException("Já existe um aluno com essa matrícula");
        }

        aluno.setId(null); // garante que é um novo registro
        return alunoRepository.save(aluno);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
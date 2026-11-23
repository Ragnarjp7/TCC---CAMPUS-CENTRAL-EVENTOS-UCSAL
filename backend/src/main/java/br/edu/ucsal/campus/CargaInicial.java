package br.edu.ucsal.campus;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.edu.ucsal.campus.model.*;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.*;

import jakarta.transaction.Transactional;

@Component
@ConditionalOnProperty(
    name = "campus.seed.enabled",
    havingValue = "true"
)
public class CargaInicial implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final LigaRepository ligaRepository;
    private final CargoRepository cargoRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaInicial;

    public CargaInicial(
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            LigaRepository ligaRepository,
            CargoRepository cargoRepository,
            MembroLigaRepository membroLigaRepository,
            PasswordEncoder passwordEncoder,
            @Value("123456") String senhaInicial) {

        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.ligaRepository = ligaRepository;
        this.cargoRepository = cargoRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaInicial = senhaInicial;
    }

    private Usuario criarUsuario(
            String nome,
            String email,
            Papel... papeis) {

        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senhaInicial));
        usuario.setPapeis(new HashSet<>(Set.of(papeis)));

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (senhaInicial == null || senhaInicial.isBlank()) {
            throw new IllegalStateException(
                "A variável SEED_INITIAL_PASSWORD deve ser configurada para executar a carga inicial."
            );
        }

        if (cursoRepository.count() > 0) {
            return;
        }

        // Criação dos cursos
        Curso curso = cursoRepository.save(
            new Curso("Sistemas de Informação")
        );

        cursoRepository.save(new Curso("Direito"));

        // Criação dos usuários iniciais
        criarUsuario(
            "Administrador Geral",
            "admin@ucsal.edu.br",
            Papel.ADMIN
        );

        criarUsuario(
            "Analista CAEX",
            "caex@ucsal.edu.br",
            Papel.CAEX
        );

        criarUsuario(
            "Núcleo de Estágio",
            "estagio@ucsal.edu.br",
            Papel.CEPRO
        );

        // Criação do professor
        Professor professor = new Professor();

        professor.setUsuario(
            criarUsuario(
                "Prof. Carlos Lima",
                "carlos@ucsal.edu.br",
                Papel.PROFESSOR,
                Papel.COORDENADOR
            )
        );

        professor.setRegistro("PROF001");
        professor.setCurso(curso);

        professor = professorRepository.save(professor);

        // Define o coordenador do curso
        curso.setCoordenador(professor);
        cursoRepository.save(curso);

        // Criação da Liga acadêmica
        Liga liga = new Liga();

        liga.setNome("Liga Acadêmica de Tecnologia");
        liga.setDescricao("Liga de exemplo para testes");
        liga.setStatus(StatusLiga.APROVADA);
        liga.setCurso(curso);
        liga.setOrientador(professor);

        liga = ligaRepository.save(liga);

        // Criação do cargo de presidente
        Cargo presidente = new Cargo();

        presidente.setNome("Presidente");
        presidente.setLiga(liga);
        presidente.setPermissoes(EnumSet.allOf(Permissao.class));

        presidente = cargoRepository.save(presidente);

        // Criação do aluno
        Aluno aluno = new Aluno();

        aluno.setUsuario(
            criarUsuario(
                "Maria Souza",
                "maria@ucsal.edu.br",
                Papel.ALUNO
            )
        );

        aluno.setMatricula("2026001");
        aluno.setCurso(curso);

        aluno = alunoRepository.save(aluno);

        // Vinculação do aluno à Liga
        MembroLiga membro = new MembroLiga();

        membro.setAluno(aluno);
        membro.setLiga(liga);
        membro.setCargo(presidente);
        membro.setStatus(StatusMembro.ATIVO);

        membroLigaRepository.save(membro);
    }
}


package br.edu.ucsal.campus;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.ucsal.campus.model.*;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.*;

import jakarta.transaction.Transactional;

@Component
public class CargaInicial implements CommandLineRunner {

    private static final String SENHA_PADRAO = "123456";

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final LigaRepository ligaRepository;
    private final CargoRepository cargoRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final PasswordEncoder passwordEncoder;

    public CargaInicial(
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            LigaRepository ligaRepository,
            CargoRepository cargoRepository,
            MembroLigaRepository membroLigaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.ligaRepository = ligaRepository;
        this.cargoRepository = cargoRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Usuario criarUsuario(
            String nome,
            String email,
            Papel... papeis) {

        Usuario u = new Usuario();

        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        u.setPapeis(new HashSet<>(Set.of(papeis)));

        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // Criptografa a senha padrão
        String hashPadrao = passwordEncoder.encode(SENHA_PADRAO);

        // Atualiza usuários antigos que ainda não possuem senha
        usuarioRepository.findAll().stream()
                .filter(u -> u.getSenhaHash() == null)
                .forEach(u -> {
                    u.setSenhaHash(hashPadrao);
                    usuarioRepository.save(u);
                });

        // Evita duplicar os dados iniciais
        if (cursoRepository.count() > 0) {
            return;
        }

        // Criação dos cursos
        Curso curso = cursoRepository.save(
                new Curso("Sistemas de Informação"));

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
        Professor prof = new Professor();

        prof.setUsuario(
                criarUsuario(
                        "Prof. Carlos Lima",
                        "carlos@ucsal.edu.br",
                        Papel.PROFESSOR,
                        Papel.COORDENADOR
                )
        );

        prof.setRegistro("PROF001");
        prof.setCurso(curso);

        prof = professorRepository.save(prof);

        // Define o coordenador do curso
        curso.setCoordenador(prof);
        cursoRepository.save(curso);

        // Criação da liga acadêmica
        Liga liga = new Liga();

        liga.setNome("Liga Acadêmica de Tecnologia");
        liga.setDescricao("Liga de exemplo para testes");
        liga.setStatus(StatusLiga.APROVADA);
        liga.setCurso(curso);
        liga.setOrientador(prof);

        liga = ligaRepository.save(liga);

        // Criação do cargo de presidente
        Cargo presidente = new Cargo();

        presidente.setNome("Presidente");
        presidente.setLiga(liga);
        presidente.setPermissoes(EnumSet.allOf(Permissao.class));

        presidente = cargoRepository.save(presidente);

        // Criação do aluno
        Aluno maria = new Aluno();

        maria.setUsuario(
                criarUsuario(
                        "Maria Souza",
                        "maria@ucsal.edu.br",
                        Papel.ALUNO
                )
        );

        maria.setMatricula("2026001");
        maria.setCurso(curso);

        maria = alunoRepository.save(maria);

        // Vinculação do aluno à liga
        MembroLiga membro = new MembroLiga();

        membro.setAluno(maria);
        membro.setLiga(liga);
        membro.setCargo(presidente);
        membro.setStatus(StatusMembro.ATIVO);

        membroLigaRepository.save(membro);
    }
}

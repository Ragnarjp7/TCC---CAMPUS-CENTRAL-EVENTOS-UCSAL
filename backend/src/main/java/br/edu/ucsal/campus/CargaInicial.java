package br.edu.ucsal.campus;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import br.edu.ucsal.campus.model.*;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.*;
import jakarta.transaction.Transactional;

@Component 
public class CargaInicial implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final LigaRepository ligaRepository;
    private final CargoRepository cargoRepository;
    private final MembroLigaRepository membroLigaRepository;

    public CargaInicial(UsuarioRepository usuarioRepository, CursoRepository cursoRepository, ProfessorRepository professorRepository, AlunoRepository alunoRepository, LigaRepository ligaRepository, CargoRepository cargoRepository, MembroLigaRepository membroLigaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.ligaRepository = ligaRepository;
        this.cargoRepository = cargoRepository;
        this.membroLigaRepository = membroLigaRepository;
    }

    private Usuario criarUsuario(String nome, String email, Papel... papeis) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setPapeis(new HashSet<>(Set.of(papeis)));
        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (cursoRepository.count() > 0) {
            return;
        }

        Curso curso = cursoRepository.save(new Curso("Sistemas de Informação"));
        cursoRepository.save(new Curso("Direito"));

        criarUsuario("Administrador Geral", "admin@ucsal.edu.br", Papel.ADMIN);
        criarUsuario("Analista CAEX", "caex@ucsal.edu.br", Papel.CAEX);
        criarUsuario("Núcleo de Estágio", "estagio@ucsal.edu.br", Papel.CEPRO);

        Professor prof = new Professor();
        prof.setUsuario(criarUsuario("Prof. Carlos Lima", "carlos@ucsal.edu.br",
                Papel.PROFESSOR, Papel.COORDENADOR));
        prof.setRegistro("PROF001");
        prof.setCurso(curso);
        prof = professorRepository.save(prof);

        curso.setCoordenador(prof);
        cursoRepository.save(curso);

        Liga liga = new Liga();
        liga.setNome("Liga Acadêmica de Tecnologia");
        liga.setDescricao("Liga de exemplo para testes");
        liga.setStatus(StatusLiga.APROVADA);
        liga.setCurso(curso);
        liga.setOrientador(prof);
        liga = ligaRepository.save(liga);

        Cargo presidente = new Cargo();
        presidente.setNome("Presidente");
        presidente.setLiga(liga);
        presidente.setPermissoes(EnumSet.allOf(Permissao.class));
        presidente = cargoRepository.save(presidente);

        Aluno maria = new Aluno();
        maria.setUsuario(criarUsuario("Maria Souza", "maria@ucsal.edu.br", Papel.ALUNO));
        maria.setMatricula("2026001");
        maria.setCurso(curso);
        maria = alunoRepository.save(maria);

        MembroLiga membro = new MembroLiga();
        membro.setAluno(maria);
        membro.setLiga(liga);
        membro.setCargo(presidente);
        membro.setStatus(StatusMembro.ATIVO);
        membroLigaRepository.save(membro);
    }

    
}

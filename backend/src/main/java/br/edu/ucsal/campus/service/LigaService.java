package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOLigaRequest;
import br.edu.ucsal.campus.dto.DTOLigaResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Cargo;
import br.edu.ucsal.campus.model.Curso;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Professor;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.SituacaoAcademica;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CargoRepository;
import br.edu.ucsal.campus.repository.CursoRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.ProfessorRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class LigaService {

    private final LigaRepository ligaRepository;
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final CargoRepository cargoRepository;
    private final MembroLigaRepository membroLigaRepository;

    public LigaService(
            LigaRepository ligaRepository,
            CursoRepository cursoRepository,
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            CargoRepository cargoRepository,
            MembroLigaRepository membroLigaRepository) {

        this.ligaRepository = ligaRepository;
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.cargoRepository = cargoRepository;
        this.membroLigaRepository = membroLigaRepository;
    }

    // Identifica o curso coordenado pelo usuário autenticado
    private Curso buscarCursoDoCoordenador(UsuarioAutenticado usuario) {

        Professor coordenador = professorRepository
                .findByUsuarioId(usuario.id())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Professor coordenador não encontrado"));

        Curso curso = cursoRepository.findById(
                coordenador.getCurso().getId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Curso do coordenador não encontrado"));

        if (curso.getCoordenador() == null ||
                !curso.getCoordenador().getId().equals(coordenador.getId())) {

            throw new IllegalArgumentException(
                    "O usuário não é coordenador deste curso");
        }

        return curso;
    }

    // Criar liga vinculada ao curso do coordenador
    @Transactional
    public DTOLigaResponse criar(
            DTOLigaRequest request,
            UsuarioAutenticado usuario) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados da liga são obrigatórios");
        }

        if (request.nome() == null || request.nome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da liga é obrigatório");
        }

        if (request.alunoResponsavelId() == null) {
            throw new IllegalArgumentException(
                    "O aluno responsável pela liga é obrigatório");
        }

        if (ligaRepository.existsByNomeIgnoreCase(request.nome().trim())) {
            throw new IllegalArgumentException(
                    "Já existe uma liga com esse nome");
        }

        Curso curso = buscarCursoDoCoordenador(usuario);

        Aluno alunoResponsavel = alunoRepository
                .findById(request.alunoResponsavelId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Aluno responsável não encontrado"));

        if (alunoResponsavel.getSituacao()
                != SituacaoAcademica.EM_ANDAMENTO) {

            throw new ConflitoException(
                    "O aluno responsável precisa estar com a situação acadêmica EM_ANDAMENTO");
        }

        if (!alunoResponsavel.getCurso().getId().equals(curso.getId())) {
            throw new ConflitoException(
                    "O aluno responsável precisa pertencer ao curso do coordenador");
        }

        Liga liga = new Liga();
        liga.setNome(request.nome().trim());
        liga.setDescricao(request.descricao());
        liga.setCurso(curso);
        liga.setAlunoResponsavel(alunoResponsavel);
        liga.setStatus(StatusLiga.PENDENTE);

        if (request.orientadorId() != null) {
            Professor orientador = professorRepository
                    .findById(request.orientadorId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Orientador não encontrado"));

            liga.setOrientador(orientador);
        }

        return DTOLigaResponse.de(
                ligaRepository.save(liga));
    }

    // Listar ligas aprovadas publicamente
    @Transactional(readOnly = true)
    public List<DTOLigaResponse> listarPublicas() {

        return ligaRepository.findByStatus(StatusLiga.APROVADA)
                .stream()
                .map(DTOLigaResponse::de)
                .toList();
    }

    // Listar ligas pendentes do curso do coordenador
    @Transactional(readOnly = true)
    public List<DTOLigaResponse> listarPendentes(
            UsuarioAutenticado usuario) {

        Curso curso = buscarCursoDoCoordenador(usuario);

        return ligaRepository.findByStatusAndCursoId(
                        StatusLiga.PENDENTE,
                        curso.getId())
                .stream()
                .map(DTOLigaResponse::de)
                .toList();
    }

    // Aprovar liga do próprio curso e criar seu primeiro responsável
    @Transactional
    public DTOLigaResponse aprovar(
            Long id,
            UsuarioAutenticado usuario) {

        Curso curso = buscarCursoDoCoordenador(usuario);

        Liga liga = buscarLigaDoCurso(id, curso.getId());

        if (liga.getStatus() != StatusLiga.PENDENTE) {
            throw new IllegalArgumentException(
                    "Somente ligas pendentes podem ser aprovadas");
        }

        Aluno alunoResponsavel = liga.getAlunoResponsavel();

        if (alunoResponsavel == null) {
            throw new ConflitoException(
                    "A liga não possui um aluno responsável cadastrado");
        }

        if (alunoResponsavel.getSituacao()
                != SituacaoAcademica.EM_ANDAMENTO) {

            throw new ConflitoException(
                    "O aluno responsável não está mais com a situação acadêmica EM_ANDAMENTO");
        }

        if (!alunoResponsavel.getCurso().getId().equals(liga.getCurso().getId())) {
            throw new ConflitoException(
                    "O aluno responsável não pertence ao curso desta liga");
        }

        // Cria o cargo inicial da liga
        Cargo cargo = new Cargo();
        cargo.setNome("Presidente");
        cargo.setLiga(liga);
        cargo.setPermissoes(new HashSet<>(
                Set.of(
                        Permissao.GERENCIAR_MEMBROS,
                        Permissao.GERENCIAR_CARGOS
                )
        ));

        cargo = cargoRepository.save(cargo);

        // Cria ou atualiza o vínculo do aluno responsável
        MembroLiga membroResponsavel = membroLigaRepository
                .findByAlunoIdAndLigaId(
                        alunoResponsavel.getId(),
                        liga.getId()
                )
                .orElseGet(MembroLiga::new);

        membroResponsavel.setAluno(alunoResponsavel);
        membroResponsavel.setLiga(liga);
        membroResponsavel.setCargo(cargo);
        membroResponsavel.setStatus(StatusMembro.ATIVO);

        membroLigaRepository.save(membroResponsavel);

        // Aprova a liga após criar seu primeiro responsável
        liga.setStatus(StatusLiga.APROVADA);

        return DTOLigaResponse.de(ligaRepository.save(liga));
    }

    // Rejeitar liga do próprio curso
    @Transactional
    public DTOLigaResponse rejeitar(
            Long id,
            UsuarioAutenticado usuario) {

        Curso curso = buscarCursoDoCoordenador(usuario);

        Liga liga = buscarLigaDoCurso(id, curso.getId());

        if (liga.getStatus() != StatusLiga.PENDENTE) {
            throw new IllegalArgumentException(
                    "Somente ligas pendentes podem ser rejeitadas");
        }

        liga.setStatus(StatusLiga.REJEITADA);

        return DTOLigaResponse.de(ligaRepository.save(liga));
    }

    private Liga buscarLigaDoCurso(Long id, Long cursoId) {

        Liga liga = ligaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Liga não encontrada"));

        if (!liga.getCurso().getId().equals(cursoId)) {
            throw new IllegalArgumentException(
                    "Esta liga não pertence ao seu curso");
        }

        return liga;
    }
}

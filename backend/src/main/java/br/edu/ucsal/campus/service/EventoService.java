package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Categoria;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CategoriaRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.InscricaoRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.dto.DTOEventoAnaliseResponse;
import br.edu.ucsal.campus.dto.DTOEventoRequest;
import br.edu.ucsal.campus.dto.DTOEventoResponse;
import br.edu.ucsal.campus.dto.DTOPagina;
import br.edu.ucsal.campus.dto.DTOStatusRequest;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final LigaRepository ligaRepository;
    private final NotificacaoService notificacaoService;
    private final InscricaoRepository inscricaoRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;

    // Limite de itens por página na consulta pública
    private static final int TAMANHO_MAXIMO_PAGINA = 50;

    // Usado quando a consulta não informa data final
    private static final LocalDateTime SEM_LIMITE = LocalDateTime.of(9999, 12, 31, 0, 0);

    // Limite do JSON da proposta completa enviado pelo app
    private static final int TAMANHO_MAXIMO_PROPOSTA = 50_000;

    // Lista "IN" nunca vazia (alguns bancos não aceitam IN ())
    private static final List<Long> NENHUMA_LIGA = List.of(-1L);

    public EventoService(
            EventoRepository eventoRepository,
            CategoriaRepository categoriaRepository,
            MembroLigaRepository membroLigaRepository,
            LigaRepository ligaRepository,
            NotificacaoService notificacaoService,
            InscricaoRepository inscricaoRepository,
            AlunoRepository alunoRepository,
            UsuarioRepository usuarioRepository) {

        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.ligaRepository = ligaRepository;
        this.notificacaoService = notificacaoService;
        this.inscricaoRepository = inscricaoRepository;
        this.alunoRepository = alunoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lista todos os eventos (consulta administrativa)
    @Transactional(readOnly = true)
    public List<DTOEventoResponse> listarTodos() {
        return paraResposta(eventoRepository.findAllComCategoria(), null);
    }

    // Busca evento por ID respeitando as regras de acesso
    @Transactional(readOnly = true)
    public DTOEventoResponse buscarPorId(Long id, UsuarioAutenticado usuario) {

        Evento evento = eventoRepository.findDetalhadoById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException("Evento não encontrado"));

        validarAcesso(evento, usuario);

        return paraResposta(List.of(evento), usuario).get(0);
    }

    private void validarAcesso(Evento evento, UsuarioAutenticado usuario) {

        // Eventos aprovados podem ser visualizados publicamente
        if (evento.getStatus() == Evento.StatusEvento.APROVADO) {
            return;
        }

        // Para visitantes, evento não aprovado é tratado como inexistente
        if (usuario == null) {
            throw new EventoNaoEncontradoException("Evento não encontrado");
        }

        // ADMIN e CAEX podem visualizar eventos em qualquer situação
        if (ehCaex(usuario)) {
            return;
        }

        // Quem criou o evento (inclusive professor sem liga) e o orientador da liga
        if (ehCriador(evento, usuario) || ehOrientador(evento.getLiga(), usuario)) {
            return;
        }

        // Os demais precisam ser membros ativos da liga responsável
        if (evento.getLiga() == null
                || membroLigaRepository.buscarMembroAtivo(
                        usuario.id(),
                        evento.getLiga().getId(),
                        StatusMembro.ATIVO).isEmpty()) {

            throw new ConflitoException(
                    "Você não tem permissão para visualizar este evento");
        }
    }

    // ---------- Regras de quem pode o quê (RF27/RF29) ----------

    private boolean temPapel(UsuarioAutenticado usuario, Papel... papeis) {
        if (usuario == null || usuario.papeis() == null) {
            return false;
        }
        for (Papel p : papeis) {
            if (usuario.papeis().contains(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean ehCaex(UsuarioAutenticado usuario) {
        return temPapel(usuario, Papel.CAEX, Papel.ADMIN);
    }

    // Professores e coordenadores podem cadastrar eventos sem vínculo com liga
    private boolean ehInstitucional(UsuarioAutenticado usuario) {
        return temPapel(usuario, Papel.PROFESSOR, Papel.COORDENADOR);
    }

    private boolean ehCriador(Evento evento, UsuarioAutenticado usuario) {
        return evento.getCriadoPor() != null
                && evento.getCriadoPor().getId().equals(usuario.id());
    }

    private boolean ehOrientador(Liga liga, UsuarioAutenticado usuario) {
        return liga != null
                && liga.getOrientador() != null
                && liga.getOrientador().getUsuario() != null
                && liga.getOrientador().getUsuario().getId().equals(usuario.id());
    }

    // Membro ativo da liga cujo cargo tem a permissão (ex.: presidente com CRIAR_EVENTO)
    private boolean temPermissaoNaLiga(Liga liga, UsuarioAutenticado usuario, Permissao permissao) {
        return liga != null
                && membroLigaRepository.buscarMembroAtivo(
                        usuario.id(), liga.getId(), StatusMembro.ATIVO)
                .map(m -> m.getCargo() != null
                        && m.getCargo().getPermissoes().contains(permissao))
                .orElse(false);
    }

    // Quem responde pelo evento: pode ver a proposta completa e reenviá-la após ajustes
    private boolean respondePeloEvento(Evento evento, UsuarioAutenticado usuario) {
        return ehCriador(evento, usuario)
                || ehOrientador(evento.getLiga(), usuario)
                || temPermissaoNaLiga(evento.getLiga(), usuario, Permissao.EDITAR_EVENTO)
                || temPermissaoNaLiga(evento.getLiga(), usuario, Permissao.CRIAR_EVENTO);
    }

    // RF31/RF33 - Eventos aprovados e ainda não encerrados, com filtros opcionais.
    // Aberto a visitantes; para aluno logado, informa também se ele já está inscrito.
    @Transactional(readOnly = true)
    public DTOPagina<DTOEventoResponse> buscarDisponiveis(
            String busca,
            Long categoriaId,
            String modalidade,
            LocalDate inicio,
            LocalDate fim,
            int pagina,
            int tamanho,
            UsuarioAutenticado usuario) {

        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "A data final deve ser posterior ou igual à data inicial");
        }

        // "Disponível" = ainda não terminou; um início no passado não traz eventos encerrados
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime de = inicio == null || inicio.atStartOfDay().isBefore(agora)
                ? agora
                : inicio.atStartOfDay();
        LocalDateTime ate = fim == null ? SEM_LIMITE : fim.plusDays(1).atStartOfDay();

        PageRequest pageable = PageRequest.of(
                Math.max(0, pagina),
                Math.min(Math.max(1, tamanho), TAMANHO_MAXIMO_PAGINA),
                Sort.by("dataInicio").ascending().and(Sort.by("id")));

        Page<Evento> resultado = eventoRepository.buscarComFiltros(
                Evento.StatusEvento.APROVADO,
                de,
                ate,
                categoriaId,
                normalizar(modalidade),
                isVazio(busca) ? "" : "%" + normalizar(busca) + "%",
                pageable);

        return DTOPagina.de(resultado, paraResposta(resultado.getContent(), usuario));
    }

    // Lista eventos por status
    @Transactional(readOnly = true)
    public List<DTOEventoResponse> listarPorStatus(Evento.StatusEvento status) {
        return paraResposta(eventoRepository.findByStatusComCategoria(status), null);
    }

    // Lista somente eventos aprovados de uma categoria
    @Transactional(readOnly = true)
    public List<DTOEventoResponse> listarPorCategoria(Long categoriaId) {

        return paraResposta(eventoRepository.findByCategoriaIdAndStatus(
                categoriaId,
                Evento.StatusEvento.APROVADO
        ), null);
    }

    // Converte para o DTO buscando os inscritos (e a inscrição do aluno) em lote
    private List<DTOEventoResponse> paraResposta(
            List<Evento> eventos,
            UsuarioAutenticado usuario) {

        if (eventos.isEmpty()) {
            return List.of();
        }

        List<Long> ids = eventos.stream().map(Evento::getId).toList();

        Map<Long, Long> inscritos = new HashMap<>();
        for (Object[] linha : inscricaoRepository.contarPorEventos(ids)) {
            inscritos.put((Long) linha[0], ((Number) linha[1]).longValue());
        }

        // null = não se aplica (visitante ou usuário que não é aluno)
        Set<Long> doAluno = null;
        if (usuario != null
                && usuario.papeis() != null
                && usuario.papeis().contains(Papel.ALUNO)) {

            Long alunoId = alunoRepository.findByUsuarioId(usuario.id())
                    .map(aluno -> aluno.getId())
                    .orElse(null);

            if (alunoId != null) {
                doAluno = new HashSet<>(
                        inscricaoRepository.findEventoIdsInscritos(alunoId, ids));
            }
        }

        final Set<Long> inscricoesDoAluno = doAluno;

        return eventos.stream()
                .map(e -> DTOEventoResponse.de(
                        e,
                        inscritos.getOrDefault(e.getId(), 0L),
                        inscricoesDoAluno == null
                                ? null
                                : inscricoesDoAluno.contains(e.getId())))
                .toList();
    }

    private String normalizar(String s) {
        return isVazio(s) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    // RF27 - Cadastra evento acadêmico ou extracurricular.
    // - Pela liga (ligaId informado): presidente ou outro cargo com CRIAR_EVENTO,
    //   ou o professor orientador da liga.
    // - Sem liga: somente professores e coordenadores.
    // Todo evento novo vai para a fila de análise do CAEX (RF29).
    @Transactional
    public DTOEventoAnaliseResponse criar(
            DTOEventoRequest request,
            UsuarioAutenticado usuario) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados do evento são obrigatórios");
        }

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        Usuario autor = usuarioRepository.findById(usuario.id())
                .orElseThrow(() -> new ConflitoException("Usuário não autenticado"));

        Liga liga = null;

        if (request.ligaId() != null) {
            liga = ligaRepository.findById(request.ligaId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Liga não encontrada"));

            if (liga.getStatus() != StatusLiga.APROVADA) {
                throw new ConflitoException(
                        "Somente ligas aprovadas podem cadastrar eventos");
            }

            boolean podeCriar = ehOrientador(liga, usuario)
                    || temPermissaoNaLiga(liga, usuario, Permissao.CRIAR_EVENTO);

            if (!podeCriar) {
                throw new ConflitoException(
                        "Somente a presidência da liga (cargo com permissão de criar eventos) "
                                + "ou o professor orientador podem cadastrar eventos por ela");
            }

        } else if (!ehInstitucional(usuario)) {
            throw new IllegalArgumentException(
                    "Informe a liga responsável pelo evento. "
                            + "Sem liga, somente professores e coordenadores podem cadastrar eventos");
        }

        Evento evento = new Evento();
        preencher(evento, request);
        evento.setLiga(liga);
        evento.setCriadoPor(autor);

        // Todo evento novo começa aguardando análise do CAEX
        evento.setStatus(Evento.StatusEvento.AGUARDANDO_ANALISE);

        Evento salvo = eventoRepository.save(evento);

        notificacaoService.notificarCaex(
                salvo,
                "Novo evento aguardando análise: '" + salvo.getTitulo() + "'"
                        + descreverResponsavel(salvo) + ".");

        return DTOEventoAnaliseResponse.de(salvo);
    }

    // Copia e valida os dados do formulário (cadastro e reenvio)
    private void preencher(Evento evento, DTOEventoRequest request) {

        if (request.categoriaId() == null) {
            throw new IllegalArgumentException("A categoria é obrigatória");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Categoria não encontrada"));

        evento.setTitulo(aparar(request.titulo()));
        evento.setDescricao(aparar(request.descricao()));
        evento.setLocal(aparar(request.local()));
        evento.setDataInicio(request.dataInicio());
        evento.setDataFim(request.dataFim());
        evento.setModalidade(aparar(request.modalidade()));
        evento.setCapacidade(request.capacidade());
        evento.setCategoria(categoria);
        evento.setProposta(isVazio(request.proposta()) ? null : request.proposta());

        validarCamposEvento(evento);

        if (!evento.getDataInicio().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "A data de início do evento deve ser futura");
        }

        if (evento.getProposta() != null
                && evento.getProposta().length() > TAMANHO_MAXIMO_PROPOSTA) {
            throw new IllegalArgumentException(
                    "A proposta excede o tamanho máximo permitido");
        }
    }

    // " (responsável: Fulano, Liga X)" para as mensagens ao CAEX
    private String descreverResponsavel(Evento evento) {
        String nome = evento.getCriadoPor() != null ? evento.getCriadoPor().getNome() : null;
        String liga = evento.getLiga() != null ? evento.getLiga().getNome() : null;

        if (nome == null && liga == null) {
            return "";
        }
        if (nome == null) {
            return " (" + liga + ")";
        }
        return " (responsável: " + nome + (liga != null ? ", " + liga : "") + ")";
    }

    private String aparar(String s) {
        return s == null ? null : s.trim();
    }

    // RF27 - Eventos pelos quais o usuário responde, com a situação na análise do CAEX
    @Transactional(readOnly = true)
    public List<DTOEventoAnaliseResponse> listarDoResponsavel(UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        Set<Long> ligaIds = new HashSet<>(ligaRepository.buscarIdsOrientadasPor(usuario.id()));

        for (MembroLiga m : membroLigaRepository.buscarVinculosDoUsuario(usuario.id(), StatusMembro.ATIVO)) {
            if (m.getCargo() != null
                    && (m.getCargo().getPermissoes().contains(Permissao.CRIAR_EVENTO)
                    || m.getCargo().getPermissoes().contains(Permissao.EDITAR_EVENTO))) {
                ligaIds.add(m.getLiga().getId());
            }
        }

        return eventoRepository.buscarDoResponsavel(
                        usuario.id(),
                        ligaIds.isEmpty() ? NENHUMA_LIGA : List.copyOf(ligaIds))
                .stream()
                .map(DTOEventoAnaliseResponse::de)
                .toList();
    }

    // RF29 - Fila de análise do CAEX (padrão: aguardando análise)
    @Transactional(readOnly = true)
    public List<DTOEventoAnaliseResponse> listarParaAnalise(Evento.StatusEvento status) {
        return eventoRepository.buscarParaAnalise(
                        status == null ? Evento.StatusEvento.AGUARDANDO_ANALISE : status)
                .stream()
                .map(DTOEventoAnaliseResponse::de)
                .toList();
    }

    // RF29 - Detalhe para análise: CAEX/ADMIN ou quem responde pelo evento
    @Transactional(readOnly = true)
    public DTOEventoAnaliseResponse buscarParaAnalise(Long id, UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        Evento evento = eventoRepository.findParaAnaliseById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException("Evento não encontrado"));

        if (!ehCaex(usuario) && !respondePeloEvento(evento, usuario)) {
            throw new ConflitoException(
                    "Você não tem permissão para visualizar a proposta deste evento");
        }

        return DTOEventoAnaliseResponse.de(evento);
    }

    // Reenvia o evento ao CAEX depois de uma solicitação de ajustes.
    // Pode reenviar: quem criou, o orientador ou membro da liga com EDITAR_EVENTO.
    // A liga do evento não muda.
    @Transactional
    public DTOEventoAnaliseResponse atualizarEvento(
            Long id,
            DTOEventoRequest request,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados do evento são obrigatórios");
        }

        Evento evento = eventoRepository.findParaAnaliseById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException("Evento não encontrado"));

        boolean podeEditar = ehCriador(evento, usuario)
                || ehOrientador(evento.getLiga(), usuario)
                || temPermissaoNaLiga(evento.getLiga(), usuario, Permissao.EDITAR_EVENTO);

        if (!podeEditar) {
            throw new ConflitoException(
                    "Você não tem permissão para editar este evento");
        }

        if (evento.getStatus() != Evento.StatusEvento.AJUSTES_SOLICITADOS) {
            throw new ConflitoException(
                    "Somente eventos com ajustes solicitados pelo CAEX podem ser editados");
        }

        preencher(evento, request);

        // Volta para a fila do CAEX. A observação anterior é preservada
        // para que o CAEX confira se os ajustes pedidos foram feitos.
        evento.setStatus(Evento.StatusEvento.AGUARDANDO_ANALISE);
        evento.setEnviadoEm(LocalDateTime.now());

        Evento salvo = eventoRepository.save(evento);

        notificacaoService.notificarCaex(
                salvo,
                "O evento '" + salvo.getTitulo() + "' foi reenviado com os ajustes solicitados"
                        + descreverResponsavel(salvo) + ".");

        return DTOEventoAnaliseResponse.de(salvo);
    }

    private static final Map<Evento.StatusEvento, Set<Evento.StatusEvento>> TRANSICOES = Map.of(
            Evento.StatusEvento.AGUARDANDO_ANALISE,
            EnumSet.of(
                    Evento.StatusEvento.APROVADO,
                    Evento.StatusEvento.AJUSTES_SOLICITADOS,
                    Evento.StatusEvento.REJEITADO
            ),

            Evento.StatusEvento.AJUSTES_SOLICITADOS,
            EnumSet.of(
                    Evento.StatusEvento.AGUARDANDO_ANALISE,
                    Evento.StatusEvento.REJEITADO,
                    Evento.StatusEvento.CANCELADO
            ),

            Evento.StatusEvento.APROVADO,
            EnumSet.of(
                    Evento.StatusEvento.CANCELADO,
                    Evento.StatusEvento.ENCERRADO
            ),

            Evento.StatusEvento.REJEITADO,
            EnumSet.noneOf(Evento.StatusEvento.class),

            Evento.StatusEvento.CANCELADO,
            EnumSet.noneOf(Evento.StatusEvento.class),

            Evento.StatusEvento.ENCERRADO,
            EnumSet.noneOf(Evento.StatusEvento.class)
    );

    // Atualiza status do evento (somente CAEX e ADMIN)
    @Transactional
    public Evento atualizarStatus(
            Long id,
            DTOStatusRequest request,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        // Valida o papel do usuário
        if (!ehCaex(usuario)) {
            throw new ConflitoException(
                    "Somente CAEX ou ADMIN podem atualizar o status do evento");
        }

        if (request == null || request.status() == null) {
            throw new IllegalArgumentException("O status é obrigatório");
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException("Evento não encontrado"));

        Evento.StatusEvento novoStatus = request.status();
        Evento.StatusEvento atual = evento.getStatus();

        if (!TRANSICOES.getOrDefault(atual, Set.of()).contains(novoStatus)) {
            throw new ConflitoException(
                    "Não é possível mudar o status de "
                            + atual + " para " + novoStatus);
        }

        boolean decisaoCaex =
                novoStatus == Evento.StatusEvento.APROVADO
                || novoStatus == Evento.StatusEvento.AJUSTES_SOLICITADOS
                || novoStatus == Evento.StatusEvento.REJEITADO;

        String observacao = request.observacao();

        if (decisaoCaex && (observacao == null || observacao.isBlank())) {
            throw new IllegalArgumentException(
                    "A observação é obrigatória para registrar a decisão do CAEX");
        }

        if (decisaoCaex && observacao.length() > 1000) {
            throw new IllegalArgumentException(
                    "A observação deve ter no máximo 1000 caracteres");
        }

        if (novoStatus == Evento.StatusEvento.APROVADO
                && evento.getDataFim().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Não é possível aprovar um evento que já terminou");
        }

        if (decisaoCaex) {
            evento.setObservacaoCaex(observacao.trim());
        }

        evento.setStatus(novoStatus);

        Evento eventoAtualizado = eventoRepository.save(evento);

        // Avisa quem responde pelo evento (membros ativos da liga e quem o criou)
        if (decisaoCaex) {

            String mensagem = switch (novoStatus) {
                case APROVADO ->
                        "O evento '" + evento.getTitulo()
                                + "' foi aprovado pelo CAEX.";

                case AJUSTES_SOLICITADOS ->
                        "O CAEX solicitou ajustes no evento '"
                                + evento.getTitulo()
                                + "'. Justificativa: "
                                + evento.getObservacaoCaex();

                case REJEITADO ->
                        "O evento '" + evento.getTitulo()
                                + "' foi rejeitado pelo CAEX."
                                + " Justificativa: "
                                + evento.getObservacaoCaex();

                default -> "";
            };

            notificacaoService.notificarResponsaveis(eventoAtualizado, mensagem);
        }

        return eventoAtualizado;
    }

    // Valida os campos obrigatórios do evento
    private void validarCamposEvento(Evento e) {

        if (isVazio(e.getTitulo()) || isVazio(e.getDescricao())
                || isVazio(e.getLocal()) || isVazio(e.getModalidade())) {

            throw new IllegalArgumentException(
                    "Título, descrição, local e modalidade são obrigatórios");
        }

        // Limites das colunas da tabela "eventos"
        validarTamanho(e.getTitulo(), 150, "O título");
        validarTamanho(e.getDescricao(), 2000, "A descrição");
        validarTamanho(e.getLocal(), 150, "O local");
        validarTamanho(e.getModalidade(), 30, "A modalidade");

        if (e.getCategoria() == null || e.getCategoria().getId() == null) {
            throw new IllegalArgumentException("A categoria é obrigatória");
        }

        if (e.getDataInicio() == null || e.getDataFim() == null) {
            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias");
        }

        if (!e.getDataFim().isAfter(e.getDataInicio())) {
            throw new IllegalArgumentException(
                    "A data de fim deve ser posterior à data de início");
        }

        if (e.getCapacidade() != null && e.getCapacidade() <= 0) {
            throw new IllegalArgumentException(
                    "A capacidade deve ser maior que zero");
        }
    }

    private void validarTamanho(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(
                    campo + " deve ter no máximo " + maximo + " caracteres");
        }
    }

    private boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    // Eventos aprovados que acontecem em um dia
    @Transactional(readOnly = true)
    public List<DTOEventoResponse> listarPorData(LocalDate data) {

        if (data == null) {
            throw new IllegalArgumentException("A data é obrigatória");
        }

        return paraResposta(eventoRepository.findByPeriodoAndStatus(
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay(),
                Evento.StatusEvento.APROVADO
        ), null);
    }

    // Eventos aprovados que acontecem dentro de um período
    @Transactional(readOnly = true)
    public List<DTOEventoResponse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "As datas inicial e final são obrigatórias");
        }

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "A data final deve ser posterior ou igual à data inicial");
        }

        return paraResposta(eventoRepository.findByPeriodoAndStatus(
                inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(),
                Evento.StatusEvento.APROVADO
        ), null);
    }
}

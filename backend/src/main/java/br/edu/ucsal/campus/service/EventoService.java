package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOEventoRequest;
import br.edu.ucsal.campus.dto.DTOStatusRequest;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;
import br.edu.ucsal.campus.model.Categoria;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.CategoriaRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final LigaRepository ligaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final AuditoriaService auditoriaService;

    public EventoService(
            EventoRepository eventoRepository,
            CategoriaRepository categoriaRepository,
            MembroLigaRepository membroLigaRepository,
            LigaRepository ligaRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService,
            AuditoriaService auditoriaService) {

        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.ligaRepository = ligaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.auditoriaService = auditoriaService;
    }

    // Lista todos os eventos
    @Transactional(readOnly = true)
    public List<Evento> listarTodos() {
        return eventoRepository.findAllComCategoria();
    }

    // Busca evento por ID respeitando as regras de acesso
    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id, UsuarioAutenticado usuario) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException(
                                "Evento não encontrado"));

        if (evento.getStatus() == Evento.StatusEvento.APROVADO) {
            return evento;
        }

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        if (usuario.papeis() != null
                && (usuario.papeis().contains(Papel.ADMIN)
                || usuario.papeis().contains(Papel.CAEX))) {
            return evento;
        }

        if (evento.getLiga() == null) {
            throw new ConflitoException(
                    "Você não tem permissão para visualizar este evento");
        }

        membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                evento.getLiga().getId(),
                StatusMembro.ATIVO
        ).orElseThrow(() ->
                new ConflitoException(
                        "Você não tem permissão para visualizar este evento"));

        return evento;
    }

    // Lista eventos por status
    @Transactional(readOnly = true)
    public List<Evento> listarPorStatus(Evento.StatusEvento status) {
        return eventoRepository.findByStatus(status);
    }

    // Lista eventos aprovados de uma categoria
    @Transactional(readOnly = true)
    public List<Evento> listarPorCategoria(Long categoriaId) {

        return eventoRepository.findByCategoriaIdAndStatus(
                categoriaId,
                Evento.StatusEvento.APROVADO
        );
    }

    // Cadastra evento pela liga
    @Transactional
    public Evento criar(
            DTOEventoRequest request,
            UsuarioAutenticado usuario) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados do evento são obrigatórios");
        }

        validarUsuario(usuario);

        if (request.ligaId() == null) {
            throw new IllegalArgumentException("A liga é obrigatória");
        }

        if (request.categoriaId() == null) {
            throw new IllegalArgumentException("A categoria é obrigatória");
        }

        Liga liga = ligaRepository.findById(request.ligaId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Liga não encontrada"));

        if (liga.getStatus() != StatusLiga.APROVADA) {
            throw new ConflitoException(
                    "Somente ligas aprovadas podem cadastrar eventos");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                liga.getId(),
                StatusMembro.ATIVO
        ).orElseThrow(() ->
                new ConflitoException(
                        "Você não é um membro ativo desta liga"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.CRIAR_EVENTO)) {

            throw new ConflitoException(
                    "Seu cargo não possui permissão para criar eventos");
        }

        Categoria categoria = categoriaRepository
                .findById(request.categoriaId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Categoria não encontrada"));

        Usuario criador = buscarUsuario(usuario);

        Evento evento = new Evento();

        evento.setTitulo(request.titulo());
        evento.setDescricao(request.descricao());
        evento.setLocal(request.local());
        evento.setDataInicio(request.dataInicio());
        evento.setDataFim(request.dataFim());
        evento.setModalidade(request.modalidade());
        evento.setCapacidade(request.capacidade());
        evento.setCategoria(categoria);
        evento.setLiga(liga);
        evento.setCriadoPor(criador);

        validarCamposEvento(evento);

        evento.setStatus(Evento.StatusEvento.AGUARDANDO_ANALISE);

        Evento eventoSalvo = eventoRepository.save(evento);

        auditoriaService.registrar(
                usuario,
                "CRIAR_EVENTO",
                "EVENTO",
                eventoSalvo.getId(),
                null,
                descreverEvento(eventoSalvo),
                "Criação do evento " + eventoSalvo.getTitulo()
                        + " pela liga " + liga.getNome()
        );

        return eventoSalvo;
    }

    // Atualiza dados do evento após solicitação de ajustes
    @Transactional
    public Evento atualizarEvento(
            Long id,
            Evento dados,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);

        if (dados == null) {
            throw new IllegalArgumentException(
                    "Os dados do evento são obrigatórios");
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException(
                                "Evento não encontrado"));

        if (evento.getStatus()
                != Evento.StatusEvento.AJUSTES_SOLICITADOS) {

            throw new ConflitoException(
                    "Somente eventos com ajustes solicitados podem ser editados pela liga");
        }

        if (evento.getLiga() == null) {
            throw new ConflitoException(
                    "Este evento não está associado a uma liga");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                evento.getLiga().getId(),
                StatusMembro.ATIVO
        ).orElseThrow(() ->
                new ConflitoException(
                        "Você não é um membro ativo da liga responsável pelo evento"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.EDITAR_EVENTO)) {

            throw new ConflitoException(
                    "Seu cargo não possui permissão para editar eventos");
        }

        String valoresAnteriores = descreverEvento(evento);

        // Atualiza somente os campos permitidos
        evento.setTitulo(dados.getTitulo());
        evento.setDescricao(dados.getDescricao());
        evento.setLocal(dados.getLocal());
        evento.setDataInicio(dados.getDataInicio());
        evento.setDataFim(dados.getDataFim());
        evento.setModalidade(dados.getModalidade());
        evento.setCapacidade(dados.getCapacidade());

        // A categoria atual é preservada se não for enviada
        if (dados.getCategoria() != null
                && dados.getCategoria().getId() != null) {

            Categoria categoria = categoriaRepository
                    .findById(dados.getCategoria().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Categoria não encontrada"));

            evento.setCategoria(categoria);
        }

        // Liga, autoria e observação do CAEX não são alteradas
        // pelo conteúdo recebido na requisição.
        validarCamposEvento(evento);

        evento.setStatus(Evento.StatusEvento.AGUARDANDO_ANALISE);

        Evento eventoAtualizado = eventoRepository.save(evento);

        auditoriaService.registrar(
                usuario,
                "EDITAR_EVENTO",
                "EVENTO",
                eventoAtualizado.getId(),
                valoresAnteriores,
                descreverEvento(eventoAtualizado),
                "Atualização do evento após solicitação de ajustes"
        );

        return eventoAtualizado;
    }

    private static final Map<Evento.StatusEvento, Set<Evento.StatusEvento>> TRANSICOES =
            Map.of(
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

    // Atualiza status do evento: somente CAEX e ADMIN
    @Transactional
    public Evento atualizarStatus(
            Long id,
            DTOStatusRequest request,
            UsuarioAutenticado usuario) {

        validarUsuario(usuario);

        if (usuario.papeis() == null
                || (!usuario.papeis().contains(Papel.CAEX)
                && !usuario.papeis().contains(Papel.ADMIN))) {

            throw new ConflitoException(
                    "Somente CAEX ou ADMIN podem atualizar o status do evento");
        }

        if (request == null || request.status() == null) {
            throw new IllegalArgumentException(
                    "O status é obrigatório");
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException(
                                "Evento não encontrado"));

        Evento.StatusEvento novoStatus = request.status();
        Evento.StatusEvento statusAtual = evento.getStatus();

        if (!TRANSICOES.getOrDefault(statusAtual, Set.of())
                .contains(novoStatus)) {

            throw new ConflitoException(
                    "Não é possível mudar o status de "
                            + statusAtual + " para " + novoStatus);
        }

        boolean decisaoCaex =
                novoStatus == Evento.StatusEvento.APROVADO
                || novoStatus == Evento.StatusEvento.AJUSTES_SOLICITADOS
                || novoStatus == Evento.StatusEvento.REJEITADO;

        String observacao = request.observacao();

        if (decisaoCaex
                && (observacao == null || observacao.isBlank())) {

            throw new IllegalArgumentException(
                    "A observação é obrigatória para registrar a decisão do CAEX");
        }

        if (decisaoCaex && observacao.length() > 1000) {
            throw new IllegalArgumentException(
                    "A observação deve ter no máximo 1000 caracteres");
        }

        if (novoStatus == Evento.StatusEvento.APROVADO
                && (evento.getDataFim() == null
                || evento.getDataFim().isBefore(LocalDateTime.now()))) {

            throw new IllegalArgumentException(
                    "Não é possível aprovar um evento que já terminou");
        }

        String valoresAnteriores =
                "status=" + statusAtual
                + "; observacao=" + evento.getObservacaoCaex();

        if (decisaoCaex) {
            evento.setObservacaoCaex(observacao.trim());
        }

        evento.setStatus(novoStatus);

        Evento eventoAtualizado = eventoRepository.save(evento);

        auditoriaService.registrar(
                usuario,
                "ALTERAR_STATUS_EVENTO",
                "EVENTO",
                eventoAtualizado.getId(),
                valoresAnteriores,
                "status=" + novoStatus
                        + "; observacao=" + eventoAtualizado.getObservacaoCaex(),
                "Alteração do status do evento "
                        + eventoAtualizado.getTitulo()
        );

        // Mantém as notificações das decisões do CAEX
        if (decisaoCaex) {

            String mensagem = switch (novoStatus) {
                case APROVADO ->
                        "O evento '" + eventoAtualizado.getTitulo()
                                + "' foi aprovado pelo CAEX.";

                case AJUSTES_SOLICITADOS ->
                        "O CAEX solicitou ajustes no evento '"
                                + eventoAtualizado.getTitulo()
                                + "'. Justificativa: "
                                + eventoAtualizado.getObservacaoCaex();

                case REJEITADO ->
                        "O evento '" + eventoAtualizado.getTitulo()
                                + "' foi rejeitado pelo CAEX."
                                + " Justificativa: "
                                + eventoAtualizado.getObservacaoCaex();

                default -> "";
            };

            notificacaoService.notificarLiga(
                    eventoAtualizado,
                    mensagem
            );
        }

        return eventoAtualizado;
    }

    // Valida os campos obrigatórios do evento
    private void validarCamposEvento(Evento evento) {

        if (isVazio(evento.getTitulo())
                || isVazio(evento.getDescricao())
                || isVazio(evento.getLocal())
                || isVazio(evento.getModalidade())) {

            throw new IllegalArgumentException(
                    "Título, descrição, local e modalidade são obrigatórios");
        }

        if (evento.getCategoria() == null
                || evento.getCategoria().getId() == null) {

            throw new IllegalArgumentException(
                    "A categoria é obrigatória");
        }

        if (evento.getDataInicio() == null
                || evento.getDataFim() == null) {

            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias");
        }

        if (!evento.getDataFim().isAfter(evento.getDataInicio())) {
            throw new IllegalArgumentException(
                    "A data de fim deve ser posterior à data de início");
        }

        if (evento.getCapacidade() != null
                && evento.getCapacidade() <= 0) {

            throw new IllegalArgumentException(
                    "A capacidade deve ser maior que zero");
        }
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void validarUsuario(UsuarioAutenticado usuario) {
        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }
    }

    private Usuario buscarUsuario(UsuarioAutenticado usuario) {
        return usuarioRepository.findById(usuario.id())
                .orElseThrow(() ->
                        new ConflitoException(
                                "Usuário responsável não encontrado"));
    }

    private String descreverEvento(Evento evento) {
        return "titulo=" + evento.getTitulo()
                + "; descricao=" + evento.getDescricao()
                + "; local=" + evento.getLocal()
                + "; dataInicio=" + evento.getDataInicio()
                + "; dataFim=" + evento.getDataFim()
                + "; modalidade=" + evento.getModalidade()
                + "; capacidade=" + evento.getCapacidade()
                + "; categoriaId=" + evento.getCategoria().getId();
    }

    // Eventos aprovados que acontecem em um dia
    @Transactional(readOnly = true)
    public List<Evento> listarPorData(LocalDate data) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data é obrigatória");
        }

        return eventoRepository.findByPeriodoAndStatus(
                data.atStartOfDay(),
                data.plusDays(1).atStartOfDay(),
                Evento.StatusEvento.APROVADO
        );
    }

    // Eventos aprovados dentro de um período
    @Transactional(readOnly = true)
    public List<Evento> listarPorPeriodo(
            LocalDate inicio,
            LocalDate fim) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "As datas inicial e final são obrigatórias");
        }

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "A data final deve ser posterior ou igual à data inicial");
        }

        return eventoRepository.findByPeriodoAndStatus(
                inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(),
                Evento.StatusEvento.APROVADO
        );
    }

    // Lista somente eventos aprovados publicamente
    @Transactional(readOnly = true)
    public List<Evento> listarPublicos() {
        return eventoRepository.findByStatusComCategoria(
                Evento.StatusEvento.APROVADO
        );
    }
}

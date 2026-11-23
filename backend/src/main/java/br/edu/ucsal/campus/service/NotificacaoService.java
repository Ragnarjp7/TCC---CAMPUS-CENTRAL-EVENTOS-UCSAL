package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Notificacao;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.NotificacaoRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoService {

    // Tamanho da coluna "mensagem"
    private static final int TAMANHO_MAXIMO_MENSAGEM = 1000;

    private final NotificacaoRepository notificacaoRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            MembroLigaRepository membroLigaRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void notificarLiga(Evento evento, String mensagem) {

        if (evento.getLiga() == null) {
            return;
        }

        notificar(membrosAtivosDaLiga(evento), evento, mensagem);
    }

    // Avisa quem responde pelo evento: os membros ativos da liga e quem o criou
    // (professor ou coordenador sem liga também recebe), sem repetir ninguém.
    @Transactional
    public void notificarResponsaveis(Evento evento, String mensagem) {

        Map<Long, Usuario> destinatarios = new LinkedHashMap<>();

        if (evento.getLiga() != null) {
            for (Usuario u : membrosAtivosDaLiga(evento)) {
                destinatarios.putIfAbsent(u.getId(), u);
            }
        }

        if (evento.getCriadoPor() != null) {
            destinatarios.putIfAbsent(evento.getCriadoPor().getId(), evento.getCriadoPor());
        }

        notificar(destinatarios.values(), evento, mensagem);
    }

    // RF29 - Avisa o CAEX que há um evento aguardando análise
    @Transactional
    public void notificarCaex(Evento evento, String mensagem) {
        notificar(usuarioRepository.buscarAtivosPorPapel(Papel.CAEX), evento, mensagem);
    }

    private List<Usuario> membrosAtivosDaLiga(Evento evento) {
        return membroLigaRepository.findByLigaId(evento.getLiga().getId())
                .stream()
                .filter(m -> m.getStatus() == StatusMembro.ATIVO)
                .map(m -> m.getAluno().getUsuario())
                .toList();
    }

    private void notificar(Collection<Usuario> usuarios, Evento evento, String mensagem) {

        String texto = mensagem.length() > TAMANHO_MAXIMO_MENSAGEM
                ? mensagem.substring(0, TAMANHO_MAXIMO_MENSAGEM - 1) + "…"
                : mensagem;

        for (Usuario usuario : usuarios) {
            Notificacao notificacao = new Notificacao();
            notificacao.setUsuario(usuario);
            notificacao.setEvento(evento);
            notificacao.setMensagem(texto);

            notificacaoRepository.save(notificacao);
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarPorUsuario(Long usuarioId) {
        return notificacaoRepository
                .findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository
                .countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Long notificacaoId, Long usuarioId) {

        Notificacao notificacao = notificacaoRepository
                .findById(notificacaoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notificação não encontrada"));

        if (!notificacao.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException(
                    "Você não tem permissão para acessar esta notificação");
        }

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }
}

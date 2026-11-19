package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Notificacao;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final MembroLigaRepository membroLigaRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            MembroLigaRepository membroLigaRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.membroLigaRepository = membroLigaRepository;
    }

    @Transactional
    public void notificarLiga(Evento evento, String mensagem) {

        if (evento.getLiga() == null) {
            return;
        }

        List<MembroLiga> membros =
                membroLigaRepository.findByLigaId(evento.getLiga().getId());

        for (MembroLiga membro : membros) {

            if (membro.getStatus() !=
                    br.edu.ucsal.campus.model.enums.StatusMembro.ATIVO) {
                continue;
            }

            Usuario usuario = membro.getAluno().getUsuario();

            Notificacao notificacao = new Notificacao();
            notificacao.setUsuario(usuario);
            notificacao.setEvento(evento);
            notificacao.setMensagem(mensagem);

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

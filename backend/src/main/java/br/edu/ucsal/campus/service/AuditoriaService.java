package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Auditoria;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.repository.AuditoriaRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository,
            UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(
            UsuarioAutenticado usuarioAutenticado,
            String acao,
            String entidade,
            Long registroId,
            String valoresAnteriores,
            String valoresNovos,
            String descricao) {

        if (usuarioAutenticado == null) {
            throw new IllegalArgumentException(
                    "Usuário autenticado é obrigatório para registrar auditoria.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.id())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário responsável pela auditoria não encontrado."));

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAcao(acao);
        auditoria.setEntidade(entidade);
        auditoria.setRegistroId(registroId);
        auditoria.setValoresAnteriores(valoresAnteriores);
        auditoria.setValoresNovos(valoresNovos);
        auditoria.setDescricao(descricao);

        auditoriaRepository.save(auditoria);
    }
}

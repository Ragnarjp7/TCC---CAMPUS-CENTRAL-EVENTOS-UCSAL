
package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOLoginRequest;
import br.edu.ucsal.campus.dto.DTOLoginResponse;
import br.edu.ucsal.campus.dto.DTOMeResponse;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.JwtService;
import br.edu.ucsal.campus.exception.CredenciaisInvalidasException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final MembroLigaRepository membroLigaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            MembroLigaRepository membroLigaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.membroLigaRepository = membroLigaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public DTOLoginResponse login(DTOLoginRequest req) {

        if (req.email() == null || req.senha() == null) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(req.email().trim())
                .filter(Usuario::isAtivo)
                .filter(u -> u.getSenhaHash() != null
                        && passwordEncoder.matches(
                                req.senha(),
                                u.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);

        return new DTOLoginResponse(
                jwtService.gerarToken(usuario),
                "Bearer",
                jwtService.getExpiracaoMinutos(),
                montarPerfil(usuario));
    }

    @Transactional(readOnly = true)
    public DTOMeResponse buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new NaoEncontradoException("Usuário não encontrado"));

        return montarPerfil(usuario);
    }

    // Papéis globais + vínculos ativos em ligas (cargo e permissões)
    private DTOMeResponse montarPerfil(Usuario usuario) {
        return DTOMeResponse.de(
                usuario,
                membroLigaRepository.buscarVinculosDoUsuario(
                        usuario.getId(), StatusMembro.ATIVO));
    }
}

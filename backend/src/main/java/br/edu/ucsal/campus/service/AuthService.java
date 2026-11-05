
package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOLoginRequest;
import br.edu.ucsal.campus.dto.DTOLoginResponse;
import br.edu.ucsal.campus.dto.DTOMeResponse;
import br.edu.ucsal.campus.model.Usuario;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
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
                DTOMeResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public DTOMeResponse buscarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new NaoEncontradoException("Usuário não encontrado"));

        return DTOMeResponse.de(usuario);
    }
}

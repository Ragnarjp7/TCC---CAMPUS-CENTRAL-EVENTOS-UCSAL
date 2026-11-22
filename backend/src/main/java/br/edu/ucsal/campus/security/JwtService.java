package br.edu.ucsal.campus.security;

import br.edu.ucsal.campus.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expiracaoMinutos) {

        byte[] chaveDecodificada;

        try {
            chaveDecodificada = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "A chave JWT deve estar codificada em Base64.",
                e
            );
        }

        if (chaveDecodificada.length < 32) {
            throw new IllegalStateException(
                "A chave JWT deve possuir pelo menos 256 bits."
            );
        }

        this.chave = Keys.hmacShaKeyFor(chaveDecodificada);
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim(
                    "papeis",
                    usuario.getPapeis().stream()
                        .map(Enum::name)
                        .toList()
                )
                .issuedAt(Date.from(agora))
                .expiration(
                    Date.from(
                        agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)
                    )
                )
                .signWith(chave)
                .compact();
    }

    public Long extrairUsuarioId(String token) {
        String subject = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.valueOf(subject);
    }

    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }
}

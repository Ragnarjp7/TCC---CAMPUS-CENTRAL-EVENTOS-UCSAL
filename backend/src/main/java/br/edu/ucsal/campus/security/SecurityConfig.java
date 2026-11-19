
package br.edu.ucsal.campus.security;

import br.edu.ucsal.campus.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwtService,
            UsuarioRepository usuarioRepository) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s ->
                    s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escrever(res, 401,
                                "Autenticação necessária ou token inválido"))
                .accessDeniedHandler((req, res, ex) ->
                        escrever(res, 403,
                                "Você não tem permissão para esta ação")))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()

                // Solicitação de participação em liga
                .requestMatchers(
                        HttpMethod.POST,
                        "/ligas/*/solicitar-participacao"
                ).hasRole("ALUNO")

                // Rotas públicas
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/health",
                        "/eventos/publicos",
                        "/cursos"
                ).permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/categorias",
                        "/categorias/*"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/alunos").permitAll()

                // Aluno logado
                .requestMatchers(
                        HttpMethod.POST,
                        "/eventos/*/inscricoes"
                ).hasRole("ALUNO")
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/eventos/*/inscricoes"
                ).hasRole("ALUNO")
                .requestMatchers(
                        "/alunos/me",
                        "/alunos/me/**"
                ).hasRole("ALUNO")

                // Somente CAEX e Admin
                .requestMatchers(
                        HttpMethod.PUT,
                        "/eventos/*/status"
                ).hasAnyRole("CAEX", "ADMIN")
                .requestMatchers(
                        HttpMethod.POST,
                        "/categorias"
                ).hasAnyRole("CAEX", "ADMIN")
                .requestMatchers(
                        HttpMethod.GET,
                        "/eventos/*/inscricoes"
                ).hasAnyRole("CAEX", "ADMIN")
                .requestMatchers(
                        HttpMethod.GET,
                        "/alunos",
                        "/alunos/*"
                ).hasAnyRole("CAEX", "ADMIN")

                // Cadastro e edição de eventos
                .requestMatchers(
                        HttpMethod.POST,
                        "/eventos"
                ).authenticated()
                .requestMatchers(
                        HttpMethod.PUT,
                        "/eventos/*"
                ).authenticated()

                // Ligas acadêmicas
                .requestMatchers(
                        HttpMethod.GET,
                        "/ligas/publicas"
                ).permitAll()
                .requestMatchers(
                        HttpMethod.POST,
                        "/ligas"
                ).hasRole("COORDENADOR")
                .requestMatchers(
                        HttpMethod.GET,
                        "/ligas/pendentes"
                ).hasRole("COORDENADOR")
                .requestMatchers(
                        HttpMethod.PUT,
                        "/ligas/*/aprovar",
                        "/ligas/*/rejeitar"
                ).hasRole("COORDENADOR")

                // Gerenciamento de membros das ligas
                .requestMatchers(
                        HttpMethod.GET,
                        "/ligas/*/solicitacoes"
                ).authenticated()
                .requestMatchers(
                        HttpMethod.PUT,
                        "/ligas/*/membros/*/aprovar",
                        "/ligas/*/membros/*/recusar"
                ).authenticated()

                // Todo o restante exige autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                    new JwtAuthFilter(jwtService, usuarioRepository),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String[] origens) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origens));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private void escrever(
            HttpServletResponse res,
            int status,
            String mensagem) throws IOException {

        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"mensagem\":\"" + mensagem + "\"}");
    }
}

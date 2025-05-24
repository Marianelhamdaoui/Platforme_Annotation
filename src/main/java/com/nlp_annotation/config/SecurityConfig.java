package com.nlp_annotation.config;


import com.nlp_annotation.model.Annotateur;
import com.nlp_annotation.model.Utilisateur;
import com.nlp_annotation.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/annotator/**").hasRole("ANNOTATEUR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/auth/redirect-by-role", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL de déconnexion
                        .logoutSuccessUrl("/auth/login?logout") // Redirection après déconnexion
                        .invalidateHttpSession(true) // Invalider la session
                        .deleteCookies("JSESSIONID") // Supprimer les cookies
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/auth/access-denied")
                );
        return http.build();
    }
    @Autowired
    private UserDetailsService userDetailsService; // Injectez votre UserDetailsService (par exemple, CustomUserDetailsService)

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                // Charger les détails de l'utilisateur via UserDetailsService
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Vérifier le mot de passe
                if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                    throw new BadCredentialsException("Mot de passe incorrect");
                }

                // Retourner un token d'authentification avec les rôles
                return new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                );
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            request.getSession().setAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            response.sendRedirect("/auth/login?error");
        };
    }


    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/auth/redirect-by-role");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

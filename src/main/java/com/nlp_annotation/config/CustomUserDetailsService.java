package com.nlp_annotation.config;


import com.nlp_annotation.model.Utilisateur;
import com.nlp_annotation.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
        if (utilisateur.isDeleted()) {
            throw new UsernameNotFoundException("Utilisateur supprimé : " + username);
        }
        List<SimpleGrantedAuthority> authorities = utilisateur.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNomRole().toUpperCase()))
                .collect(Collectors.toList());
        return new User(
                utilisateur.getLogin(),
                utilisateur.getPassword(),
                authorities
        );
    }
}

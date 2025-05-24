package com.nlp_annotation.repository;


import com.nlp_annotation.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByLogin(String login);
    List<Utilisateur> findByDeletedFalse();
}
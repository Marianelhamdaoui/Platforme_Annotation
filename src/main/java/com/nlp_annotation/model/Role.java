package com.nlp_annotation.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_role", nullable = false, unique = true)
    private String nomRole;

    @ManyToMany(mappedBy = "roles")
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
    public List<Utilisateur> getUtilisateurs() { return utilisateurs; }
    public void setUtilisateurs(List<Utilisateur> utilisateurs) { this.utilisateurs = utilisateurs; }
}
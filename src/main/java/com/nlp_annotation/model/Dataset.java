package com.nlp_annotation.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset")
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomDataset;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int taille = 0;

    @Column(nullable = false)
    private double avancement = 0.0;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoupleTexte> couples = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassesPossibles> classesPossibles = new ArrayList<>();

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomDataset() { return nomDataset; }
    public void setNomDataset(String nomDataset) { this.nomDataset = nomDataset; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
    public double getAvancement() { return avancement; }
    public void setAvancement(double avancement) { this.avancement = avancement; }
    public List<CoupleTexte> getCouples() { return couples; }
    public void setCouples(List<CoupleTexte> couples) { this.couples = couples; }
    public List<ClassesPossibles> getClassesPossibles() { return classesPossibles; }
    public void setClassesPossibles(List<ClassesPossibles> classesPossibles) { this.classesPossibles = classesPossibles; }
}
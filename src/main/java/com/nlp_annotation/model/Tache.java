package com.nlp_annotation.model;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tache")
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id", nullable = true)
    private Annotateur annotateur;

    @Column(name = "date_limite", nullable = false)
    private LocalDate dateLimite;

    @Column(nullable = false)
    private double avancement = 0.0;

    @Column(nullable = false)
    private int taille = 0;

    @ManyToMany
    @JoinTable(
            name = "tache_couple_texte",
            joinColumns = @JoinColumn(name = "tache_id"),
            inverseJoinColumns = @JoinColumn(name = "couple_texte_id")
    )
    private List<CoupleTexte> assignedCouples = new ArrayList<>();

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Dataset getDataset() { return dataset; }
    public void setDataset(Dataset dataset) { this.dataset = dataset; }
    public Annotateur getAnnotateur() { return annotateur; }
    public void setAnnotateur(Annotateur annotateur) { this.annotateur = annotateur; }
    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }
    public double getAvancement() { return avancement; }
    public void setAvancement(double avancement) { this.avancement = avancement; }
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
    public List<CoupleTexte> getAssignedCouples() { return assignedCouples; }
    public void setAssignedCouples(List<CoupleTexte> assignedCouples) { this.assignedCouples = assignedCouples; }
}

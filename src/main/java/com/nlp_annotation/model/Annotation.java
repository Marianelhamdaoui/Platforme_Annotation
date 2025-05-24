package com.nlp_annotation.model;


import jakarta.persistence.*;

@Entity
@Table(name = "annotation")
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id", nullable = false)
    private Annotateur annotateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_texte_id", nullable = false)
    private CoupleTexte coupleTexte;

    @Column(nullable = false)
    private String classe;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Annotateur getAnnotateur() { return annotateur; }
    public void setAnnotateur(Annotateur annotateur) { this.annotateur = annotateur; }
    public CoupleTexte getCoupleTexte() { return coupleTexte; }
    public void setCoupleTexte(CoupleTexte coupleTexte) { this.coupleTexte = coupleTexte; }
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
}
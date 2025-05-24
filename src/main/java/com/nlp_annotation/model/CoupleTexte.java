package com.nlp_annotation.model;


import jakarta.persistence.*;

@Entity
@Table(name = "couple_texte")
public class CoupleTexte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text1;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    // Constructeur par défaut requis par JPA
    public CoupleTexte() {}

    public CoupleTexte(String text1, String text2, Dataset dataset) {
        this.text1 = text1;
        this.text2 = text2;
        this.dataset = dataset;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText1() { return text1; }
    public void setText1(String text1) { this.text1 = text1; }
    public String getText2() { return text2; }
    public void setText2(String text2) { this.text2 = text2; }
    public Dataset getDataset() { return dataset; }
    public void setDataset(Dataset dataset) { this.dataset = dataset; }
}

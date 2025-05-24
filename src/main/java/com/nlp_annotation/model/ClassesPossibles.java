package com.nlp_annotation.model;


import jakarta.persistence.*;

@Entity
@Table(name = "classes_possibles")
public class ClassesPossibles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texte_class", nullable = false)
    private String texteClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTexteClass() { return texteClass; }
    public void setTexteClass(String texteClass) { this.texteClass = texteClass; }
    public Dataset getDataset() { return dataset; }
    public void setDataset(Dataset dataset) { this.dataset = dataset; }
}

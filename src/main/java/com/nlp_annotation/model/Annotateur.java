package com.nlp_annotation.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "annotateur")
public class Annotateur extends Utilisateur {
}
package com.nlp_annotation.repository;


import com.nlp_annotation.model.ClassesPossibles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassesPossiblesRepository extends JpaRepository<ClassesPossibles, Long> {
    List<ClassesPossibles> findByDatasetId(Long datasetId);
}
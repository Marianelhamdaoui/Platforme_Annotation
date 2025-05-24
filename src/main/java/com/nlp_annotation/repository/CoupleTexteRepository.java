package com.nlp_annotation.repository;


import com.nlp_annotation.model.CoupleTexte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoupleTexteRepository extends JpaRepository<CoupleTexte, Long> {
    List<CoupleTexte> findByDatasetId(Long datasetId);
    Page<CoupleTexte> findByDatasetId(Long datasetId, Pageable pageable);
}

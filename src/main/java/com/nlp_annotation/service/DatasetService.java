package com.nlp_annotation.service;


import com.nlp_annotation.model.*;
import com.nlp_annotation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private ClassesPossiblesRepository classesPossiblesRepository;

    @Autowired
    private CoupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotationRepository annotationRepository;


    public List<Dataset> getAllDatasets() {
        return datasetRepository.findAll();
    }


    public Dataset createDataset(String nomDataset, String description, String classes) {
       // logger.info("Tentative de création du dataset avec nom : {}", nomDataset);

        // Normaliser le nom pour la comparaison (ignorer casse et espaces)
        String normalizedName = nomDataset.trim().toLowerCase();
        Dataset existingDataset = datasetRepository.findByNomDatasetIgnoreCase(normalizedName);
        if (existingDataset != null) {
           // logger.warn("Dataset avec le nom '{}' existe déjà (comparaison insensible à la casse).", normalizedName);
            throw new IllegalArgumentException("Nom de dataset déjà utilisé");
        }

        // Créer le nouveau dataset
        Dataset dataset = new Dataset();
        dataset.setNomDataset(nomDataset.trim()); // Conserver le nom original (juste sans espaces superflus)
        dataset.setDescription(description);
        dataset.setTaille(0);
        dataset.setAvancement(0.0);
        dataset = datasetRepository.save(dataset);

        // Gérer les classes
        String[] classesArray = classes.contains(";") ? classes.split(";") : classes.split(",");
        for (String classe : classesArray) {
            ClassesPossibles cp = new ClassesPossibles();
            cp.setTexteClass(classe.trim());
            cp.setDataset(dataset);
            classesPossiblesRepository.save(cp);
        }

       // logger.info("Dataset '{}' créé avec succès.", dataset.getNomDataset());
        return dataset;
    }

    public Page<CoupleTexte> getCoupleTextesByDatasetId(Long datasetId, int page, int size) {
        return coupleTexteRepository.findByDatasetId(datasetId, PageRequest.of(page, size));
    }

    public List<Utilisateur> getAnnotatorsByDatasetId(Long datasetId) {
        List<Tache> taches = tacheRepository.findByDatasetId(datasetId);
        return taches.stream()
                .map(Tache::getAnnotateur)
                .filter(annotateur -> annotateur != null)
                .distinct()
                .collect(Collectors.toList());
    }

    public void assignAnnotators(Long datasetId, List<Long> annotateurIds, LocalDate dateLimite) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        List<CoupleTexte> couples = coupleTexteRepository.findByDatasetId(datasetId);
        if (couples.isEmpty()) {
            throw new IllegalArgumentException("Aucun couple de texte à assigner");
        }

        Collections.shuffle(couples);
        int totalCouples = couples.size();
        int annotatorsCount = annotateurIds.size();
        if (annotatorsCount == 0) {
            throw new IllegalArgumentException("Aucun annotateur sélectionné");
        }
        int couplesPerAnnotator = totalCouples / annotatorsCount;
        int remainingCouples = totalCouples % annotatorsCount;

        int startIndex = 0;
        for (int i = 0; i < annotatorsCount; i++) {
            int currentCouplesCount = couplesPerAnnotator + (i < remainingCouples ? 1 : 0);
            if (startIndex + currentCouplesCount > totalCouples) break;
            List<CoupleTexte> assignedCouples = couples.subList(startIndex, startIndex + currentCouplesCount);

            Long annotateurId = annotateurIds.get(i);
            Utilisateur utilisateur = utilisateurRepository.findById(annotateurId)
                    .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé"));
            if (utilisateur instanceof Annotateur) {
                Tache tache = tacheRepository.findByAnnotateurIdAndDatasetId(annotateurId, datasetId);
                if (tache == null) {
                    tache = new Tache();
                    tache.setDataset(dataset);
                    tache.setAnnotateur((Annotateur) utilisateur);
                }
                tache.setDateLimite(dateLimite);
                tache.setAssignedCouples(new ArrayList<>(assignedCouples));
                tache.setAvancement(0.0);
                tache.setTaille(assignedCouples.size());
                tacheRepository.save(tache);
            }
            startIndex += currentCouplesCount;
        }

        dataset.setTaille(totalCouples);
        dataset.setAvancement(0.0);
        datasetRepository.save(dataset);
    }

    public String exportDataset(Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        List<CoupleTexte> couples = dataset.getCouples();
        List<Annotation> annotations = annotationRepository.findByCoupleTexte_DatasetId(datasetId);

        StringBuilder csv = new StringBuilder();
        csv.append("Text1,Text2,Class\n");
        for (CoupleTexte couple : couples) {
            String annotationClass = annotations.stream()
                    .filter(a -> a.getCoupleTexte().getId().equals(couple.getId()))
                    .map(Annotation::getClasse)
                    .findFirst()
                    .orElse("");
            csv.append(String.format("\"%s\",\"%s\",\"%s\"\n", couple.getText1(), couple.getText2(), annotationClass));
        }
        return csv.toString();
    }

    public List<ClassesPossibles> getClassesByDatasetId(Long datasetId) {
        return classesPossiblesRepository.findByDatasetId(datasetId);
    }

    public List<Tache> getTachesByAnnotateur(Long annotateurId) {
        return tacheRepository.findByAnnotateurId(annotateurId);
    }



  @Transactional
  public void saveAnnotation(Long tacheId, Long annotateurId, Long coupleId, String classe) {
     // logger.info("Sauvegarde de l'annotation : tacheId={}, annotateurId={}, coupleId={}, classe={}", tacheId, annotateurId, coupleId, classe);

      Utilisateur utilisateur = utilisateurService.findById(annotateurId);
              //.orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé"));
      if (!(utilisateur instanceof Annotateur)) {
          throw new IllegalArgumentException("L'utilisateur avec ID " + annotateurId + " n'est pas un annotateur");
      }
      Annotateur annotateur = (Annotateur) utilisateur;

      CoupleTexte coupleTexte = coupleTexteRepository.findById(coupleId)
              .orElseThrow(() -> new IllegalArgumentException("Couple non trouvé"));

      Optional<Annotation> existingAnnotation = annotationRepository.findByCoupleTexteAndAnnotateur(coupleTexte, annotateur);
      Annotation annotation;

      if (existingAnnotation.isPresent()) {
          annotation = existingAnnotation.get();
          annotation.setClasse(classe);
        //  logger.info("Mise à jour de l'annotation existante pour coupleId={}", coupleId);
      } else {
          annotation = new Annotation();
          annotation.setCoupleTexte(coupleTexte);
          annotation.setAnnotateur(annotateur);
          annotation.setClasse(classe);
         // logger.info("Création d'une nouvelle annotation pour coupleId={}", coupleId);
      }
      annotationRepository.save(annotation);

      Tache tache = tacheRepository.findById(tacheId)
              .orElseThrow(() -> new IllegalArgumentException("Tache non trouvée avec ID " + tacheId));
      List<CoupleTexte> couples = tache.getAssignedCouples();
      if (couples == null || couples.isEmpty()) {
         // logger.warn("Aucun couple assigné à la tâche {}. Avancement non mis à jour.", tacheId);
          return;
      }
     // logger.info("Nombre total de couples assignés à la tâche {} : {}", tacheId, couples.size());
      long annotatedCount = couples.stream()
              .filter(c -> annotationRepository.findByCoupleTexteAndAnnotateur(c, annotateur).isPresent())
              .count();
     // logger.info("Nombre de couples annotés pour la tâche {} : {}", tacheId, annotatedCount);
      double avancementTache = (double) annotatedCount / couples.size() * 100;
      tache.setAvancement(Math.min(avancementTache, 100.0));
      tacheRepository.save(tache);
     // logger.info("Avancement mis à jour pour la tâche {} : {}%", tache.getId(), tache.getAvancement());

      // Mettre à jour l'avancement du dataset
      updateDatasetAvancement(tache.getDataset());
  }

    private void updateDatasetAvancement(Dataset dataset) {
        if (dataset == null) {
            //logger.warn("Dataset null, avancement non mis à jour.");
            return;
        }
        List<Tache> taches = tacheRepository.findByDataset(dataset);
        if (taches.isEmpty()) {
            dataset.setAvancement(0.0);
           // logger.info("Aucune tâche associée au dataset {}. Avancement mis à 0%.", dataset.getId());
        } else {
            double totalAvancement = taches.stream()
                    .mapToDouble(Tache::getAvancement)
                    .average()
                    .orElse(0.0);
            dataset.setAvancement(Math.min(totalAvancement, 100.0));
           // logger.info("Avancement mis à jour pour le dataset {} : {}%", dataset.getId(), dataset.getAvancement());
        }
        datasetRepository.save(dataset);
    }
    public Optional<Dataset> findById(Long id) {
        return datasetRepository.findById(id);
    }

    public List<Annotateur> getAnnotatorsForDataset(Long datasetId) {
        Dataset dataset = findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé avec l'ID " + datasetId));
        List<Tache> taches = tacheRepository.findByDataset(dataset);
        return taches.stream()
                .map(Tache::getAnnotateur)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Annotateur> getAvailableAnnotators(Dataset dataset, Annotateur excludedAnnotator) {
        // Récupérer tous les annotateurs
        List<Utilisateur> allUsers = utilisateurService.findAll();
        List<Annotateur> allAnnotators = allUsers.stream()
                .filter(user -> user instanceof Annotateur)
                .map(user -> (Annotateur) user)
                .collect(Collectors.toList());

        // Récupérer les annotateurs déjà assignés à ce dataset
        List<Annotateur> assignedAnnotators = getAnnotatorsForDataset(dataset.getId());

        // Retourner les annotateurs disponibles (non assignés et différents de l'exclu)
        return allAnnotators.stream()
                .filter(annotator -> !assignedAnnotators.contains(annotator))
                .filter(annotator -> !annotator.getId().equals(excludedAnnotator.getId()))
                .collect(Collectors.toList());
    }
}

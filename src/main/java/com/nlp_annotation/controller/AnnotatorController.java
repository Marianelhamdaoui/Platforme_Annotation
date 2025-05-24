package com.nlp_annotation.controller;


import com.nlp_annotation.model.*;
import com.nlp_annotation.repository.AnnotationRepository;
import com.nlp_annotation.repository.TacheRepository;
import com.nlp_annotation.service.DatasetService;
import com.nlp_annotation.service.UtilisateurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/annotator")
public class AnnotatorController {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatorController.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private AnnotationRepository annotationRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Utilisateur user = utilisateurService.findByLogin(username);
        if (user instanceof Annotateur) {
            model.addAttribute("taches", datasetService.getTachesByAnnotateur(user.getId()));
            return "annotator/dashboard";
        }
        return "redirect:/auth/login?error";
    }

    @GetMapping("/annotate/{tacheId}")
    public String annotate(@PathVariable Long tacheId, @RequestParam(defaultValue = "0") int index, Authentication authentication, Model model) {
        String username = authentication.getName();
        Utilisateur user = utilisateurService.findByLogin(username);
        if (!(user instanceof Annotateur)) {
            return "redirect:/auth/login?error";
        }
        Long annotateurId = user.getId();

        Tache tache = datasetService.getTachesByAnnotateur(annotateurId).stream()
                .filter(t -> t.getId().equals(tacheId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée"));

        if (tache.getAssignedCouples() == null || tache.getAssignedCouples().isEmpty()) {
            return "redirect:/annotator/dashboard";
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= tache.getAssignedCouples().size()) {
            index = tache.getAssignedCouples().size() - 1;
        }

        List<String> classes = datasetService.getClassesByDatasetId(tache.getDataset().getId()).stream()
                .flatMap(cp -> Arrays.stream(cp.getTexteClass().contains(";") ? cp.getTexteClass().split(";") : cp.getTexteClass().split(",")))
                .map(String::trim)
                .collect(Collectors.toList());
        if (classes.isEmpty()) {
            model.addAttribute("errorMessage", "Aucune classe disponible pour ce dataset. Contactez l'administrateur.");
        }

        // Récupérer le couple actuel
        CoupleTexte couple = tache.getAssignedCouples().get(index);

        // Vérifier si une annotation existe déjà pour ce couple et cet annotateur
        Annotateur annotator = (Annotateur) user;
        Optional<Annotation> existingAnnotation = annotationRepository.findByCoupleTexteAndAnnotateur(couple, annotator);
        String selectedClass = existingAnnotation.map(Annotation::getClasse).orElse(null);

        model.addAttribute("tache", tache);
        model.addAttribute("couple", couple);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCouples", tache.getAssignedCouples().size());
        model.addAttribute("datasetClasses", classes);
        model.addAttribute("annotateurId", annotateurId);
        model.addAttribute("selectedClass", selectedClass); // Ajout de la classe sélectionnée
        model.addAttribute("totalCouples", tache.getAssignedCouples().size());
        return "annotator/annotate";
    }


   @PostMapping("/save-annotation")
   public String saveAnnotation(
           @RequestParam("tacheId") Long tacheId,
           @RequestParam("annotateurId") Long annotateurId,
           @RequestParam("coupleId") Long coupleId,
           @RequestParam("classe") String classe,
           @RequestParam("currentIndex") int currentIndex,
           RedirectAttributes redirectAttributes) {
       logger.info("Sauvegarde de l'annotation : tacheId={}, annotateurId={}, coupleId={}, classe={}", tacheId, annotateurId, coupleId, classe);
       try {
           datasetService.saveAnnotation(tacheId, annotateurId, coupleId, classe);

           Tache tache = tacheRepository.findById(tacheId)
                   .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée"));
           int totalCouples = tache.getAssignedCouples().size();
           int nextIndex = currentIndex + 1;

           redirectAttributes.addFlashAttribute("successMessage", "Annotation enregistrée avec succès.");

           if (nextIndex >= totalCouples) {
               return "redirect:/annotator/annotate/" + tacheId;
           }
           return "redirect:/annotator/annotate/" + tacheId + "?index=" + nextIndex;
       } catch (Exception e) {
           logger.error("Erreur lors de l'enregistrement des annotations : {}", e.getMessage(), e);
           redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'enregistrement : " + e.getMessage());
           return "redirect:/annotator/annotate/" + tacheId + "?index=" + currentIndex;
       }
   }
}

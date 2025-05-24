package com.nlp_annotation.controller;


import com.nlp_annotation.model.*;
import com.nlp_annotation.repository.TacheRepository;
import com.nlp_annotation.service.DatasetService;
import com.nlp_annotation.service.UtilisateurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private TacheRepository tacheRepository;



    @Autowired
    private com.nlp_annotation.repository.CoupleTexteRepository coupleTexteRepository;

    @Autowired
    private com.nlp_annotation.repository.DatasetRepository datasetRepository;


   @GetMapping("/dashboard")
   public String adminDashboard(Model model) {
       List<Dataset> datasets = datasetRepository.findAll();
       // Créer une Map pour stocker l'état de désactivation du bouton
       Map<Long, Boolean> disableAddAnnotatorButtonMap = new HashMap<>();
       for (Dataset dataset : datasets) {
           boolean hasAssignedAnnotators = tacheRepository.existsByDatasetAndAnnotateurIsNotNull(dataset);
           boolean isCompleted = dataset.getAvancement() == 100.0; // Vérifier si l'avancement est à 100%
           // Désactiver le bouton si des annotateurs sont affectés OU si l'avancement est à 100%
           disableAddAnnotatorButtonMap.put(dataset.getId(), hasAssignedAnnotators || isCompleted);
       }
       model.addAttribute("datasets", datasets);
       model.addAttribute("disableAddAnnotatorButtonMap", disableAddAnnotatorButtonMap);
       return "admin/dashboard";
   }

    @GetMapping("/create-user")
    public String createUserForm(Model model) {
        model.addAttribute("newUser", new Utilisateur());
        return "admin/create-user";
    }

    @PostMapping("/create-user")
    public String createUser(
            @RequestParam String login,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.createUser(login, nom, prenom, email, "ANNOTATEUR");
            redirectAttributes.addFlashAttribute("successMessage", "Annotateur créé avec succès. Un e-mail a été envoyé avec les identifiants.");
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'annotateur : {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création de l'annotateur : " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(Model model) {
        model.addAttribute("users", utilisateurService.getAnnotators());
        return "admin/manage-users";
    }

    @GetMapping("/edit-user/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", utilisateurService.findById(id));
        return "admin/edit-user";
    }

    @PostMapping("/edit-user/{id}")
    public String editUser(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String login,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.updateUser(id, login, nom, prenom, email);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur modifié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification : " + e.getMessage());
        }
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Utilisateur marqué comme supprimé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/create-dataset")
    public String createDatasetForm(Model model) {
        model.addAttribute("newDataset", new Dataset());
        return "admin/create-dataset";
    }

    @PostMapping("/create-dataset")
    public String createDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam String nomDataset,
            @RequestParam String description,
            @RequestParam String classes,
            RedirectAttributes redirectAttributes) {
        try {
            datasetService.createDataset(nomDataset, description, classes);

            Dataset dataset = datasetService.getAllDatasets().stream()
                    .filter(d -> d.getNomDataset().equals(nomDataset))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Dataset non créé"));

            if (!file.isEmpty()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] texts = line.split(",");
                        if (texts.length == 2 && !texts[0].trim().isEmpty() && !texts[1].trim().isEmpty()) {
                            CoupleTexte couple = new CoupleTexte(texts[0].trim(), texts[1].trim(), dataset);
                            coupleTexteRepository.save(couple);
                        }
                    }
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Dataset créé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création du dataset : " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dataset-details/{id}")
    public String datasetDetails(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, Model model) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        Page<CoupleTexte> couples = datasetService.getCoupleTextesByDatasetId(id, page, 10); // Limite à 10 par page
        model.addAttribute("dataset", dataset);
        model.addAttribute("couples", couples.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", couples.getTotalPages());
        model.addAttribute("classes", datasetService.getClassesByDatasetId(id));
        return "admin/dataset-details";
    }

    @GetMapping("/dataset-annotators/{id}")
    public String datasetAnnotators(@PathVariable Long id, Model model) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        model.addAttribute("dataset", dataset);
        model.addAttribute("annotators", datasetService.getAnnotatorsByDatasetId(id));
        return "admin/dataset-annotators";
    }

    @GetMapping("/assign-annotators/{datasetId}")
    public String assignAnnotatorsForm(@PathVariable Long datasetId, Model model) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        List<Utilisateur> annotateurs = utilisateurService.getAnnotators();
        model.addAttribute("dataset", dataset);
        model.addAttribute("annotateurs", annotateurs);
        return "admin/assign-annotators";
    }

    @PostMapping("/assign-annotators/{datasetId}")
    public String assignAnnotators(
            @PathVariable Long datasetId,
            @RequestParam("annotateurIds") List<Long> annotateurIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateLimite,
            RedirectAttributes redirectAttributes) {
        try {
            datasetService.assignAnnotators(datasetId, annotateurIds, dateLimite);
            redirectAttributes.addFlashAttribute("successMessage", "Annotateurs assignés avec succès.");
        } catch (Exception e) {
            logger.error("Erreur lors de l'assignation des annotateurs : {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'assignation des annotateurs : " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/export-dataset/{id}")
    public String exportDataset(@PathVariable Long id, Model model) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        String csvContent = datasetService.exportDataset(id);
        model.addAttribute("csvContent", csvContent);
        model.addAttribute("dataset", dataset);
        return "admin/export-dataset";
    }

    @GetMapping("/export-dataset/{id}/download")
    public void downloadDataset(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé"));
        String csvContent = datasetService.exportDataset(id);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + dataset.getNomDataset() + ".csv\"");
        response.getWriter().write(csvContent);
    }


    @PostMapping("/unassign-annotator/{datasetId}/{annotatorId}")
    public String unassignAnnotator(
            @PathVariable("datasetId") Long datasetId,
            @PathVariable("annotatorId") Long annotatorId,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            Dataset dataset = datasetService.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé avec l'ID " + datasetId));
            Annotateur annotator = (Annotateur) utilisateurService.findById(annotatorId);
                   // .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé avec l'ID " + annotatorId));

            Tache tache = tacheRepository.findByDatasetAndAnnotateur(dataset, annotator)
                    .orElseThrow(() -> new IllegalArgumentException("Aucune tâche trouvée pour cet annotateur et ce dataset"));


            tache.setAnnotateur(null);
            tacheRepository.save(tache);


            if (tache.getAvancement() < 100.0) {
                List<Annotateur> availableAnnotators = datasetService.getAvailableAnnotators(dataset, annotator);
                if (availableAnnotators.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun autre annotateur disponible pour réaffecter la tâche.");
                    return "redirect:/admin/dataset-annotators/" + datasetId;
                }
                model.addAttribute("dataset", dataset);
                model.addAttribute("tacheId", tache.getId());
                model.addAttribute("availableAnnotators", availableAnnotators);
                return "admin/select-new-annotator";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "L'annotateur a été désaffecté avec succès.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la désaffectation : " + e.getMessage());
        }
        return "redirect:/admin/dataset-annotators/" + datasetId;
    }

    @PostMapping("/assign-new-annotator/{datasetId}/{tacheId}")
    public String assignNewAnnotator(
            @PathVariable("datasetId") Long datasetId,
            @PathVariable("tacheId") Long tacheId,
            @RequestParam("newAnnotatorId") Long newAnnotatorId,
            RedirectAttributes redirectAttributes) {
        try {
            Dataset dataset = datasetService.findById(datasetId)
                    .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé avec l'ID " + datasetId));
            Tache tache = tacheRepository.findById(tacheId)
                    .orElseThrow(() -> new IllegalArgumentException("Tâche non trouvée avec l'ID " + tacheId));
            Annotateur newAnnotator = (Annotateur) utilisateurService.findById(newAnnotatorId);
                    //.orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé avec l'ID " + newAnnotatorId));

            // Réaffecter la tâche
            tache.setAnnotateur(newAnnotator);
            tacheRepository.save(tache);

            redirectAttributes.addFlashAttribute("successMessage",
                    "La tâche a été réaffectée à " + newAnnotator.getNom() + " " + newAnnotator.getPrenom() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la réaffectation : " + e.getMessage());
        }
        return "redirect:/admin/dataset-annotators/" + datasetId;
    }
}

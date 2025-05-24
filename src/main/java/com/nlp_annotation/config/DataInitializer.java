package com.nlp_annotation.config;


import com.nlp_annotation.model.Administrateur;
import com.nlp_annotation.model.Role;
import com.nlp_annotation.repository.RoleRepository;
import com.nlp_annotation.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialisation des rôles
        Role adminRole = roleRepository.findByNomRole("ADMINISTRATEUR");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setNomRole("ADMINISTRATEUR");
            roleRepository.save(adminRole);
            logger.info("Rôle ADMINISTRATEUR créé.");
        }

        Role annotatorRole = roleRepository.findByNomRole("ANNOTATEUR");
        if (annotatorRole == null) {
            annotatorRole = new Role();
            annotatorRole.setNomRole("ANNOTATEUR");
            roleRepository.save(annotatorRole);
            logger.info("Rôle ANNOTATEUR créé.");
        }

        // Création de l'administrateur par défaut
        if (utilisateurRepository.findByLogin("admin").isEmpty()) {
            Administrateur admin = new Administrateur();
            admin.setLogin("admin");
            admin.setNom("Admin");
            admin.setPrenom("Principal");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDeleted(false);
            admin.setRoles(java.util.Arrays.asList(adminRole));
            utilisateurRepository.save(admin);
            logger.info("Administrateur créé avec succès : login=admin, mot de passe=admin123");
        } else {
            logger.info("Administrateur avec login 'admin' existe déjà.");
        }
    }
}
package com.nlp_annotation.controller;

import com.nlp_annotation.model.*;
import com.nlp_annotation.model.Utilisateur;
import com.nlp_annotation.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }

    @GetMapping("/redirect-by-role")
    public String redirectByRole(Authentication authentication) {
        String username = authentication.getName();
        Utilisateur user = utilisateurService.findByLogin(username);
        if (user instanceof Administrateur) {
            return "redirect:/admin/dashboard";
        } else if (user instanceof Annotateur) {
            return "redirect:/annotator/dashboard";
        }
        return "redirect:/auth/login?error";
    }
}

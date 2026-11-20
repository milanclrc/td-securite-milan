package com.example.banque.utilisateur.out;

import com.example.banque.utilisateur.Utilisateur;
import com.example.banque.utilisateur.UtilisateurRepository;
import jakarta.validation.constraints.Null;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Faux utilisateur courant : tout le monde est « app.utilisateur-fake » (alice par défaut).
 * Il sert tant que l'appli n'a pas de sécurité. À remplacer par un UtilisateurCourant
 * qui lit le SecurityContext (supprimez alors cette classe).
 */
@Component
public class UtilisateurCourantEntity implements UtilisateurCourant {

    private final UtilisateurRepository utilisateurs;

    public UtilisateurCourantEntity(UtilisateurRepository utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @Override
    public Optional<Utilisateur> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return Optional.empty();
    }
}

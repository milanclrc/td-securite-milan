package com.example.banque.config;

import com.example.banque.compte.Agence;
import com.example.banque.compte.AgenceRepository;
import com.example.banque.compte.Compte;
import com.example.banque.compte.CompteRepository;
import com.example.banque.utilisateur.*;
import com.example.banque.virement.Statut;
import com.example.banque.virement.Virement;
import com.example.banque.virement.VirementRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée au démarrage les utilisateurs, comptes et virements décrits dans la conf.
 * Rejouable : un utilisateur qui existe déjà est laissé tel quel. Fourni : à ne pas toucher.
 * Il hache avec son propre BCrypt : le bean PasswordEncoder de l'appli, c'est au TD de le déclarer.
 */
@Component
public class DonneesDemo implements ApplicationRunner {

    private final DemoProperties demo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UtilisateurRepository utilisateurs;
    private final AgenceRepository agences;
    private final ConseillerRepository conseillers;
    private final ClientRepository clients;
    private final CompteRepository comptes;
    private final VirementRepository virements;

    public DonneesDemo(DemoProperties demo, UtilisateurRepository utilisateurs,
                       AgenceRepository agences, ConseillerRepository conseillers, ClientRepository clients,
                       CompteRepository comptes, VirementRepository virements) {
        this.demo = demo;
        this.utilisateurs = utilisateurs;
        this.agences = agences;
        this.conseillers = conseillers;
        this.clients = clients;
        this.comptes = comptes;
        this.virements = virements;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        demo.agences().stream()
                .filter(a -> agences.findByNom(a.nom()).isEmpty())
                .forEach(a -> agences.save(new Agence(a.nom())));

        for (var c : demo.conseillers()) {
            if (utilisateurs.existsByLogin(c.login())) {
                continue;
            }
            var utilisateur = new Utilisateur(c.login(), encoder.encode(c.motDePasse()), Role.CONSEILLER);
            conseillers.save(new Conseiller(utilisateur, c.nom(), agence(c.agence())));
        }

        for (var c : demo.clients()) {
            if (utilisateurs.existsByLogin(c.login())) {
                continue;
            }
            var utilisateur = new Utilisateur(c.login(), encoder.encode(c.motDePasse()), Role.CLIENT);
            var adresse = new Adresse(c.adresse().rue(), c.adresse().ville(), c.adresse().codePostal());
            var client = clients.save(new Client(utilisateur, c.nom(), adresse));
            for (var cc : c.comptes()) {
                var compte = comptes.save(new Compte(cc.numero(), agence(cc.agence()), client));
                for (var v : cc.virements()) {
                    var virement = new Virement(compte, v.iban(), v.montant());
                    if (v.statut() == Statut.VALIDE) {
                        virement.valider(v.validePar());
                    }
                    virements.save(virement);
                }
            }
        }
    }

    private Agence agence(String nom) {
        return agences.findByNom(nom).orElseThrow(() -> new IllegalStateException("Agence inconnue : " + nom));
    }
}

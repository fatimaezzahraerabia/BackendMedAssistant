package com.rabia.backendmedassistant.config;

import com.opencsv.CSVReader;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Role;
import com.rabia.backendmedassistant.model.Specialite;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final SpecialiteRepository specialiteRepository;
    int counter = 1; // compteur global

    @Autowired
    public DataInitializer(MedecinRepository medecinRepository, UtilisateurRepository utilisateurRepository, SpecialiteRepository specialiteRepository) {
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.specialiteRepository = specialiteRepository;
    }

    @Override
    public void run(String... args) throws Exception {

    /*    try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/dataset_medecins_final.csv"))) {
            String[] line;
            boolean isHeader = true;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String nom = line[0].trim();
                String prenom = line[1].trim();
                String specialiteNom = line[2].trim();
                String adresse = line[3].trim();

                Double lat = null;
                Double lng = null;
                try {
                    lat = Double.parseDouble(line[4]);
                    lng = Double.parseDouble(line[5]);
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Coordonnées invalides pour " + nom + " " + prenom);
                }

                // Vérifier ou créer la spécialité
                Specialite sp = specialiteRepository.findByNom(specialiteNom);
                if (sp == null) {
                    sp = new Specialite();
                    sp.setNom(specialiteNom);
                    specialiteRepository.save(sp);
                }

                // Création utilisateur
                String email = "med" + counter + "@gmail.com";
                String motDePasseClair = "password123";
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setEmail(email);
                utilisateur.setMotDePasse(encoder.encode(motDePasseClair));
                utilisateur.setRole(Role.MEDECIN);
                utilisateurRepository.save(utilisateur);

                // Création médecin
                Medecin medecin = new Medecin();
                medecin.setNom(nom);
                medecin.setPrenom(prenom);
                medecin.setAdresseCabinet(adresse);
                medecin.setLat(lat);
                medecin.setLng(lng);
                medecin.setSpecialite(sp);
                medecin.setUtilisateur(utilisateur); // 🔗 lien avec utilisateur

                medecinRepository.save(medecin);

                System.out.println("✅ Médecin " + nom + " créé avec email: " + email + " / mot de passe: " + motDePasseClair);

                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } */

    }
}

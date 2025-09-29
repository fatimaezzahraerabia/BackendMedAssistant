package com.rabia.backendmedassistant.config;

import com.opencsv.CSVReader;
import java.io.FileReader;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Role;
import com.rabia.backendmedassistant.model.Specialite;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.model.Ville;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import com.rabia.backendmedassistant.repository.VilleRepository;
import com.rabia.backendmedassistant.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileReader;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SpecialiteRepository specialiteRepository;
    private final VilleRepository villeRepository;
    private final GeocodingService geocodingService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    int counter = 1;

    @Autowired
    public DataInitializer(MedecinRepository medecinRepository, UtilisateurRepository utilisateurRepository,
                           SpecialiteRepository specialiteRepository, VilleRepository villeRepository,
                           GeocodingService geocodingService) {
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.specialiteRepository = specialiteRepository;
        this.villeRepository = villeRepository;
        this.geocodingService = geocodingService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (medecinRepository.count() > 0) {
            System.out.println("La base de données des médecins est déjà initialisée.");
            return;
        }

   /*      try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/dataset_medecins_final.csv"))) {
            String[] line;
            boolean isHeader = true;

            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String nom = line[0].trim();
                String prenom = line[1].trim();
                String specialiteNom = line[2].trim();
                String adresse = line[3].trim();
                String villeNom = line[6].trim();

                Double lat = null;
                Double lng = null;
                try {
                    lat = Double.parseDouble(line[4]);
                    lng = Double.parseDouble(line[5]);
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Coordonnées invalides pour " + nom + " " + prenom);
                }

                // Gestion ville
                Ville ville = villeRepository.findByNom(villeNom);
                if (ville == null) {
                    ville = new Ville();
                    ville.setNom(villeNom);

                    double[] coords = geocodingService.geocode(villeNom);
                    ville.setLat(coords[0]);
                    ville.setLng(coords[1]);

                    villeRepository.save(ville);
                    System.out.println("✅ Ville ajoutée : " + villeNom + " (" + coords[0] + ", " + coords[1] + ")");
                }

                // Gestion spécialité
                Specialite sp = specialiteRepository.findByNom(specialiteNom);
                if (sp == null) {
                    sp = new Specialite();
                    sp.setNom(specialiteNom);
                    specialiteRepository.save(sp);
                }

                // Création utilisateur
                String email = "med" + counter + "@gmail.com";
                String motDePasseClair = "password123";
                Utilisateur utilisateur; // Declare utilisateur here

                // Check if user already exists before saving
                if (!utilisateurRepository.existsByEmail(email)) {
                    utilisateur = new Utilisateur(); // Initialize if not exists
                    utilisateur.setEmail(email);
                    utilisateur.setMotDePasse(encoder.encode(motDePasseClair));
                    utilisateur.setRole(Role.MEDECIN);
                    utilisateur = utilisateurRepository.save(utilisateur); // Save and get the persisted object with ID
                    System.out.println("✅ Utilisateur créé : " + email + " (ID: " + utilisateur.getId() + ")");
                } else {
                    // If user exists, retrieve it to link with Medecin
                    utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
                    if (utilisateur == null) { // Should not happen if existsByEmail is true, but for safety
                        System.err.println("Error: User with email " + email + " reported to exist but could not be retrieved.");
                        continue; // Skip this iteration if user retrieval fails
                    }
                    System.out.println("ℹ️ Utilisateur existe déjà : " + email + " (ID: " + utilisateur.getId() + ")");
                }

                // Création médecin
                // Ensure utilisateur is not null before proceeding
                if (utilisateur != null) {
                    Medecin medecin = new Medecin();
                    medecin.setNom(nom);
                    medecin.setPrenom(prenom);
                    medecin.setAdresseCabinet(adresse);
                    medecin.setLat(lat);
                    medecin.setLng(lng);
                    medecin.setSpecialite(sp);
                    medecin.setVille(ville);
                    medecin.setUtilisateur(utilisateur); // Lien avec utilisateur

                    medecinRepository.save(medecin);
                    System.out.println("✅ Médecin " + nom + " créé avec email: " + email + " / mot de passe: " + motDePasseClair);
                } else {
                    System.err.println("Skipping Medecin creation for " + nom + " " + prenom + " due to missing utilisateur.");
                }

                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }   */
    }
}

package com.rabia.backendmedassistant.config;

import com.opencsv.CSVReader;
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
     /*   try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/dataset_medecins_final.csv"))) {
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
                medecin.setVille(ville);
                medecin.setUtilisateur(utilisateur); // Lien avec utilisateur

                medecinRepository.save(medecin);
                System.out.println("✅ Médecin " + nom + " créé avec email: " + email + " / mot de passe: " + motDePasseClair);

                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } */
    }
}

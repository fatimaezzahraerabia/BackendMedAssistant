package com.rabia.backendmedassistant.config;

import com.opencsv.CSVReader;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Specialite;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedecinRepository medecinRepository;
    private final SpecialiteRepository specialiteRepository;
    int counter = 1; // compteur global


    @Autowired
    public DataInitializer(MedecinRepository medecinRepository, SpecialiteRepository specialiteRepository) {
        this.medecinRepository = medecinRepository;
        this.specialiteRepository = specialiteRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/dataset_medecins_final.csv"))) {
            String[] line;
            boolean isHeader = true;

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            while ((line = reader.readNext()) != null) {
                if (isHeader) { // ignorer l'entête
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

                // Vérifier si la spécialité existe déjà
                Specialite sp = specialiteRepository.findByNom(specialiteNom);
                if (sp == null) {
                    sp = new Specialite();
                    sp.setNom(specialiteNom);
                    specialiteRepository.save(sp);
                }

                Medecin medecin = new Medecin();
                medecin.setNom(nom);
                medecin.setPrenom(prenom);
                medecin.setAdresseCabinet(adresse);
                medecin.setLat(lat);
                medecin.setLng(lng);
                medecin.setSpecialite(sp);
                String email = "med" + counter + "@gmail.com";
                medecin.setEmail(email);
                counter++;
                // Générer mot de passe par défaut hashé
                String motDePasseClair = "password123";
                medecin.setMotDePasse(encoder.encode(motDePasseClair)); // ✅ hash bcrypt
                String motDePasseHash = encoder.encode(motDePasseClair);
                medecin.setMotDePasse(motDePasseHash);
                System.out.println("Mot de passe hashé : " + motDePasseHash);

                medecinRepository.save(medecin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

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
import java.util.ArrayList;
import java.util.Arrays;
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

        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/doctorsDataset.csv"))) {
            List<String[]> lines = reader.readAll();

            // Optionnel : sauter la ligne d'en-tête
            lines.remove(0);


            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            for (String[] line : lines) {
                Medecin medecin = new Medecin();
                medecin.setNom(line[0]);
                medecin.setPrenom(line[1]);
                medecin.setAdresseCabinet(line[3]);

                // Latitude / Longitude
                try {
                    medecin.setLat(Double.parseDouble(line[4]));
                    medecin.setLng(Double.parseDouble(line[5]));
                } catch (NumberFormatException e) {
                    medecin.setLat(null);
                    medecin.setLng(null);
                }

                // Spécialité
                String specialiteNom = line[2].trim();
                Specialite sp = specialiteRepository.findByNom(specialiteNom);
                if (sp == null) {
                    sp = new Specialite();
                    sp.setNom(specialiteNom);
                    specialiteRepository.save(sp);
                }
                medecin.setSpecialite(sp);
                // ✅ Générer un email unique
                String email = "med" + counter + "@gmail.com";
                medecin.setEmail(email);
                counter++;
                // Générer mot de passe par défaut hashé
                String motDePasseClair = "password123";
                medecin.setMotDePasse(encoder.encode(motDePasseClair)); // ✅ hash bcrypt
                System.out.println("Mot de passe hashé : " + encoder.encode(motDePasseClair));

                medecinRepository.save(medecin);
                medecinRepository.flush();  // force Hibernate à exécuter l'INSERT immédiatement
                System.out.println("Medecin sauvegardé avec mot de passe : " + medecin.getMotDePasse());
            }
        }




//


    }
}

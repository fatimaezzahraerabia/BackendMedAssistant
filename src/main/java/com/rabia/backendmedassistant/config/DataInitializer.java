package com.rabia.backendmedassistant.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SpecialiteRepository specialiteRepository;
    private final VilleRepository villeRepository;
    private final GeocodingService geocodingService;
    private final BCryptPasswordEncoder encoder;
    private final ResourceLoader resourceLoader;

    @Autowired
    public DataInitializer(MedecinRepository medecinRepository, UtilisateurRepository utilisateurRepository,
                           SpecialiteRepository specialiteRepository, VilleRepository villeRepository,
                           GeocodingService geocodingService, ResourceLoader resourceLoader,
                           BCryptPasswordEncoder encoder) {
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.specialiteRepository = specialiteRepository;
        this.villeRepository = villeRepository;
        this.geocodingService = geocodingService;
        this.resourceLoader = resourceLoader;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (medecinRepository.count() > 0) {
            logger.info("La base de données des médecins est déjà initialisée.");
            return;
        }

        Resource resource = resourceLoader.getResource("classpath:dataset_medecins_final.csv");
        if (!resource.exists()) {
            logger.error("Fichier CSV introuvable : dataset_medecins_final.csv");
            throw new FileNotFoundException("Fichier CSV introuvable : dataset_medecins_final.csv");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            boolean isHeader = true;
            int counter = 1;

            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Validate CSV line length
                if (line.length < 7) {
                    logger.warn("Ligne CSV incomplète, ignorée : {}", String.join(",", line));
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
                    logger.warn("Coordonnées invalides pour {} {} : {}", nom, prenom, e.getMessage());
                }

                // Gestion ville
                Ville ville = villeRepository.findByNom(villeNom);
                if (ville == null) {
                    ville = new Ville();
                    ville.setNom(villeNom);
                    try {
                        double[] coords = geocodingService.geocode(villeNom);
                        ville.setLat(coords[0]);
                        ville.setLng(coords[1]);
                        villeRepository.save(ville);
                        logger.info("Ville ajoutée : {} ({}, {})", villeNom, coords[0], coords[1]);
                    } catch (Exception e) {
                        logger.error("Erreur lors du géocodage de {} : {}", villeNom, e.getMessage());
                        continue;
                    }
                }

                // Gestion spécialité
                Specialite sp = specialiteRepository.findByNom(specialiteNom);
                if (sp == null) {
                    sp = new Specialite();
                    sp.setNom(specialiteNom);
                    specialiteRepository.save(sp);
                    logger.info("Spécialité ajoutée : {}", specialiteNom);
                }

                // Création utilisateur
                String email = "med" + counter + "@gmail.com";
                String motDePasseClair = "password123"; // Consider a more secure approach
                Utilisateur utilisateur;

                if (!utilisateurRepository.existsByEmail(email)) {
                    utilisateur = new Utilisateur();
                    utilisateur.setEmail(email);
                    utilisateur.setMotDePasse(encoder.encode(motDePasseClair));
                    utilisateur.setRole(Role.MEDECIN);
                    utilisateur = utilisateurRepository.save(utilisateur);
                    logger.info("Utilisateur créé : {} (ID: {})", email, utilisateur.getId());
                } else {
                    utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
                    if (utilisateur == null) {
                        logger.error("Utilisateur avec email {} existe mais introuvable.", email);
                        continue;
                    }
                    logger.info("Utilisateur existe déjà : {} (ID: {})", email, utilisateur.getId());
                }

                // Création médecin
                if (utilisateur != null) {
                    Medecin medecin = new Medecin();
                    medecin.setNom(nom);
                    medecin.setPrenom(prenom);
                    medecin.setAdresseCabinet(adresse);
                    medecin.setLat(lat);
                    medecin.setLng(lng);
                    medecin.setSpecialite(sp);
                    medecin.setVille(ville);
                    medecin.setUtilisateur(utilisateur);

                    medecinRepository.save(medecin);
                    logger.info("Médecin {} {} créé avec email: {}", nom, prenom, email);
                } else {
                    logger.error("Échec de la création du médecin {} {} : utilisateur manquant.", nom, prenom);
                }

                counter++;
            }
        } catch (CsvValidationException e) {
            logger.error("Erreur de validation CSV : {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation des données : {}", e.getMessage());
            throw e;
        }
    }
}
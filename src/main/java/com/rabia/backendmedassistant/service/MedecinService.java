package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Autowired
    public MedecinService(MedecinRepository medecinRepository) {
        this.medecinRepository = medecinRepository;
    }

    public Medecin saveMedecin(Medecin medecin) {
        // Générer un mot de passe aléatoire
        String rawPassword = generateRandomPassword();

        // Hasher le mot de passe
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Sauvegarder dans l’entité
        medecin.setMotDePasse(encodedPassword);

        return medecinRepository.save(medecin);
    }
    private String generateRandomPassword() {
        // "DOC" + (int)(Math.random() * 10000); // Exemple simple: DOC1234
        return "medcin123";
    }

    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }
}

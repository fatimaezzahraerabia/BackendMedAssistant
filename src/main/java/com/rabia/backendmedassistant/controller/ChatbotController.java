package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.dto.RendezVousRequest;
import com.rabia.backendmedassistant.dto.RendezVousResponse;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import com.rabia.backendmedassistant.service.RendezVousChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    private final RendezVousChatbotService rendezVousChatbotService;
    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;

    public ChatbotController(RendezVousChatbotService rendezVousChatbotService,
                             UtilisateurRepository utilisateurRepository,
                             MedecinRepository medecinRepository) {
        this.rendezVousChatbotService = rendezVousChatbotService;
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository;
    }

    @PostMapping("/rendezvous")
    public RendezVousResponse prendreRendezVous(@RequestBody RendezVousRequest request) {
        try {
            logger.info("Requête reçue : patientId={}, medecinId={}, date={}, heureDebut={}, heureFin={}, confirmer={}",
                    request.getPatientId(), request.getMedecinId(), request.getDate(),
                    request.getHeureDebut(), request.getHeureFin(), request.isConfirmer());

            // Valider les entrées
            if (request.getDate() == null || request.getHeureDebut() == null || request.getHeureFin() == null) {
                throw new IllegalArgumentException("Les champs date, heureDebut et heureFin sont requis.");
            }

            Utilisateur utilisateur = utilisateurRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            Medecin medecin = medecinRepository.findById(request.getMedecinId())
                    .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

            // Appel au service chatbot
            RendezVousResponse response = rendezVousChatbotService.discuterAvecPatient(
                    request.getPatientId(),
                    request.getMedecinId(),
                    request.getDate(),
                    request.getHeureDebut(),
                    request.getHeureFin(),
                    request.isConfirmer(),
                    utilisateur,
                    medecin
            );

            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation : {}", e.getMessage());
            return new RendezVousResponse("Erreur : " + e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la requête : {}", e.getMessage());
            return new RendezVousResponse("Une erreur s'est produite. Veuillez réessayer.", null);
        }
    }
}
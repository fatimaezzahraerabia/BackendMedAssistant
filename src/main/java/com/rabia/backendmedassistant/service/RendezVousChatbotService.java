package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.dto.RendezVousResponse;
import com.rabia.backendmedassistant.model.Disponibilite;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.RendezVous;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.RendezVousRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RendezVousChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousChatbotService.class);

    private final RendezVousService rendezVousService;
    private final GeminiService geminiService;
    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final RendezVousRepository rendezVousRepository;

    public RendezVousChatbotService(RendezVousService rendezVousService,
                                    GeminiService geminiService,
                                    UtilisateurRepository utilisateurRepository,
                                    MedecinRepository medecinRepository,
                                    RendezVousRepository rendezVousRepository) {
        this.rendezVousService = rendezVousService;
        this.geminiService = geminiService;
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository;
        this.rendezVousRepository = rendezVousRepository;
    }

    public RendezVousResponse discuterAvecPatient(Long patientId, Long medecinId,
                                                  String date, String heureDebut, String heureFin, boolean confirmer,
                                                  Utilisateur utilisateur, Medecin medecin) {
        try {
            LocalDate rdvDate = LocalDate.parse(date);
            LocalTime rdvHeureDebut = LocalTime.parse(heureDebut);
            LocalTime rdvHeureFin = LocalTime.parse(heureFin);

            List<Disponibilite> disponibilites = rendezVousService.getDisponibilitesLibres(medecinId, rdvDate);

            String reponseBrute = genererReponse(patientId, medecinId, rdvDate, rdvHeureDebut, rdvHeureFin, confirmer, utilisateur, medecin, disponibilites);

            String message = geminiService.generateResponse(
                    "Reformule ce message pour un patient de manière polie et naturelle : " + reponseBrute
            );

            // Si la réponse indique une confirmation, récupérer le dernier rendez-vous créé
            RendezVous rdv = reponseBrute.contains("✅") ? rendezVousRepository.findTopByOrderByIdDesc().orElse(null) : null;

            return new RendezVousResponse(message, rdv);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la demande de rendez-vous : {}", e.getMessage());
            return new RendezVousResponse("Une erreur s'est produite lors du traitement de votre demande. Veuillez réessayer.", null);
        }
    }

    private String genererReponse(Long patientId, Long medecinId,
                                  LocalDate rdvDate, LocalTime rdvHeureDebut, LocalTime rdvHeureFin, boolean confirmer,
                                  Utilisateur utilisateur, Medecin medecin,
                                  List<Disponibilite> disponibilites) {
        logger.info("Vérification disponibilité pour médecin {}, date {}, heureDebut {}, heureFin {}",
                medecinId, rdvDate, rdvHeureDebut, rdvHeureFin);

        // Vérifier si le créneau demandé est inclus dans une disponibilité
        boolean estDisponible = disponibilites.stream()
                .anyMatch(d -> !rdvHeureDebut.isBefore(d.getHeureDebut()) && !rdvHeureFin.isAfter(d.getHeureFin()));

        if (estDisponible) {
            // Vérifier s'il y a un chevauchement avec un rendez-vous existant
            List<RendezVous> existingRdvs = rendezVousRepository.findByMedecinIdAndDate(medecinId, rdvDate);
            boolean hasOverlap = existingRdvs.stream()
                    .anyMatch(rdv ->
                            !(rdvHeureFin.isBefore(rdv.getHeureDebut()) || rdvHeureDebut.isAfter(rdv.getHeureFin()))
                    );

            if (hasOverlap) {
                logger.info("Chevauchement détecté pour le créneau demandé.");
                return "⛔ Le créneau demandé est déjà occupé. Voici des alternatives : "
                        + formaterAlternatives(disponibilites);
            }

            if (confirmer) {
                try {
                    rendezVousService.reserverRendezVous(patientId, medecinId, rdvDate, rdvHeureDebut, rdvHeureFin, utilisateur, medecin);
                    return "✅ Votre rendez-vous est confirmé le " + rdvDate + " de " + rdvHeureDebut + " à " + rdvHeureFin;
                } catch (RuntimeException e) {
                    logger.error("Erreur lors de la réservation : {}", e.getMessage());
                    return "⛔ Une erreur s'est produite lors de la confirmation du rendez-vous : " + e.getMessage();
                }
            }
            return "Le créneau de " + rdvHeureDebut + " à " + rdvHeureFin + " est disponible. Voulez-vous confirmer ce rendez-vous ?";
        }

        if (!disponibilites.isEmpty()) {
            return "⛔ Le créneau demandé n'est pas disponible. Voici des alternatives : "
                    + formaterAlternatives(disponibilites);
        }

        logger.info("Aucune disponibilité trouvée pour médecin {}, date {}", medecinId, rdvDate);
        return "❌ Aucune disponibilité trouvée pour ce jour. Veuillez choisir un autre jour.";
    }

    private String formaterAlternatives(List<Disponibilite> disponibilites) {
        return disponibilites.stream()
                .limit(3)
                .map(d -> d.getHeureDebut() + " - " + d.getHeureFin())
                .collect(Collectors.joining(", "));
    }
}
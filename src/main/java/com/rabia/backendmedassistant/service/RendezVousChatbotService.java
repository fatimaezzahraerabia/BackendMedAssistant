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

            // Vérifier d'abord la disponibilité du jour
            List<Disponibilite> disponibilites = rendezVousService.getDisponibilitesLibres(medecinId, rdvDate);
            if (disponibilites.isEmpty()) {
                String message = "Bonjour ! Il n'y a malheureusement aucun créneau disponible ce jour-là. Pourriez-vous choisir une autre date ?";
                return new RendezVousResponse(message, null);
            }

            // Si l'heure est fournie, parser et vérifier le créneau
            LocalTime rdvHeureDebut;
            LocalTime rdvHeureFin;
            try {
                rdvHeureDebut = LocalTime.parse(heureDebut);
                rdvHeureFin = LocalTime.parse(heureFin);
            } catch (Exception e) {
                return new RendezVousResponse("Format d'heure invalide. Veuillez utiliser HH:MM.", null);
            }

            // Générer la réponse finale
            String reponseBrute = genererReponse(patientId, medecinId, rdvDate, rdvHeureDebut, rdvHeureFin, confirmer, utilisateur, medecin, disponibilites);

            String message = geminiService.generateResponse(
                    "Reformule ce message en une seule version polie et naturelle pour communiquer avec un patient : " + reponseBrute
            );

            RendezVous rdv = reponseBrute.contains("confirmé") ? rendezVousRepository.findTopByOrderByIdDesc().orElse(null) : null;

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

        if (!estDisponible) {
            return "Ce créneau n'est pas disponible. Voici des alternatives : " + formaterAlternatives(disponibilites);
        }

        // Vérifier chevauchement avec rendez-vous existants
        List<RendezVous> existingRdvs = rendezVousRepository.findByMedecinIdAndDate(medecinId, rdvDate);
        boolean hasOverlap = existingRdvs.stream()
                .anyMatch(rdv -> !(rdvHeureFin.isBefore(rdv.getHeureDebut()) || rdvHeureDebut.isAfter(rdv.getHeureFin())));

        if (hasOverlap) {
            return "Ce créneau est déjà réservé. Voici des alternatives : " + formaterAlternatives(disponibilites);
        }

        // Confirmer le rendez-vous si demandé
        if (confirmer) {
            try {
                rendezVousService.reserverRendezVous(patientId, medecinId, rdvDate, rdvHeureDebut, rdvHeureFin, utilisateur, medecin);
                // Utiliser le nom du patient si disponible
                String nomPatient = utilisateur.getNom() != null ? utilisateur.getNom() : "Patient";
                return "Bonjour " + nomPatient + ", votre rendez-vous est confirmé pour le " 
                       + rdvDate + " de " + rdvHeureDebut + " à " + rdvHeureFin + ". À bientôt !";
            } catch (RuntimeException e) {
                logger.error("Erreur lors de la réservation : {}", e.getMessage());
                return "Une erreur s'est produite lors de la confirmation du rendez-vous : " + e.getMessage();
            }
        }
        

        return "Le créneau de " + rdvHeureDebut + " à " + rdvHeureFin + " est disponible. Voulez-vous confirmer ce rendez-vous ? (oui/non)";
    }

    private String formaterAlternatives(List<Disponibilite> disponibilites) {
        return disponibilites.stream()
                .limit(3)
                .map(d -> d.getHeureDebut() + " - " + d.getHeureFin())
                .collect(Collectors.joining(", "));
    }
}

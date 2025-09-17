package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.model.Disponibilite;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.RendezVous;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.DisponibiliteRepository;
import com.rabia.backendmedassistant.repository.RendezVousRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RendezVousService {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousService.class);

    private final RendezVousRepository rendezVousRepository;
    private final DisponibiliteRepository disponibiliteRepository;

    public RendezVousService(RendezVousRepository rendezVousRepository,
                             DisponibiliteRepository disponibiliteRepository) {
        this.rendezVousRepository = rendezVousRepository;
        this.disponibiliteRepository = disponibiliteRepository;
    }

    public List<Disponibilite> getDisponibilitesLibres(Long medecinId, LocalDate date) {
        List<Disponibilite> disponibilites = disponibiliteRepository.findByMedecinIdAndDateAndDisponibleTrue(medecinId, date);
        logger.info("Disponibilités trouvées pour médecin {} et date {} : {}", medecinId, date, disponibilites.size());
        return disponibilites;
    }

    public RendezVous reserverRendezVous(Long patientId, Long medecinId,
                                         LocalDate date, LocalTime heureDebut,
                                         LocalTime heureFin, Utilisateur utilisateur, Medecin medecin) {

        // Vérifier si le créneau demandé est inclus dans une disponibilité du médecin
        List<Disponibilite> disponibilites = disponibiliteRepository.findByMedecinIdAndDateAndDisponibleTrue(medecinId, date);
        boolean isInDisponibilite = disponibilites.stream()
                .anyMatch(d -> !heureDebut.isBefore(d.getHeureDebut()) && !heureFin.isAfter(d.getHeureFin()));

        if (!isInDisponibilite) {
            logger.warn("Créneau non disponible pour médecin {}, date {}, heureDebut {}, heureFin {}",
                    medecinId, date, heureDebut, heureFin);
            throw new RuntimeException("Le créneau demandé n'est pas inclus dans les disponibilités du médecin.");
        }

        // Vérifier s'il y a un chevauchement avec un rendez-vous existant
        List<RendezVous> existingRdvs = rendezVousRepository.findByMedecinIdAndDate(medecinId, date);
        boolean hasOverlap = existingRdvs.stream()
                .anyMatch(rdv ->
                        !(heureFin.isBefore(rdv.getHeureDebut()) || heureDebut.isAfter(rdv.getHeureFin()))
                );

        if (hasOverlap) {
            logger.warn("Chevauchement détecté pour médecin {}, date {}, heureDebut {}, heureFin {}",
                    medecinId, date, heureDebut, heureFin);
            throw new RuntimeException("Le créneau est déjà occupé par un autre rendez-vous.");
        }

        // Créer et sauvegarder le rendez-vous
        RendezVous rdv = new RendezVous();
        rdv.setDate(date);
        rdv.setHeureDebut(heureDebut);
        rdv.setHeureFin(heureFin);
        rdv.setStatut("EN_COURS");
        rdv.setUtilisateur(utilisateur);
        rdv.setMedecin(medecin);

        rendezVousRepository.save(rdv);
        logger.info("Rendez-vous sauvegardé pour patient {}, médecin {}, date {}, heureDebut {}, heureFin {}",
                patientId, medecinId, date, heureDebut, heureFin);

        for (Disponibilite dispo : disponibilites) {
            if (!heureDebut.isBefore(dispo.getHeureDebut()) && !heureFin.isAfter(dispo.getHeureFin())) {

                // Cas 1 : RDV couvre toute la dispo → supprimer complètement
                if (heureDebut.equals(dispo.getHeureDebut()) && heureFin.equals(dispo.getHeureFin())) {
                    disponibiliteRepository.delete(dispo);
                }
                // Cas 2 : RDV au début → décaler le début de la dispo
                else if (heureDebut.equals(dispo.getHeureDebut())) {
                    dispo.setHeureDebut(heureFin);
                    disponibiliteRepository.save(dispo);
                }
                // Cas 3 : RDV à la fin → décaler la fin de la dispo
                else if (heureFin.equals(dispo.getHeureFin())) {
                    dispo.setHeureFin(heureDebut);
                    disponibiliteRepository.save(dispo);
                }
                // Cas 4 : RDV au milieu → couper la dispo en 2
                else {
                    Disponibilite nouvelleDispo = new Disponibilite();
                    nouvelleDispo.setMedecin(dispo.getMedecin());
                    nouvelleDispo.setDate(dispo.getDate());
                    nouvelleDispo.setDisponible(true);
                    nouvelleDispo.setHeureDebut(heureFin);
                    nouvelleDispo.setHeureFin(dispo.getHeureFin());

                    // L’ancienne dispo garde la 1ère partie
                    dispo.setHeureFin(heureDebut);

                    disponibiliteRepository.save(dispo);
                    disponibiliteRepository.save(nouvelleDispo);
                }
            }
        }


        return rdv;
    }
}
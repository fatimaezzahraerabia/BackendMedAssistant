package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // Trouver tous les rendez-vous d'un médecin pour une date donnée
    List<RendezVous> findByMedecinIdAndDate(Long medecinId, LocalDate date);

    // Trouver le dernier rendez-vous créé, trié par ID décroissant
    Optional<RendezVous> findTopByOrderByIdDesc();
}
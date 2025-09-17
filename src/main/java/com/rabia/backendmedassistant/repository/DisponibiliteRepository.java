package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    List<Disponibilite> findByMedecinIdAndDateAndDisponibleTrue(Long medecinId, LocalDate date);

    Optional<Disponibilite> findByMedecinIdAndDateAndHeureDebut(Long medecinId, LocalDate date, LocalTime heureDebut);
}

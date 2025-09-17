package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {
    Ville findByNom(String nom);

    Optional<Ville> findByNomIgnoreCase(String nom);
}

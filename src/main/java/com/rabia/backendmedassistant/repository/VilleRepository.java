package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {
    Ville findByNom(String nom);
}

package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
   
}

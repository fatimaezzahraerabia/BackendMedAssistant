package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
  
}

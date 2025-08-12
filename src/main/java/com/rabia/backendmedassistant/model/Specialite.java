package com.rabia.backendmedassistant.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Specialite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    // To maintain the ManyToMany relationship from Medecin side,
    // we might need to manage the owning side or use mappedBy.
    // For now, let's just define the basic entity.
    // If Medecin is the owning side, this list might not be strictly necessary here
    // for basic entity definition, but it's good practice for bidirectional relationships.
    // However, since Medecin has @JoinTable, it's the owning side.
    // So, we don't need to define the inverse side here for basic functionality.

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}

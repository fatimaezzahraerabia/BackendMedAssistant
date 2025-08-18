package com.rabia.backendmedassistant.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "adresse_professionnelle")
    private String adresseCabinet;

    private Double lat;
    private Double lng;
    private String bio;
    @ManyToOne
    private Specialite specialite;


    @ElementCollection
    @CollectionTable(name = "medecin_disponibilites", joinColumns = @JoinColumn(name = "medecin_id"))
    @MapKeyColumn(name = "disponibilite_date")
    @Column(name = "disponibilite_horaires")
    private Map<LocalDate, List<String>> disponibilites;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getAdresseCabinet() { return adresseCabinet; }
    public void setAdresseCabinet(String adresseCabinet) { this.adresseCabinet = adresseCabinet; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Specialite getSpecialite() {
        return specialite;
    }

    public void setSpecialite(Specialite specialite) {
        this.specialite = specialite;
    }

    public Map<LocalDate, List<String>> getDisponibilites() { return disponibilites; }
    public void setDisponibilites(Map<LocalDate, List<String>> disponibilites) { this.disponibilites = disponibilites; }
}

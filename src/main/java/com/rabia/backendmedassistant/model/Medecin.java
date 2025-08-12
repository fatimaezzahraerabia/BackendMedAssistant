package com.rabia.backendmedassistant.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adresseCabinet;
    private Double lat;
    private Double lng;
    private String bio;

    
    @ElementCollection
    @MapKeyColumn(name="disponibilite_date")
    @Column(name="disponibilite_horaires")
    private Map<java.time.LocalDate, List<String>> disponibilites;

    @ManyToMany
    @JoinTable(
        name = "medecin_specialite",
        joinColumns = @JoinColumn(name = "medecin_id"),
        inverseJoinColumns = @JoinColumn(name = "specialite_id")
    )
    private List<Specialite> specialites;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdresseCabinet() {
        return adresseCabinet;
    }

    public void setAdresseCabinet(String adresseCabinet) {
        this.adresseCabinet = adresseCabinet;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Map<java.time.LocalDate, List<String>> getDisponibilites() {
        return disponibilites;
    }

    public void setDisponibilites(Map<java.time.LocalDate, List<String>> disponibilites) {
        this.disponibilites = disponibilites;
    }

    public List<Specialite> getSpecialites() {
        return specialites;
    }

    public void setSpecialites(List<Specialite> specialites) {
        this.specialites = specialites;
    }

   
}

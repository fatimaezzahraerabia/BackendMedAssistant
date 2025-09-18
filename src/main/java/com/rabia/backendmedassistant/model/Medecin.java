package com.rabia.backendmedassistant.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
    //    @ManyToOne
//    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties("medecins")
    private Specialite specialite;

    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Disponibilite> disponibilites = new ArrayList<>();

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties("medecins")
    private RendezVous rendezVous;





    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ville_id", nullable = false)
    private Ville ville;
    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur; //

    @Transient
    private Double distance;

    private String drivingDuration; // minutes (converties depuis secondes)
    private String walkingDuration; // minutes



    public String getDrivingDuration() {
        return drivingDuration;
    }

    public void setDrivingDuration(String drivingDuration) {
        this.drivingDuration = drivingDuration;
    }

    public String getWalkingDuration() {
        return walkingDuration;
    }

    public void setWalkingDuration(String walkingDuration) {
        this.walkingDuration = walkingDuration;
    }

    public List<String> getDiplomes() {
        return diplomes;
    }

    public void setDiplomes(List<String> diplomes) {
        this.diplomes = diplomes;
    }

    public List<String> getLanguesParlees() {
        return languesParlees;
    }

    public void setLanguesParlees(List<String> languesParlees) {
        this.languesParlees = languesParlees;
    }

    public Integer getAnneesExperience() {
        return anneesExperience;
    }

    public void setAnneesExperience(Integer anneesExperience) {
        this.anneesExperience = anneesExperience;
    }

    public Double getTarifConsultation() {
        return tarifConsultation;
    }

    public void setTarifConsultation(Double tarifConsultation) {
        this.tarifConsultation = tarifConsultation;
    }

    @ElementCollection
    @CollectionTable(name = "medecin_diplomes", joinColumns = @JoinColumn(name = "medecin_id"))
    @Column(name = "diplome")
    private List<String> diplomes;

    @ElementCollection
    @CollectionTable(name = "medecin_langues", joinColumns = @JoinColumn(name = "medecin_id"))
    @Column(name = "langue")
    private List<String> languesParlees;

    private Integer anneesExperience;
    private Double tarifConsultation;
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    // Getters & Setters
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public Specialite getSpecialite() {
        return specialite;
    }

    public void setSpecialite(Specialite specialite) {
        this.specialite = specialite;
    }




    public void setUtilisateur(Utilisateur savedUser) {
        this.utilisateur=savedUser;
    }



    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }

    public List<Disponibilite> getDisponibilites() {
        return disponibilites;
    }

    public void setDisponibilites(List<Disponibilite> disponibilites) {
        this.disponibilites = disponibilites;
    }
}

package com.rabia.backendmedassistant.dto;


public class MedecinRequest {
    public String nom;
    public String prenom;
    public String email;      // pour le compte utilisateur
    public String motDePasse; // pour le compte utilisateur
    public String adresseCabinet;
    public Double lat; // facultatif si on géocode côté serveur à partir de l’adresse
    public Double lng;
    public String bio;
    public Long specialiteId;
}

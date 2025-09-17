package com.rabia.backendmedassistant.dto;

import java.time.LocalDate;

public class RendezVousRequest {
    private Long patientId;
    private Long medecinId;
    private String date;
    private String heureDebut;
    private String heureFin;
    private boolean confirmer;

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isConfirmer() {
        return confirmer;
    }

    public void setConfirmer(boolean confirmer) {
        this.confirmer = confirmer;
    }
}
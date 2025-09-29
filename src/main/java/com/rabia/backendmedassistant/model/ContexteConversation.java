package com.rabia.backendmedassistant.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class ContexteConversation {
    private EtatConversation etat;
    private LocalDate dateProposee;
    private LocalTime heureDebutProposee;
    private LocalTime heureFinProposee;

    public EtatConversation getEtat() {
        return etat;
    }

    public void setEtat(EtatConversation etat) {
        this.etat = etat;
    }

    public LocalDate getDateProposee() {
        return dateProposee;
    }

    public void setDateProposee(LocalDate dateProposee) {
        this.dateProposee = dateProposee;
    }

    public LocalTime getHeureDebutProposee() {
        return heureDebutProposee;
    }

    public void setHeureDebutProposee(LocalTime heureDebutProposee) {
        this.heureDebutProposee = heureDebutProposee;
    }

    public LocalTime getHeureFinProposee() {
        return heureFinProposee;
    }

    public void setHeureFinProposee(LocalTime heureFinProposee) {
        this.heureFinProposee = heureFinProposee;
    }
}

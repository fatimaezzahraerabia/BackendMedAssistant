package com.rabia.backendmedassistant.dto;

import com.rabia.backendmedassistant.model.RendezVous;

public class RendezVousResponse {
    private String message;
    private RendezVous rendezVous;

    public RendezVousResponse(String message, RendezVous rendezVous) {
        this.message = message;
        this.rendezVous = rendezVous;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }
}
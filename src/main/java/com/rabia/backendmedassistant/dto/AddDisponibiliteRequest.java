package com.rabia.backendmedassistant.dto;

import java.util.List;

public class AddDisponibiliteRequest {
    private String date;
    private List<String> creneaux;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getCreneaux() {
        return creneaux;
    }

    public void setCreneaux(List<String> creneaux) {
        this.creneaux = creneaux;
    }
}

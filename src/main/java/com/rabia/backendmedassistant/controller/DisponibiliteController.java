package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.model.Disponibilite;
import com.rabia.backendmedassistant.service.RendezVousService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilites")
public class DisponibiliteController {
/*
    @Autowired
    private RendezVousService rendezVousService;

    @GetMapping("/{medecinId}")
    public List<Disponibilite> getDisponibilitesLibres(
            @PathVariable Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return rendezVousService.getDisponibilitesLibres(medecinId, date);
    }  */
}

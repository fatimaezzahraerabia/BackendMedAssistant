package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.service.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinService medecinService;

    @Autowired
    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

   
    @PostMapping
    public ResponseEntity<Medecin> addMedecin(@RequestBody Medecin medecin) {
        Medecin savedMedecin = medecinService.saveMedecin(medecin);
        return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
    }

   
    @GetMapping
    public ResponseEntity<List<Medecin>> getAllMedecins() {
        List<Medecin> medecins = medecinService.getAllMedecins();
        return new ResponseEntity<>(medecins, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getMedecinById(@PathVariable Long id) {
        return medecinService.getMedecinById(id)
                .map(medecin -> new ResponseEntity<>(medecin, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}

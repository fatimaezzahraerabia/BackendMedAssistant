package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import com.rabia.backendmedassistant.service.GeocodingService;
import com.rabia.backendmedassistant.service.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medecins")
@CrossOrigin(origins = "http://localhost:4200")
public class MedecinController {

    private final MedecinService medecinService;
    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private final UtilisateurRepository utilisateurRepository;


    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    public MedecinController(MedecinService medecinService, UtilisateurRepository utilisateurRepository) {
        this.medecinService = medecinService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/nearest")
    public List<Medecin> getNearest(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "50") double radius) {
        return medecinService.findNearest(lat, lng, query, limit, radius);
    }

    @PostMapping
    public ResponseEntity<Medecin> addMedecin(@RequestBody Medecin medecin) {
        try {
            Medecin savedMedecin = medecinService.addMedecin(medecin);
            return new ResponseEntity<>(savedMedecin, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medecin> updateMedecinProfile(
            @PathVariable Long id,
            @RequestBody Medecin updatedMedecin) {

        return medecinRepository.findById(id).map(existing -> {
            // Infos de base
            if (updatedMedecin.getNom() != null) existing.setNom(updatedMedecin.getNom());
            if (updatedMedecin.getPrenom() != null) existing.setPrenom(updatedMedecin.getPrenom());
            if (updatedMedecin.getAdresseCabinet() != null) existing.setAdresseCabinet(updatedMedecin.getAdresseCabinet());
            if (updatedMedecin.getBio() != null) existing.setBio(updatedMedecin.getBio());
            if (updatedMedecin.getSpecialite() != null) existing.setSpecialite(updatedMedecin.getSpecialite());

            // ✅ Nouveaux attributs
            if (updatedMedecin.getDiplomes() != null) existing.setDiplomes(updatedMedecin.getDiplomes());
            if (updatedMedecin.getLanguesParlees() != null) existing.setLanguesParlees(updatedMedecin.getLanguesParlees());
            if (updatedMedecin.getAnneesExperience() != null) existing.setAnneesExperience(updatedMedecin.getAnneesExperience());
            if (updatedMedecin.getTarifConsultation() != null) existing.setTarifConsultation(updatedMedecin.getTarifConsultation());

            return ResponseEntity.ok(medecinRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }



    @GetMapping
    public List<Medecin> getAllMedecins() {
        return medecinService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getMedecinById(@PathVariable Long id) {
        return medecinService.getMedecinById(id)
                .map(medecin -> new ResponseEntity<>(medecin, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
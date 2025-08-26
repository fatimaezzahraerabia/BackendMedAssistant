package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.service.GeocodingService;
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
    private MedecinRepository medecinRepository;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }


    public Medecin addMedecin(@RequestBody Medecin medecin) {
        if (medecin.getAdresseCabinet() != null) {
            double[] coords = geocodingService.geocode(medecin.getAdresseCabinet());
            medecin.setLatitude(coords[0]);
            medecin.setLongitude(coords[1]);
        }
        return medecinRepository.save(medecin);
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

package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Ville;
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
@CrossOrigin(origins = "http://localhost:4200")
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
    public Medecin addMedecin(@RequestBody Medecin medecin) {
        if (medecin.getAdresseCabinet() != null) {
            double[] coords = geocodingService.geocode(medecin.getAdresseCabinet());
            medecin.setLat(coords[0]);
            medecin.setLng(coords[1]);
        }
        return medecinRepository.save(medecin);
    }

    @GetMapping("/cities")
    public List<Ville> getCities() {
        return medecinService.getCities();
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
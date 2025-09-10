package com.rabia.backendmedassistant.controller;


import com.rabia.backendmedassistant.model.Specialite;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialites")
@CrossOrigin(origins = "http://localhost:4200")
public class SpecialiteController {

    @Autowired
    private SpecialiteRepository specialiteRepository;

    @GetMapping
    public List<Specialite> getAllSpecialites() {
        return specialiteRepository.findAll();
    }
}

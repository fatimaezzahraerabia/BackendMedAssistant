package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.dto.AdminRegistrationDTO;
import com.rabia.backendmedassistant.dto.LoginRequest;
import com.rabia.backendmedassistant.dto.PatientRegistrationDTO;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.rabia.backendmedassistant.model.Role; // Ajouter cet import



    @RestController
    @RequestMapping("/auth")
    @CrossOrigin(origins = "http://localhost:4200")
    public class AuthController {

        private final AuthenticationManager authManager;
        private final UtilisateurRepository utilisateurRepository;
        private final PasswordEncoder passwordEncoder;

        public AuthController(AuthenticationManager authManager,
                              UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
            this.authManager = authManager;
            this.utilisateurRepository = utilisateurRepository;
            this.passwordEncoder = passwordEncoder;
        }

        // Login
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
            try {
                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );
                Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                return ResponseEntity.ok(utilisateur);
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur de connexion : " + e.getMessage());
            }
        }
    // Register
    @PostMapping(value = "/register-patient", produces = "application/json")
    public ResponseEntity<?> registerPatient(@RequestBody PatientRegistrationDTO dto) {
        try {
            if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : Cet email est déjà utilisé.");
            }
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(dto.getNom());
            utilisateur.setEmail(dto.getEmail());
            utilisateur.setTelephone(dto.getTelephone());
            utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
            utilisateur.setRole(Role.PATIENT);
            Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
            return ResponseEntity.ok(savedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

   @PostMapping(value = "/register-admin", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegistrationDTO dto) {
    try {
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : Cet email est déjà utilisé.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(Role.ADMINISTRATEUR);

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        return ResponseEntity.ok(savedUtilisateur);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Erreur lors de l'enregistrement : " + e.getMessage());
    }
}

}

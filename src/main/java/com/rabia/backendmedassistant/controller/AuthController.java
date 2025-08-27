package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.dto.LoginRequest;
import com.rabia.backendmedassistant.model.Utilisateur;
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
    @PostMapping("/register-patient")
    public ResponseEntity<?> registerPatient(@RequestBody Utilisateur utilisateur) {
        try {
            if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : Cet email est déjà utilisé.");
            }
            utilisateur.setRole(Role.PATIENT); // Forcer le rôle PATIENT
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
            return ResponseEntity.ok(savedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Utilisateur utilisateur) {
        try {
            if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : Cet email est déjà utilisé.");
            }
            utilisateur.setRole(Role.ADMINISTRATEUR);
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
            return ResponseEntity.ok(savedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }
}

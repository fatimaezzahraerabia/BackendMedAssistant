package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = null;
        try {
            utilisateur = (Utilisateur) utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().name())
                .build();
    }
}

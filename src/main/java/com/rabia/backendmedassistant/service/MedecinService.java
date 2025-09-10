package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Role;
import com.rabia.backendmedassistant.model.Utilisateur;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.text.Normalizer;


@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired

    public MedecinService(MedecinRepository medecinRepository,
                          UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder) {
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private GeocodingService geocodingService;




    public List<Medecin> findNearest(double lat, double lng, String query, int limit, double radius) {
        List<Medecin> allMedecins = medecinRepository.findAll();
        System.out.println("Total médecins chargés: " + allMedecins.size()); // Debug

        List<Medecin> filtered = allMedecins;
        if (query != null && !query.isEmpty()) {
            filtered = filterByQuery(allMedecins, query);
            System.out.println("Médecins après filtre query '" + query + "': " + filtered.size()); // Debug
        }

        // Fallback: Si 0 après filtre, utiliser tous dans radius
        if (filtered.isEmpty() && query != null && !query.isEmpty()) {
            filtered = allMedecins;
            System.out.println("Fallback: Utilise tous les médecins dans radius " + radius + "km."); // Debug
        }

        // Filtre par distance <= radius, tri par distance
        return filtered.stream()
                .filter(m -> m.getLat() != null && m.getLng() != null)
                .filter(m -> {
                    double dist = calculateDistance(lat, lng, m.getLat(), m.getLng());
                    return dist <= radius;
                })
                .sorted(Comparator.comparingDouble(m -> calculateDistance(lat, lng, m.getLat(), m.getLng())))
                .limit(limit)
                .peek(m -> System.out.println("Médecin inclus: " + m.getNom() + " " + m.getPrenom() + ", Distance: " + calculateDistance(lat, lng, m.getLat(), m.getLng()) + "km")) // Debug
                .collect(Collectors.toList());
    }

    private List<Medecin> filterByQuery(List<Medecin> medecins, String query) {
        String normalizedQuery = normalize(query.toLowerCase());

        // Dictionnaire de synonymes/variantes
        Map<String, List<String>> synonyms = Map.of(
                "cardiologie", List.of("cardiologue"),
                "generaliste", List.of("médecin généraliste", "chirurgien généraliste"),
                "gyneco", List.of("gynécologue obstétricien"),
                "psy", List.of("psychiatre"),
                "orl", List.of("orl"),
                "ophtalmo", List.of("ophtalmologue"),
                "radio", List.of("radiologue"),
                "nephro", List.of("néphrologue"),
                "rhumato", List.of("rhumatologue")
        );

        // On enrichit la recherche avec les synonymes
        List<String> searchTerms = new java.util.ArrayList<>();
        searchTerms.add(normalizedQuery);
        synonyms.forEach((k, v) -> {
            if (normalize(k).equals(normalizedQuery)) {
                v.forEach(s -> searchTerms.add(normalize(s.toLowerCase())));
            }
        });

        return medecins.stream()
                .filter(m -> {
                    String fullName = normalize((m.getNom() + " " + m.getPrenom()).toLowerCase());
                    String specialite = m.getSpecialite() != null ? normalize(m.getSpecialite().getNom().toLowerCase()) : "";
                    return searchTerms.stream().anyMatch(term ->
                            fullName.contains(term) || specialite.contains(term)
                    );
                })
                .collect(Collectors.toList());
    }


    private String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public Medecin saveMedecin(Medecin medecin) {
        // Générer un mot de passe aléatoire
        String rawPassword = generateRandomPassword();

        // Hasher le mot de passe
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Sauvegarder dans l’entité

        return medecinRepository.save(medecin);
    }

    public Medecin addMedecin(Medecin medecin) {
        if (medecin.getUtilisateur() == null) {
            throw new IllegalArgumentException("Un médecin doit avoir un compte utilisateur associé.");
        }

        Utilisateur utilisateur = medecin.getUtilisateur();
        utilisateur.setRole(Role.MEDECIN);
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        medecin.setUtilisateur(savedUser);
        return medecinRepository.save(medecin);
    }
    private String generateRandomPassword() {
        // "DOC" + (int)(Math.random() * 10000); // Exemple simple: DOC1234
        return "medcin123";
    }

    public List<Medecin> getAll() {
        return medecinRepository.findAll();
    }

    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }


}
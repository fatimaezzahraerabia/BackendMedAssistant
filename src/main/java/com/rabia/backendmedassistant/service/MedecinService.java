package com.rabia.backendmedassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabia.backendmedassistant.model.*;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import com.rabia.backendmedassistant.repository.UtilisateurRepository;
import com.rabia.backendmedassistant.repository.VilleRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedecinService {

    private static final Logger logger = LoggerFactory.getLogger(MedecinService.class);

    private final MedecinRepository medecinRepository;
    private final SpecialiteRepository specialiteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VilleRepository villeRepository;
    private final GeocodingService geocodingService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouteservice.api.key}")
    private String orsApiKey;

    @Autowired
    public MedecinService(MedecinRepository medecinRepository, SpecialiteRepository specialiteRepository,
                          UtilisateurRepository utilisateurRepository, VilleRepository villeRepository,
                          GeocodingService geocodingService, PasswordEncoder passwordEncoder) {
        this.medecinRepository = medecinRepository;
        this.specialiteRepository = specialiteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.villeRepository = villeRepository;
        this.geocodingService = geocodingService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Ville> getCities() {
        return villeRepository.findAll().stream()
                .sorted(Comparator.comparing(Ville::getNom))
                .collect(Collectors.toList());
    }

    public List<Medecin> findNearest(double lat, double lng, String query, int limit, double radius) {
        List<Medecin> allMedecins = medecinRepository.findAll();
        logger.info("Total médecins chargés: {}", allMedecins.size());

        List<Medecin> filtered = allMedecins;
        if (query != null && !query.isEmpty()) {
            filtered = filterByQuery(allMedecins, query);
            logger.info("Médecins après filtre query '{}': {}", query, filtered.size());
        }

        // Fallback: Si 0 après filtre, utiliser tous dans radius
        if (filtered.isEmpty() && query != null && !query.isEmpty()) {
            filtered = allMedecins;
            logger.info("Fallback: Utilise tous les médecins dans radius {}km.", radius);
        }

        // Pré-filtre par distance Haversine (rayon élargi pour compenser les routes)
        double expandedRadius = radius * 1.5;
        List<Medecin> candidates = filtered.stream()
                .filter(m -> m.getLat() != null && m.getLng() != null)
                .filter(m -> {
                    double dist = calculateDistance(lat, lng, m.getLat(), m.getLng());
                    return dist <= expandedRadius;
                })
                .collect(Collectors.toList());

        // Limiter les candidats pour éviter surcharge API (ex: max 50 destinations)
        if (candidates.size() > 50) {
            candidates = candidates.stream()
                    .sorted(Comparator.comparingDouble(m -> calculateDistance(lat, lng, m.getLat(), m.getLng())))
                    .limit(50)
                    .collect(Collectors.toList());
        }

        // Calculer distance (voiture) et durées (voiture, pied) via ORS
        Map<Medecin, Map<String, Object>> realMetrics = getRealMetrics(lat, lng, candidates);

        // Filtrer par distance voiture <= radius, trier par distance voiture, limiter
        return realMetrics.entrySet().stream()
                .filter(entry -> {
                    Double dist = (Double) entry.getValue().get("distance");
                    return dist != null && dist <= radius;
                })
                .sorted(Comparator.comparingDouble(entry -> (Double) entry.getValue().get("distance")))
                .map(Map.Entry::getKey)
                .limit(limit)
                .peek(m -> {
                    Map<String, Object> metrics = realMetrics.get(m);
                    m.setDistance((Double) metrics.get("distance"));
                    m.setDrivingDuration((String) metrics.get("drivingDuration"));
                    m.setWalkingDuration((String) metrics.get("walkingDuration"));
                    logger.info("Médecin inclus: {} {}, Distance: {}km, Voiture: {}, À pied: {}",
                            m.getNom(), m.getPrenom(), metrics.get("distance"),
                            metrics.get("drivingDuration"), metrics.get("walkingDuration"));
                })
                .collect(Collectors.toList());
    }

    private Map<Medecin, Map<String, Object>> getRealMetrics(double originLat, double originLng, List<Medecin> medecins) {
        Map<Medecin, Map<String, Object>> metrics = new HashMap<>();
        if (medecins.isEmpty()) {
            logger.warn("Aucun médecin à calculer pour les métriques.");
            return metrics;
        }

        medecins.forEach(m -> metrics.put(m, new HashMap<>()));

        // Calculer pour voiture (distance + durée)
        callOrsMatrix(originLat, originLng, medecins, "driving-car", metrics, "distance", "drivingDuration");

        // Calculer pour pied (durée seulement)
        callOrsMatrix(originLat, originLng, medecins, "foot-walking", metrics, null, "walkingDuration");

        return metrics;
    }

    private void callOrsMatrix(double originLat, double originLng, List<Medecin> medecins, String profile,
                               Map<Medecin, Map<String, Object>> metrics, String distanceKey, String durationKey) {
        // Préparer le JSON pour l'API Matrix
        StringBuilder body = new StringBuilder("{\"locations\":[[" + originLng + "," + originLat + "]");
        for (Medecin m : medecins) {
            body.append(",[").append(m.getLng()).append(",").append(m.getLat()).append("]");
        }
        body.append("],\"metrics\":[").append(distanceKey != null ? "\"distance\",\"duration\"" : "\"duration\"").append("],\"units\":\"km\"}");

        // Configurer les headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", orsApiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        // Appeler l'API Matrix
        String url = "https://api.openrouteservice.org/v2/matrix/" + profile;
        try {
            logger.info("Appel à l'API ORS pour {}", profile);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode distancesNode = distanceKey != null ? root.path("distances").get(0) : null;
            JsonNode durationsNode = root.path("durations").get(0);

            for (int i = 0; i < medecins.size(); i++) {
                Medecin m = medecins.get(i);
                Map<String, Object> mMetrics = metrics.get(m);

                // Distance (seulement pour voiture)
                if (distanceKey != null) {
                    Double distKm = distancesNode.get(i + 1).isNull() ?
                            calculateDistance(originLat, originLng, m.getLat(), m.getLng()) :
                            distancesNode.get(i + 1).asDouble();
                    mMetrics.put(distanceKey, distKm);
                }

                // Durée (convertie en format Xh Ymin)
                Double durationMin = durationsNode.get(i + 1).isNull() ? null : durationsNode.get(i + 1).asDouble() / 60.0;
                mMetrics.put(durationKey, durationMin != null ? formatDuration(durationMin) : "N/A");
            }
        } catch (Exception e) {
            logger.error("Erreur API ORS pour {} : {}", profile, e.getMessage());
            medecins.forEach(m -> {
                Map<String, Object> mMetrics = metrics.get(m);
                if (distanceKey != null) {
                    mMetrics.put(distanceKey, calculateDistance(originLat, originLng, m.getLat(), m.getLng()));
                }
                mMetrics.put(durationKey, "N/A");
            });
        }
    }

    private String formatDuration(Double minutes) {
        if (minutes == null) return "N/A";
        int totalMinutes = (int) Math.round(minutes);
        if (totalMinutes < 60) {
            return totalMinutes + " min";
        }
        int hours = totalMinutes / 60;
        int remainingMinutes = totalMinutes % 60;
        return hours + "h " + remainingMinutes + "min";
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
        List<String> searchTerms = new ArrayList<>();
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

    @Transactional
    public Medecin saveMedecin(Medecin medecin) {
        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        medecin.getUtilisateur().setMotDePasse(encodedPassword);
        return medecinRepository.save(medecin);
    }

    @Transactional
    public Medecin addMedecin(Medecin medecin) {
        if (medecin.getUtilisateur() == null) {
            throw new IllegalArgumentException("Un médecin doit avoir un compte utilisateur associé.");
        }

        Utilisateur utilisateur = medecin.getUtilisateur();

        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }

        // Gestion utilisateur
        utilisateur.setRole(Role.MEDECIN);
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);
        medecin.setUtilisateur(savedUser);

        // Gestion ville
        Ville ville = medecin.getVille();
        if (ville != null) {
            Ville existingVille = villeRepository.findByNomIgnoreCase(ville.getNom()).orElse(null);
            if (existingVille == null) {
                try {
                    double[] coords = geocodingService.geocode(ville.getNom());
                    ville.setLat(coords[0]);
                    ville.setLng(coords[1]);
                    existingVille = villeRepository.save(ville);
                } catch (Exception e) {
                    logger.error("Erreur lors du géocodage de {} : {}", ville.getNom(), e.getMessage());
                    throw new RuntimeException("Échec du géocodage de la ville : " + ville.getNom(), e);
                }
            }
            medecin.setVille(existingVille);
        }

        // Gestion disponibilités
        if (medecin.getDisponibilites() != null) {
            medecin.getDisponibilites().forEach(d -> d.setMedecin(medecin));
        }

        // Gestion spécialité
        Specialite specialite = medecin.getSpecialite();
        if (specialite != null && specialite.getId() != null) {
            Specialite existingSpec = specialiteRepository.findById(specialite.getId())
                    .orElseThrow(() -> new RuntimeException("Spécialité introuvable !"));
            medecin.setSpecialite(existingSpec);
        } else {
            throw new IllegalArgumentException("Une spécialité valide avec un ID est requise.");
        }

        return medecinRepository.save(medecin);
    }

    private String generateRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(12); // Generate a 12-character random password
    }

    public List<Medecin> getAll() {
        return medecinRepository.findAll();
    }

    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }

    public Optional<Medecin> getMedecinByUtilisateurId(Long userId) {
        return medecinRepository.findByUtilisateurId(userId);
    }
}
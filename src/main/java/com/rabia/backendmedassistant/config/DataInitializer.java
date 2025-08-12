package com.rabia.backendmedassistant.config;

import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Specialite;
import com.rabia.backendmedassistant.repository.MedecinRepository;
import com.rabia.backendmedassistant.repository.SpecialiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedecinRepository medecinRepository;
    private final SpecialiteRepository specialiteRepository;

    @Autowired
    public DataInitializer(MedecinRepository medecinRepository, SpecialiteRepository specialiteRepository) {
        this.medecinRepository = medecinRepository;
        this.specialiteRepository = specialiteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
     
        Specialite cardio = new Specialite();
        cardio.setNom("Cardiologue");
        specialiteRepository.save(cardio);

        Specialite dermato = new Specialite();
        dermato.setNom("Dermatologue");
        specialiteRepository.save(dermato);

        Specialite neuro = new Specialite();
        neuro.setNom("Neurologue");
        specialiteRepository.save(neuro);

        Medecin medecin1 = new Medecin();
        medecin1.setAdresseCabinet("123 Rue de la Sante, Paris");
        medecin1.setLat(48.8566);
        medecin1.setLng(2.3522);
        medecin1.setBio("Expert en cardiologie avec 10 ans d'experience.");
        medecin1.setSpecialites(Arrays.asList(cardio));
        medecinRepository.save(medecin1);

        Medecin medecin2 = new Medecin();
        medecin2.setAdresseCabinet("456 Avenue des Champs-Elysees, Paris");
        medecin2.setLat(48.8698);
        medecin2.setLng(2.3072);
        medecin2.setBio("Specialiste des maladies de la peau.");
        medecin2.setSpecialites(Arrays.asList(dermato));
        medecinRepository.save(medecin2);

        Medecin medecin3 = new Medecin();
        medecin3.setAdresseCabinet("789 Boulevard Saint-Germain, Paris");
        medecin3.setLat(48.8534);
        medecin3.setLng(2.3345);
        medecin3.setBio("Recherche avancee en neurologie.");
        medecin3.setSpecialites(Arrays.asList(neuro));
        medecinRepository.save(medecin3);

        Medecin medecin4 = new Medecin();
        medecin4.setAdresseCabinet("101 Rue de Rivoli, Paris");
        medecin4.setLat(48.8610);
        medecin4.setLng(2.3359);
        medecin4.setBio("Cardiologue interventionnel.");
        medecin4.setSpecialites(Arrays.asList(cardio));
        medecinRepository.save(medecin4);

        Medecin medecin5 = new Medecin();
        medecin5.setAdresseCabinet("212 Rue du Faubourg Saint-Honore, Paris");
        medecin5.setLat(48.8722);
        medecin5.setLng(2.3147);
        medecin5.setBio("Dermatologie esthetique et laser.");
        medecin5.setSpecialites(Arrays.asList(dermato));
        medecinRepository.save(medecin5);
    }
}

package com.rabia.backendmedassistant.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rabia.backendmedassistant.dto.RendezVousResponse;
import com.rabia.backendmedassistant.model.ContexteConversation;
import com.rabia.backendmedassistant.model.EtatConversation;
import com.rabia.backendmedassistant.model.Medecin;
import com.rabia.backendmedassistant.model.Utilisateur;

@Service
public class RendezVousDialogueService {

    private final RendezVousChatbotService rendezVousChatbotService;
    private final Map<Long, ContexteConversation> conversations = new HashMap<>();

    public RendezVousDialogueService(RendezVousChatbotService rendezVousChatbotService) {
        this.rendezVousChatbotService = rendezVousChatbotService;
    }
    private LocalDate tryParseDate(String message) {
        try {
            return LocalDate.parse(message.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalTime tryParseTime(String message) {
        try {
            return LocalTime.parse(message.trim());
        } catch (Exception e) {
            return null;
        }
    }
    

    public String discuter(Long patientId, Long medecinId, String message,
                       Utilisateur utilisateur, Medecin medecin) {
    ContexteConversation contexte = conversations
            .computeIfAbsent(patientId, id -> new ContexteConversation());

    if (contexte.getEtat() == null) {
        contexte.setEtat(EtatConversation.DEMANDE_DATE);
        return "Bonjour ! Quelle date vous convient pour le rendez-vous ? (format YYYY-MM-DD)";
    }

    switch (contexte.getEtat()) {
        case DEMANDE_DATE -> {
            LocalDate date = tryParseDate(message);
            if (date == null) {
                return "Je n’ai pas compris la date. Merci de la donner au format YYYY-MM-DD.";
            }
            contexte.setDateProposee(date);
            contexte.setEtat(EtatConversation.DEMANDE_HEURE);
            return "À quelle heure souhaitez-vous commencer ? (format HH:mm)";
        }

        case DEMANDE_HEURE -> {
            LocalTime heure = tryParseTime(message);
            if (heure == null) {
                return "Je n’ai pas compris l’heure. Merci de la donner au format HH:mm.";
            }
            contexte.setHeureDebutProposee(heure);
            contexte.setHeureFinProposee(heure.plusHours(1));
            contexte.setEtat(EtatConversation.CONFIRMATION);
            return "Je peux vous proposer le " + contexte.getDateProposee()
                    + " à " + contexte.getHeureDebutProposee()
                    + ". Est-ce que vous confirmez ? (oui/non)";
        }

        case CONFIRMATION -> {
            if (message.equalsIgnoreCase("oui")) {
                // ici tu appelles ton service pour créer le rendez-vous
                RendezVousResponse response = rendezVousChatbotService.discuterAvecPatient(
                    patientId,
                    medecinId,
                    contexte.getDateProposee().toString(),
                    contexte.getHeureDebutProposee().toString(),
                    contexte.getHeureFinProposee().toString(),
                    true,
                    utilisateur,
                    medecin
                );
                conversations.remove(patientId);
                return response.getMessage();
            } else if (message.equalsIgnoreCase("non")) {
                contexte.setEtat(EtatConversation.DEMANDE_DATE);
                return "D’accord, quelle autre date vous conviendrait ?";
            } else {
                return "Merci de répondre par 'oui' ou 'non'.";
            }
        }
    }

    return "Je n’ai pas compris. Pouvez-vous répéter ?";
}

    
}
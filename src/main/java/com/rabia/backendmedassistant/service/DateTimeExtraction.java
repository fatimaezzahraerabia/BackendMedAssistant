package com.rabia.backendmedassistant.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeExtraction {

    public static LocalDate extraireDate(String message) {
        String msg = message.toLowerCase(Locale.FRANCE);
        LocalDate today = LocalDate.now();

        if (msg.contains("demain")) {
            return today.plusDays(1);
        }
        if (msg.contains("après-demain") || msg.contains("apres-demain")) {
            return today.plusDays(2);
        }
        if (msg.contains("aujourd'hui")) {
            return today;
        }

        // Exemple : lundi prochain
        if (msg.contains("lundi")) {
            return prochainJour(DayOfWeek.MONDAY, today);
        }
        if (msg.contains("mardi")) {
            return prochainJour(DayOfWeek.TUESDAY, today);
        }
        if (msg.contains("mercredi")) {
            return prochainJour(DayOfWeek.WEDNESDAY, today);
        }
        if (msg.contains("jeudi")) {
            return prochainJour(DayOfWeek.THURSDAY, today);
        }
        if (msg.contains("vendredi")) {
            return prochainJour(DayOfWeek.FRIDAY, today);
        }
        if (msg.contains("samedi")) {
            return prochainJour(DayOfWeek.SATURDAY, today);
        }
        if (msg.contains("dimanche")) {
            return prochainJour(DayOfWeek.SUNDAY, today);
        }

        return null; // pas trouvé
    }

    public static LocalTime extraireHeure(String message) {
        String msg = message.toLowerCase(Locale.FRANCE);

        if (msg.contains("matin")) {
            return LocalTime.of(9, 0);
        }
        if (msg.contains("après-midi") || msg.contains("apres-midi")) {
            return LocalTime.of(14, 0);
        }
        if (msg.contains("soir")) {
            return LocalTime.of(18, 0);
        }

        // Si l'utilisateur écrit une heure exacte (ex: 09:30, 9h30)
        msg = msg.replace("h", ":");
        try {
            return LocalTime.parse(msg, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            // Ignorer si pas une vraie heure
        }

        return null;
    }

    private static LocalDate prochainJour(DayOfWeek day, LocalDate today) {
        LocalDate result = today;
        while (result.getDayOfWeek() != day) {
            result = result.plusDays(1);
        }
        return result;
    }
}

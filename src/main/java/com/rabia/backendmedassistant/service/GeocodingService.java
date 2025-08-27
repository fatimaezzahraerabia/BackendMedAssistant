package com.rabia.backendmedassistant.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;

@Service
public class GeocodingService {

    public double[] geocode(String adresse) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + adresse;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            JSONArray arr = new JSONArray(response);
            if (arr.length() > 0) {
                double lat = arr.getJSONObject(0).getDouble("lat");
                double lon = arr.getJSONObject(0).getDouble("lon");
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{0.0, 0.0}; // défaut si échec
    }
}


package org.phinix.nasaApiEndpointApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.phinix.nasaApiEndpointApp.client.NasaClient;
import org.phinix.nasaApiEndpointApp.dto.external.NasaApiResponse;
import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class AsteroidsService {
    private final NasaClient client;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AsteroidsService(NasaClient client) {
        this.client = client;
    }

    public NasaApiResponse getRawAsteroids(int days) {
        return getAsteroidsFeedWithDays(days, NasaApiResponse.class);
    }

    public List<AsteroidResponseDTO> getMappedAsteroids(int days) {
        String response = getAsteroidsFeedWithDays(days, String.class);

        List<AsteroidResponseDTO> results = new ArrayList<>();
        try {
            JsonNode root = parseResponseToJson(response);
            JsonNode neoMap = extractNearEarthObjects(root);

            Iterator<String> dateFields = getDateFields(neoMap);
            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode asteroidArray = neoMap.get(date);
                processAsteroids(asteroidArray, date, results);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private JsonNode parseResponseToJson(String response) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response);
    }

    private JsonNode extractNearEarthObjects(JsonNode root) {
        return root.get("near_earth_objects");
    }

    private Iterator<String> getDateFields(JsonNode neoMap) {
        return neoMap.fieldNames();
    }

    private void processAsteroids(JsonNode asteroidArray, String date, List<AsteroidResponseDTO> results) {
        for (JsonNode asteroid : asteroidArray) {
            AsteroidResponseDTO asteroidDTO = mapAsteroidFromResponse(asteroid, date);
            results.add(asteroidDTO);
        }
    }

    private AsteroidResponseDTO mapAsteroidFromResponse(JsonNode asteroid, String date) {
        String name = asteroid.get("name").asText();
        double diameter = extractDiameter(asteroid);
        double speed = extractSpeed(asteroid);
        String planet = extractPlanet(asteroid);

        return new AsteroidResponseDTO(name, diameter, speed, date, planet);
    }

    private double extractDiameter(JsonNode asteroid) {
        return asteroid
                .get("estimated_diameter")
                .get("kilometers")
                .get("estimated_diameter_max")
                .asDouble();
    }

    private double extractSpeed(JsonNode asteroid) {
        JsonNode approachData = asteroid
                .get("close_approach_data")
                .get(0);

        return approachData
                .get("relative_velocity")
                .get("kilometers_per_hour")
                .asDouble();
    }

    private String extractPlanet(JsonNode asteroid) {
        JsonNode approachData = asteroid
                .get("close_approach_data")
                .get(0);

        return approachData.get("orbiting_body").asText();
    }

    private <T> T getAsteroidsFeedWithDays(int days, Class<T> responseType) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return client.getAsteroidsFeed(
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                responseType
        );
    }
}

package org.phinix.nasaApiEndpointApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.phinix.nasaApiEndpointApp.client.NasaClient;
import org.phinix.nasaApiEndpointApp.dto.external.NasaApiResponse;
import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AsteroidsService {

    private final NasaClient client;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsteroidsService(NasaClient client) {
        this.client = client;
    }

    public NasaApiResponse getRawAsteroids(int days) {
        return getAsteroidsFeed(days, NasaApiResponse.class);
    }

    public List<AsteroidResponseDTO> getMappedAsteroids(int days) {
        JsonNode neoMap = fetchNeoMap(days);
        return extractAsteroidsFromNeoMap(neoMap, false);
    }

    public List<AsteroidResponseDTO> getTopDangerousAsteroids(int days) {
        validateDays(days);
        JsonNode neoMap = fetchNeoMap(days);

        return extractAsteroidsFromNeoMap(neoMap, true).stream()
                .sorted(Comparator.comparingDouble(AsteroidResponseDTO::diameter).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private <T> T getAsteroidsFeed(int days, Class<T> responseType) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return client.getAsteroidsFeed(
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                responseType
        );
    }

    private JsonNode fetchNeoMap(int days) {
        String response = getAsteroidsFeed(days, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("near_earth_objects");
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la respuesta de la API de la NASA.", e);
        }
    }

    private List<AsteroidResponseDTO> extractAsteroidsFromNeoMap(JsonNode neoMap, boolean onlyHazardous) {
        List<AsteroidResponseDTO> results = new ArrayList<>();
        neoMap.fieldNames().forEachRemaining(date -> {
            JsonNode asteroidArray = neoMap.get(date);
            for (JsonNode asteroid : asteroidArray) {
                if (!onlyHazardous || asteroid.get("is_potentially_hazardous_asteroid").asBoolean()) {
                    results.add(mapAsteroid(asteroid, date));
                }
            }
        });
        return results;
    }

    private AsteroidResponseDTO mapAsteroid(JsonNode asteroid, String date) {
        String name = asteroid.get("name").asText();
        double diameter = calculateAverageDiameter(asteroid);
        double speed = extractSpeed(asteroid);
        String planet = extractPlanet(asteroid);

        return new AsteroidResponseDTO(name, diameter, speed, date, planet);
    }

    private double calculateAverageDiameter(JsonNode asteroid) {
        JsonNode km = asteroid.get("estimated_diameter").get("kilometers");
        double min = km.get("estimated_diameter_min").asDouble();
        double max = km.get("estimated_diameter_max").asDouble();
        return (min + max) / 2.0;
    }

    private double extractSpeed(JsonNode asteroid) {
        return asteroid.get("close_approach_data").get(0)
                .get("relative_velocity").get("kilometers_per_hour").asDouble();
    }

    private String extractPlanet(JsonNode asteroid) {
        return asteroid.get("close_approach_data").get(0).get("orbiting_body").asText();
    }

    private void validateDays(int days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("El parámetro 'days' debe estar entre 1 y 7.");
        }
    }
}

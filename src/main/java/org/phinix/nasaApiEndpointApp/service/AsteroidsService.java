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

    public List<AsteroidResponseDTO> getTopDangerousAsteroids(int days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("El parámetro 'days' debe estar entre 1 y 7.");
        }

        String response = getAsteroidsFeedWithDays(days, String.class);
        List<AsteroidResponseDTO> results = new ArrayList<>();

        try {
            JsonNode neoMap = extractNearEarthObjects(parseResponseToJson(response));
            Iterator<String> dateFields = getDateFields(neoMap);

            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode asteroidArray = neoMap.get(date);

                for (JsonNode asteroid : asteroidArray) {
                    if (asteroid.get("is_potentially_hazardous_asteroid").asBoolean()) {
                        JsonNode approach = asteroid.get("close_approach_data").get(0);
                        double diameterMin = asteroid.get("estimated_diameter").get("kilometers").get("estimated_diameter_min").asDouble();
                        double diameterMax = asteroid.get("estimated_diameter").get("kilometers").get("estimated_diameter_max").asDouble();
                        double avgDiameter = (diameterMin + diameterMax) / 2.0;
                        double speed = approach.get("relative_velocity").get("kilometers_per_hour").asDouble();
                        String planet = approach.get("orbiting_body").asText();
                        String name = asteroid.get("name").asText();
                        String approachDate = approach.get("close_approach_date").asText();

                        results.add(new AsteroidResponseDTO(name, avgDiameter, speed, approachDate, planet));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ordenar por diámetro descendente y devolver top 3
        return results.stream()
                .sorted((a1, a2) -> Double.compare(a2.diameter(), a1.diameter()))
                .limit(3)
                .toList();
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

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

    public List<AsteroidResponseDTO> getSimplifyAsteroids(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        String response = client.getAsteroidsFeed(
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                String.class
        );

        List<AsteroidResponseDTO> results = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode neoMap = root.get("near_earth_objects");

            Iterator<String> dateFields = neoMap.fieldNames();
            while (dateFields.hasNext()) {
                String date = dateFields.next();
                JsonNode asteroidArray = neoMap.get(date);

                for (JsonNode asteroid : asteroidArray) {
                    String name = asteroid.get("name").asText();

                    double diameter = asteroid
                            .get("estimated_diameter")
                            .get("kilometers")
                            .get("estimated_diameter_max")
                            .asDouble();

                    JsonNode approachData = asteroid
                            .get("close_approach_data")
                            .get(0);

                    double speed = approachData
                            .get("relative_velocity")
                            .get("kilometers_per_hour")
                            .asDouble();

                    String planet = approachData.get("orbiting_body").asText();

                    results.add(new AsteroidResponseDTO(name, diameter, speed, date, planet));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
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

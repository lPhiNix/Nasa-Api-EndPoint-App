package org.phinix.nasaApiEndpointApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.phinix.nasaApiEndpointApp.client.NasaClient;
import org.phinix.nasaApiEndpointApp.dto.external.NasaApiResponse;
import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.phinix.nasaApiEndpointApp.exception.NasaApiException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for handling the business logic related to asteroid data retrieval
 * and transformation.
 *
 * <p><strong>Architectural Design:</strong></p>
 * <ul>
 *     <li>Annotated with {@code @Service} to indicate that it holds business logic and to allow Spring to manage it as a bean.</li>
 *     <li>Acts as a bridge between the controller layer and the external NASA API client.</li>
 *     <li>Encapsulates logic for transforming raw JSON responses into domain-specific DTOs.</li>
 * </ul>
 *
 * <p>This class supports three core use cases:</p>
 * <ol>
 *     <li>Fetching raw asteroid data as returned by the NASA API.</li>
 *     <li>Mapping the raw data to simplified DTOs.</li>
 *     <li>Extracting and sorting the most dangerous asteroids.</li>
 */
@Service
public class AsteroidsService {

    private final NasaClient client;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor with dependency injection of {@link NasaClient}.
     * This client handles communication with the NASA API.
     *
     * @param client the NASA API client
     */
    public AsteroidsService(NasaClient client) {
        this.client = client;
    }

    /**
     * Fetches the raw asteroid feed from the NASA API.
     *
     * @param days the number of days to fetch data for
     * @return raw {@link NasaApiResponse}
     */
    public NasaApiResponse getRawAsteroids(int days) {
        return getAsteroidsFeed(days, NasaApiResponse.class);
    }

    /**
     * Returns a list of simplified asteroid DTOs.
     * Filters and transforms the raw data into a format optimized for client consumption.
     *
     * @param days the number of days to fetch data for
     * @return list of {@link AsteroidResponseDTO}
     */
    public List<AsteroidResponseDTO> getMappedAsteroids(int days) {
        JsonNode neoMap = fetchNeoMap(days);
        return extractAsteroidsFromNeoMap(neoMap, false);
    }

    /**
     * Retrieves the top 3 most dangerous asteroids based on their average diameter.
     * Only considers those marked as hazardous.
     *
     * @param days the number of days to fetch data for
     * @return list of top 3 dangerous asteroids
     */
    public List<AsteroidResponseDTO> getTopDangerousAsteroids(int days) {
        validateDays(days);
        JsonNode neoMap = fetchNeoMap(days);

        return extractAsteroidsFromNeoMap(neoMap, true).stream()
                .sorted(Comparator.comparingDouble(AsteroidResponseDTO::diameter).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Generic method to retrieve asteroid feed from NASA API and deserialize it into a given type.
     *
     * @param days         number of days to fetch
     * @param responseType target class to deserialize the response
     * @param <T>          response type
     * @return deserialized response object
     */
    private <T> T getAsteroidsFeed(int days, Class<T> responseType) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return client.getAsteroidsFeed(
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                responseType
        );
    }

    /**
     * Fetches the raw JSON string and parses the "near_earth_objects" node.
     *
     * @param days number of days to fetch
     * @return parsed JSON node for near earth objects
     */
    private JsonNode fetchNeoMap(int days) {
        String response = getAsteroidsFeed(days, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("near_earth_objects");
        } catch (Exception e) {
            throw new NasaApiException("Error parsing the NASA API response.", e);
        }
    }

    /**
     * Extracts and maps asteroid data from the parsed JSON tree into DTOs.
     *
     * @param neoMap         the JSON map of near-earth objects
     * @param onlyHazardous  whether to include only hazardous asteroids
     * @return list of asteroid DTOs
     */
    private List<AsteroidResponseDTO> extractAsteroidsFromNeoMap(JsonNode neoMap, boolean onlyHazardous) {
        List<AsteroidResponseDTO> results = new ArrayList<>();

        // Iterate through each date group of asteroids
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

    /**
     * Maps a single asteroid node to a DTO.
     *
     * @param asteroid raw JSON node
     * @param date     date of the asteroid flyby
     * @return DTO representing the asteroid
     */
    private AsteroidResponseDTO mapAsteroid(JsonNode asteroid, String date) {
        String name = asteroid.get("name").asText();
        double diameter = calculateAverageDiameter(asteroid);
        double speed = extractSpeed(asteroid);
        String planet = extractPlanet(asteroid);

        return new AsteroidResponseDTO(name, diameter, speed, date, planet);
    }

    /**
     * Calculates the average estimated diameter of an asteroid in kilometers.
     *
     * @param asteroid raw JSON node
     * @return average diameter in km
     */
    private double calculateAverageDiameter(JsonNode asteroid) {
        JsonNode km = asteroid.get("estimated_diameter").get("kilometers");
        double min = km.get("estimated_diameter_min").asDouble();
        double max = km.get("estimated_diameter_max").asDouble();
        return (min + max) / 2.0;
    }

    /**
     * Extracts the relative velocity of the asteroid in km/h.
     *
     * @param asteroid raw JSON node
     * @return speed in km/h
     */
    private double extractSpeed(JsonNode asteroid) {
        return asteroid.get("close_approach_data").get(0)
                .get("relative_velocity").get("kilometers_per_hour").asDouble();
    }

    /**
     * Extracts the name of the planet or celestial body the asteroid is orbiting.
     *
     * @param asteroid raw JSON node
     * @return orbiting body name
     */
    private String extractPlanet(JsonNode asteroid) {
        return asteroid.get("close_approach_data").get(0).get("orbiting_body").asText();
    }

    /**
     * Validates the number of days parameter.
     * NASA API allows a maximum of 7 days of data to be fetched at once.
     *
     * @param days number of days requested
     */
    private void validateDays(int days) {
        if (days < 1 || days > 7) {
            throw new NasaApiException("The 'days' parameter must be between 1 and 7.");
        }
    }
}

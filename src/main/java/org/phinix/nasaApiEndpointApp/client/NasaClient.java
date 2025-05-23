package org.phinix.nasaApiEndpointApp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client component responsible for communicating with the external NASA API.
 *
 * <p>This class encapsulates the logic required to build and send HTTP GET
 * requests to the NASA Near-Earth Object (NEO) Web Service. It abstracts
 * away the details of HTTP communication, keeping the rest of the application
 * decoupled from low-level REST API handling.</p>
 *
 * <p>By isolating this logic in a separate component, it adheres to the
 * principle of separation of concerns and supports cleaner service-layer code.
 * Configuration values like the API key and base URL are injected via
 * Spring's {@code @Value} annotation for flexibility and security.</p>
 */
@Component
public class NasaClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    /**
     * Constructs a new NasaClient with the required dependencies and configuration.
     *
     * @param restTemplate the RestTemplate used for HTTP communication
     * @param apiUrl the base URL of the NASA NEO API (injected from application properties)
     * @param apiKey the API key used to authenticate with NASA's services (injected)
     */
    public NasaClient(
            RestTemplate restTemplate,
            @Value("${nasa.api.url}") String apiUrl,
            @Value("${nasa.api.key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    /**
     * Fetches the asteroid feed from the NASA API for a given date range.
     *
     * @param startDate the start date of the range in format yyyy-MM-dd
     * @param endDate the end date of the range in format yyyy-MM-dd
     * @param responseType the expected response type (e.g., String.class or NasaApiResponse.class)
     * @param <T> the generic return type to support multiple deserialization targets
     * @return the response parsed into the specified response type
     */
    public <T> T getAsteroidsFeed(String startDate, String endDate, Class<T> responseType) {
        // Constructs the URL with required parameters: date range and API key
        String url = String.format("%s?start_date=%s&end_date=%s&api_key=%s",
                apiUrl, startDate, endDate, apiKey);

        // Sends the GET request and maps the result to the desired type
        return restTemplate.getForObject(url, responseType);
    }
}

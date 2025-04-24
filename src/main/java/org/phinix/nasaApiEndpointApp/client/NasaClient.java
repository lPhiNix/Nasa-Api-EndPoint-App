package org.phinix.nasaApiEndpointApp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NasaClient {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public NasaClient(
            RestTemplate restTemplate,
            @Value("${nasa.api.url}") String apiUrl,
            @Value("${nasa.api.key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public <T> T getAsteroidsFeed(String startDate, String endDate, Class<T> responseType) {
        String url = String.format("%s?start_date=%s&end_date=%s&api_key=%s",
                apiUrl, startDate, endDate, apiKey);
        return restTemplate.getForObject(url, responseType);
    }
}

package org.phinix.nasaApiEndpointApp.service;

import org.phinix.nasaApiEndpointApp.client.NasaClient;
import org.phinix.nasaApiEndpointApp.dto.external.NasaApiResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AsteroidsService {
    private final NasaClient client;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AsteroidsService(NasaClient client) {
        this.client = client;
    }

    public NasaApiResponse getRawAsteroids(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return client.getAsteroidsFeed(
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                NasaApiResponse.class
        );
    }
}

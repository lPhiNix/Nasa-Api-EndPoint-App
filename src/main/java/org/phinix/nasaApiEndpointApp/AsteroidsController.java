package org.phinix.nasaApiEndpointApp;

import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.phinix.nasaApiEndpointApp.service.AsteroidsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller responsible for handling HTTP requests related to asteroid data.
 *
 * <p>This controller exposes three endpoints:
 * <ul>
 *     <li>Fetch all raw asteroid data from the NASA API</li>
 *     <li>Return a simplified mapped version of the asteroid data</li>
 *     <li>Return the top 3 most dangerous asteroids based on size</li>
 * </ul>
 *
 * <p><strong>Architectural Design:</strong></p>
 * <ul>
 *     <li>Marked with {@code @RestController} to register it as a Spring MVC controller.</li>
 *     <li>{@code @RequestMapping("api/v1/asteroids")} defines a base path, allowing versioning and clear organization of endpoints.</li>
 *     <li>Delegates business logic to {@link AsteroidsService}, respecting the separation of concerns principle.</li>
 * </ul>
 */
@RestController
@RequestMapping("api/v1/asteroids")
public class AsteroidsController {

    /**
     * Service layer dependency that encapsulates the business logic related to asteroid processing.
     */
    private final AsteroidsService service;

    /**
     * Constructor-based dependency injection of {@link AsteroidsService}.
     *
     * @param service service class that handles business logic
     */
    public AsteroidsController(AsteroidsService service) {
        this.service = service;
    }

    /**
     * Endpoint to retrieve raw asteroid data as received from the NASA API.
     *
     * <p>This method does not transform the response; it's useful for debugging
     * or for clients that need full original data.</p>
     *
     * @param days number of days to fetch data for (between 1 and 7)
     * @return raw response object (structure matches NASA API)
     */
    @GetMapping
    public Object getAllAsteroids(@RequestParam int days) {
        // Directly returns the unprocessed data from NASA for the given number of days
        return service.getRawAsteroids(days);
    }

    /**
     * Endpoint to return simplified asteroid data mapped into custom DTOs.
     *
     * <p>Each asteroid is transformed into an {@link AsteroidResponseDTO} which includes
     * selected fields: name, diameter, speed, date, and planet. This simplifies consumption
     * by frontend clients and avoids overexposing irrelevant data.</p>
     *
     * @param days number of days to fetch data for (between 1 and 7)
     * @return list of simplified asteroid DTOs
     */
    @GetMapping("/simplify")
    public List<AsteroidResponseDTO> getSimplifyAsteroids(@RequestParam int days) {
        // Returns a cleaned and minimal representation of each asteroid
        return service.getMappedAsteroids(days);
    }

    /**
     * Endpoint to return the top 3 most dangerous asteroids in the given time range.
     *
     * <p>This method filters for only hazardous asteroids and returns the three largest
     * by average estimated diameter.</p>
     *
     * @param days number of days to fetch data for (between 1 and 7)
     * @return list of top 3 most dangerous asteroid DTOs
     */
    @GetMapping("/top-dangerous")
    public List<AsteroidResponseDTO> getTopDangerousAsteroids(@RequestParam int days) {
        // Filters and sorts the hazardous asteroids by size in descending order
        return service.getTopDangerousAsteroids(days);
    }
}

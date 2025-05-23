package org.phinix.nasaApiEndpointApp.dto.external;

import java.util.Map;

/**
 * Wrapper class representing the raw response structure from NASA's NEO (Near Earth Object) API.
 *
 * <p>This DTO mirrors the structure of the external API's JSON response. It allows the internal
 * system to deserialize and navigate the data structure easily before transforming it for public use.</p>
 *
 * @param near_earth_objects A map where the key is a date (String) and the value is an Object (array of asteroids)
 */
public record NasaApiResponse(
        Map<String, Object> near_earth_objects
) {}

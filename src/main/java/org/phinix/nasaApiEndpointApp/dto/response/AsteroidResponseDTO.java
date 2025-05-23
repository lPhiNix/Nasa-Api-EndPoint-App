package org.phinix.nasaApiEndpointApp.dto.response;

/**
 * Data Transfer Object representing simplified information about an asteroid.
 *
 * <p>This record is used to decouple the internal representation of asteroid data
 * from the external NASA API format. It ensures the controller exposes only the necessary
 * and formatted fields to clients.</p>
 *
 * @param name    Name of the asteroid
 * @param diameter Average estimated diameter in kilometers
 * @param speed   Relative velocity in kilometers per hour
 * @param date    Close approach date
 * @param planet  Planet the asteroid is approaching
 */
public record AsteroidResponseDTO(
        String name,
        double diameter,
        double speed,
        String date,
        String planet
) {}

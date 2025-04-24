package org.phinix.nasaApiEndpointApp.dto.response;

public record AsteroidResponseDTO(
    String name,
    double diameter,
    double speed,
    String date,
    String planet
) {}

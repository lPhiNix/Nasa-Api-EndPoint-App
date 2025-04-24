package org.phinix.nasaApiEndpointApp.dto.response;

public record NasaAsteroidsResponseDTO(
    String name,
    double diameter,
    double speed,
    String date,
    String planet
) {}

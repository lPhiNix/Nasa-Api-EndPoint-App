package org.phinix.nasaApiEndpointApp.dto.external;

import java.util.Map;

public record NasaApiResponse(
        Map<String, Object> near_earth_objects
) {}

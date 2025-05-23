package org.phinix.nasaApiEndpointApp.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating and managing a {@link RestTemplate} bean.
 *
 * <p><strong>Architectural Purpose:</strong></p>
 * <ul>
 *     <li>Provides a centralized configuration for HTTP communication in the application.</li>
 *     <li>Uses Spring Boot's {@link RestTemplateBuilder} for potential customization such as timeouts, interceptors, or message converters.</li>
 *     <li>Encapsulates third-party HTTP client setup to follow the single-responsibility principle.</li>
 *     <li>By declaring the RestTemplate as a bean, it becomes injectable into services like {@code NasaClient}.</li>
 * </ul>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a {@link RestTemplate} bean using Spring Boot's {@link RestTemplateBuilder}.
     *
     * @param builder preconfigured builder injected by Spring Boot
     * @return configured {@code RestTemplate} instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Builds a basic RestTemplate with default configurations.
        return builder.build();
    }
}

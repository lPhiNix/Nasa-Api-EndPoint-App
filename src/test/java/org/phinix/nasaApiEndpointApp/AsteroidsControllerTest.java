package org.phinix.nasaApiEndpointApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.phinix.nasaApiEndpointApp.dto.external.NasaApiResponse;
import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.phinix.nasaApiEndpointApp.service.AsteroidsService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AsteroidsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AsteroidsService service;

    @InjectMocks
    private AsteroidsController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testGetAllAsteroids() throws Exception {
        // Arrange
        int days = 3;
        NasaApiResponse mockResponse = new NasaApiResponse(Map.of());
        when(service.getRawAsteroids(days)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/asteroids")
                        .param("days", String.valueOf(days))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));  // Empty JSON because mockResponse is empty object

        verify(service, times(1)).getRawAsteroids(days);
    }

    @Test
    public void testGetSimplifyAsteroids() throws Exception {
        // Arrange
        int days = 2;
        List<AsteroidResponseDTO> mockAsteroids = List.of(
                new AsteroidResponseDTO("Asteroid 1", 1.5, 30000.0, "2025-05-26", "Earth"),
                new AsteroidResponseDTO("Asteroid 2", 2.0, 25000.0, "2025-05-27", "Mars")
        );
        when(service.getMappedAsteroids(days)).thenReturn(mockAsteroids);

        // Act & Assert
        mockMvc.perform(get("/api/v1/asteroids/simplify")
                        .param("days", String.valueOf(days))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Asteroid 1"))
                .andExpect(jsonPath("$[1].diameter").value(2.0));

        verify(service, times(1)).getMappedAsteroids(days);
    }

    @Test
    public void testGetTopDangerousAsteroids() throws Exception {
        // Arrange
        int days = 5;
        List<AsteroidResponseDTO> mockTopDangerous = List.of(
                new AsteroidResponseDTO("Dangerous 1", 5.0, 40000.0, "2025-05-26", "Earth"),
                new AsteroidResponseDTO("Dangerous 2", 4.5, 38000.0, "2025-05-27", "Venus"),
                new AsteroidResponseDTO("Dangerous 3", 4.0, 35000.0, "2025-05-28", "Mars")
        );
        when(service.getTopDangerousAsteroids(days)).thenReturn(mockTopDangerous);

        // Act & Assert
        mockMvc.perform(get("/api/v1/asteroids/top-dangerous")
                        .param("days", String.valueOf(days))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Dangerous 1"))
                .andExpect(jsonPath("$[2].planet").value("Mars"));

        verify(service, times(1)).getTopDangerousAsteroids(days);
    }
}

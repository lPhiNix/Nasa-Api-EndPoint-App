package org.phinix.nasaApiEndpointApp.controller;

import org.phinix.nasaApiEndpointApp.dto.response.AsteroidResponseDTO;
import org.phinix.nasaApiEndpointApp.service.AsteroidsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/asteroids")
public class AsteroidsController {
    private final AsteroidsService service;

    public AsteroidsController(AsteroidsService service) {
        this.service = service;
    }

    @GetMapping
    public Object getAllAsteroids(@RequestParam int days) {
        return service.getRawAsteroids(days);
    }

    @GetMapping("/simplify")
    public List<AsteroidResponseDTO> getSimplifyAsteroids(@RequestParam int days) {
        return service.getMappedAsteroids(days);
    }
}
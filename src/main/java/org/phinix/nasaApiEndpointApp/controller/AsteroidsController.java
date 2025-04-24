package org.phinix.nasaApiEndpointApp.controller;

import org.phinix.nasaApiEndpointApp.service.AsteroidsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
public class AsteroidsController {
    private final AsteroidsService service;

    public AsteroidsController(AsteroidsService service) {
        this.service = service;
    }

    @GetMapping("/asteroids")
    public Object getAsteroids(@RequestParam int days) {
        return service.getRawAsteroids(days);
    }
}

package com.example.auth.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Get weather for Oslo (default location)
     * GET /api/weather
     */
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather() {
        // Default to Oslo coordinates
        return getWeatherByLocation(59.9139, 10.7522);
    }

    /**
     * Get weather by coordinates
     * GET /api/weather?lat=59.9139&lon=10.7522
     */
    @GetMapping("/location")
    public ResponseEntity<WeatherResponse> getWeatherByLocation(
            @RequestParam double lat,
            @RequestParam double lon) {

        WeatherResponse weather = weatherService.getWeather(lat, lon);
        return ResponseEntity.ok(weather);
    }
}
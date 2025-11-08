package com.example.weather;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
// NO @CrossOrigin annotation here - CORS is handled by API Gateway
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();

    // Default location: Oslo
    private static final String DEFAULT_LAT = "59.9333";
    private static final String DEFAULT_LON = "10.7166";

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWeather(
            @RequestParam(required = false, defaultValue = DEFAULT_LAT) String lat,
            @RequestParam(required = false, defaultValue = DEFAULT_LON) String lon) {

        try {
            // Build Met.no API URL
            String metNoUrl = String.format(
                    "https://api.met.no/weatherapi/nowcast/2.0/complete?lat=%s&lon=%s",
                    lat, lon
            );

            // Set required User-Agent header (Met.no requirement)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CardGameWeatherService/1.0 (hugo@harnaes.no)");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call Met.no API
            ResponseEntity<Map> response = restTemplate.exchange(
                    metNoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Extract and simplify the response
            Map<String, Object> weatherData = simplifyWeatherData(response.getBody());

            return ResponseEntity.ok(weatherData);

        } catch (Exception e) {
            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch weather data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Map<String, Object> simplifyWeatherData(Map<String, Object> metNoData) {
        Map<String, Object> simplified = new HashMap<>();

        try {
            // Extract relevant data from Met.no response
            Map<String, Object> properties = (Map<String, Object>) metNoData.get("properties");

            if (properties != null && properties.containsKey("timeseries")) {
                var timeseries = (java.util.List<?>) properties.get("timeseries");

                if (!timeseries.isEmpty()) {
                    Map<String, Object> firstEntry = (Map<String, Object>) timeseries.get(0);
                    Map<String, Object> data = (Map<String, Object>) firstEntry.get("data");
                    Map<String, Object> instant = (Map<String, Object>) data.get("instant");
                    Map<String, Object> details = (Map<String, Object>) instant.get("details");

                    // Extract temperature and other details
                    simplified.put("temperature", details.get("air_temperature"));
                    simplified.put("humidity", details.get("relative_humidity"));
                    simplified.put("windSpeed", details.get("wind_speed"));
                    simplified.put("windDirection", details.get("wind_from_direction"));

                    // Get precipitation if available
                    if (data.containsKey("next_1_hours")) {
                        Map<String, Object> next1Hour = (Map<String, Object>) data.get("next_1_hours");
                        Map<String, Object> next1Details = (Map<String, Object>) next1Hour.get("details");
                        simplified.put("precipitation", next1Details.get("precipitation_amount"));

                        Map<String, Object> summary = (Map<String, Object>) next1Hour.get("summary");
                        simplified.put("condition", summary.get("symbol_code"));
                    }

                    simplified.put("location", String.format("Lat: %s, Lon: %s",
                            properties.get("meta") != null ?
                                    ((Map<?, ?>) properties.get("meta")).get("latitude") : "N/A",
                            properties.get("meta") != null ?
                                    ((Map<?, ?>) properties.get("meta")).get("longitude") : "N/A"
                    ));
                }
            }

            simplified.put("source", "MET Norway");

        } catch (Exception e) {
            simplified.put("error", "Failed to parse weather data");
            simplified.put("rawData", metNoData);
        }

        return simplified;
    }
}
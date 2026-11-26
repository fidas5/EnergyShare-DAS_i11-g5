package tn.esprit.energyshare.monitoring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/readings")
public class EnergyReadingController {

    @GetMapping
    public Map<String, String> getReadings() {
        return Map.of(
                "service", "energy-monitoring-service",
                "message", "Monitoring accessible via API Gateway"
        );
    }
}


package tn.esprit.energyshare.gateway.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/monitoring")
    public Mono<ResponseEntity<Map<String, Object>>> monitoringFallback() {

        return Mono.just(ResponseEntity.ok(Map.of(
                "message", "Service Energy Monitoring temporairement indisponible",
                "degraded", true
        )));
    }


    @GetMapping("/fallback/marketplace")
    public Mono<ResponseEntity<Map<String, Object>>> marketplaceFallback() {

        return Mono.just(ResponseEntity.ok(Map.of(
                "message", "Service Energy Marketplace temporairement indisponible",
                "degraded", true
        )));
    }

}

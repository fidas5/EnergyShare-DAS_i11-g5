package tn.esprit.energyshare.marketplace.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.service.EnergyMatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class EnergyMatchingController {

    private final EnergyMatchingService matchingService;

    public EnergyMatchingController(
            EnergyMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/requests/{requestId}/offers")
    public ResponseEntity<List<EnergyOffer>> findMatches(
            @PathVariable String requestId) {

        return ResponseEntity.ok(
                matchingService.findMatchingOffers(requestId)
        );
    }
}

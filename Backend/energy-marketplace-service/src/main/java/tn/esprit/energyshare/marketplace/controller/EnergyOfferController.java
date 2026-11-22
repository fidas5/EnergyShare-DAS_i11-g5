package tn.esprit.energyshare.marketplace.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.energyshare.marketplace.dto.CreateOfferRequest;
import tn.esprit.energyshare.marketplace.dto.UpdateOfferRequest;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.service.EnergyOfferService;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class EnergyOfferController {

    private final EnergyOfferService service;

    public EnergyOfferController(EnergyOfferService service) {
        this.service = service;
    }

    // POST /api/offers
    @PostMapping
    public ResponseEntity<EnergyOffer> createOffer(
            @RequestBody CreateOfferRequest request
    ) {
        EnergyOffer offer = service.createOffer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offer);
    }

    // GET /api/offers
    @GetMapping
    public ResponseEntity<List<EnergyOffer>> getAllOffers() {
        return ResponseEntity.ok(
                service.getAllOffers()
        );
    }

    // GET /api/offers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EnergyOffer> getOfferById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                service.getOfferById(id)
        );
    }

    // PUT /api/offers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EnergyOffer> updateOffer(
            @PathVariable String id,
            @RequestBody UpdateOfferRequest request
    ) {
        return ResponseEntity.ok(
                service.updateOffer(id, request)
        );
    }
}

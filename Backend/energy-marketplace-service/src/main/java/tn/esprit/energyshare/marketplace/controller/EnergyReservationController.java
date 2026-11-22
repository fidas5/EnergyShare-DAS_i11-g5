package tn.esprit.energyshare.marketplace.controller;

import tn.esprit.energyshare.marketplace.dto.CreateReservationRequest;
import tn.esprit.energyshare.marketplace.entity.EnergyReservation;
import tn.esprit.energyshare.marketplace.service.EnergyReservationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class EnergyReservationController {

    private final EnergyReservationService service;

    public EnergyReservationController(
            EnergyReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EnergyReservation> create(
            @RequestBody CreateReservationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createReservation(request));
    }

    @GetMapping
    public ResponseEntity<List<EnergyReservation>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnergyReservation> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable String id) {

        service.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}

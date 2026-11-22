package tn.esprit.energyshare.marketplace.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.energyshare.marketplace.dto.CreateRequestDTO;
import tn.esprit.energyshare.marketplace.entity.EnergyRequest;
import tn.esprit.energyshare.marketplace.service.EnergyRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class EnergyRequestController {

    private final EnergyRequestService service;

    public EnergyRequestController(EnergyRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EnergyRequest> create(
            @RequestBody CreateRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createRequest(dto));
    }

    @GetMapping
    public ResponseEntity<List<EnergyRequest>> getAll() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnergyRequest> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(service.getRequestById(id));
    }

    @GetMapping("/household/{householdId}")
    public ResponseEntity<List<EnergyRequest>> getByHousehold(
            @PathVariable String householdId) {

        return ResponseEntity.ok(
                service.getRequestsByHousehold(householdId)
        );
    }
}

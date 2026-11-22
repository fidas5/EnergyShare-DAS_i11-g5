package tn.esprit.energyshare.marketplace.service;

import org.springframework.stereotype.Service;
import tn.esprit.energyshare.marketplace.dto.CreateRequestDTO;
import tn.esprit.energyshare.marketplace.entity.EnergyRequest;
import tn.esprit.energyshare.marketplace.enums.RequestStatus;
import tn.esprit.energyshare.marketplace.exception.ResourceNotFoundException;
import tn.esprit.energyshare.marketplace.repository.EnergyRequestRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyRequestService {

    private final EnergyRequestRepository repository;

    public EnergyRequestService(EnergyRequestRepository repository) {
        this.repository = repository;
    }

    public EnergyRequest createRequest(CreateRequestDTO dto) {

        if (dto.buyerHouseholdId() == null ||
                dto.buyerHouseholdId().isBlank()) {
            throw new IllegalArgumentException(
                    "Buyer household ID is required"
            );
        }

        if (dto.requestedKwh() == null ||
                !Double.isFinite(dto.requestedKwh()) ||
                dto.requestedKwh() <= 0) {
            throw new IllegalArgumentException(
                    "Requested quantity must be positive"
            );
        }

        if (dto.maxPrice() == null ||
                dto.maxPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Max price must be positive"
            );
        }

        if (dto.zone() == null || dto.zone().isBlank()) {
            throw new IllegalArgumentException("Zone is required");
        }

        if (dto.startTime() == null ||
                dto.endTime() == null ||
                !dto.startTime().isBefore(dto.endTime()) ||
                !dto.endTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Invalid availability period"
            );
        }

        EnergyRequest request = new EnergyRequest();

        request.setBuyerHouseholdId(dto.buyerHouseholdId());
        request.setRequestedKwh(dto.requestedKwh());
        request.setMaxPrice(dto.maxPrice());
        request.setZone(dto.zone());
        request.setStartTime(dto.startTime());
        request.setEndTime(dto.endTime());
        request.setStatus(RequestStatus.OPEN);

        return repository.save(request);
    }

    public List<EnergyRequest> getAllRequests() {
        return repository.findAll();
    }

    public EnergyRequest getRequestById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Energy request not found: " + id
                        ));
    }

    public List<EnergyRequest> getRequestsByHousehold(String householdId) {
        return repository.findByBuyerHouseholdId(householdId);
    }
}

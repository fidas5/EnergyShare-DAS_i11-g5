package tn.esprit.energyshare.marketplace.dto;



import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateRequestDTO(
        String buyerHouseholdId,
        Double requestedKwh,
        BigDecimal maxPrice,
        String zone,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}


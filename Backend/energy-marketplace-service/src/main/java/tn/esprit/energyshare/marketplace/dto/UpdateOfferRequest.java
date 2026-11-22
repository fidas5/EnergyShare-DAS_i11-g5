package tn.esprit.energyshare.marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateOfferRequest(
        Double quantityKwh,
        BigDecimal pricePerKwh,
        String zone,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
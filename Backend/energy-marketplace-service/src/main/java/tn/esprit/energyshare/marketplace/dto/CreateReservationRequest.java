package tn.esprit.energyshare.marketplace.dto;


public record CreateReservationRequest(
        String offerId,
        String requestId,
        String buyerHouseholdId,
        Double reservedKwh
) {}


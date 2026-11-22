package tn.esprit.energyshare.marketplace.service;



import org.springframework.stereotype.Service;
import tn.esprit.energyshare.marketplace.dto.CreateOfferRequest;
import tn.esprit.energyshare.marketplace.dto.UpdateOfferRequest;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.enums.OfferStatus;
import tn.esprit.energyshare.marketplace.exception.ResourceNotFoundException;
import tn.esprit.energyshare.marketplace.repository.EnergyOfferRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyOfferService {

    private final EnergyOfferRepository repository;

    public EnergyOfferService(EnergyOfferRepository repository) {
        this.repository = repository;
    }

    // CREATE OFFER
    public EnergyOffer createOffer(CreateOfferRequest request) {

        validateOffer(
                request.quantityKwh(),
                request.pricePerKwh(),
                request.zone(),
                request.startTime(),
                request.endTime()
        );

        if (request.sellerHouseholdId() == null ||
                request.sellerHouseholdId().isBlank()) {
            throw new IllegalArgumentException(
                    "Seller household ID is required"
            );
        }

        EnergyOffer offer = new EnergyOffer();

        offer.setSellerHouseholdId(request.sellerHouseholdId());
        offer.setQuantityKwh(request.quantityKwh());
        offer.setRemainingKwh(request.quantityKwh());
        offer.setPricePerKwh(request.pricePerKwh());
        offer.setZone(request.zone());
        offer.setStartTime(request.startTime());
        offer.setEndTime(request.endTime());
        offer.setStatus(OfferStatus.ACTIVE);

        return repository.save(offer);
    }

    // GET ALL OFFERS
    public List<EnergyOffer> getAllOffers() {
        return repository.findAll();
    }

    // GET OFFER BY ID
    public EnergyOffer getOfferById(String id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Offer not found: " + id
                        )
                );
    }

    // UPDATE OFFER
    public EnergyOffer updateOffer(
            String id,
            UpdateOfferRequest request
    ) {

        EnergyOffer offer = getOfferById(id);

        if (offer.getStatus() != OfferStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active offers can be modified"
            );
        }

        validateOffer(
                request.quantityKwh(),
                request.pricePerKwh(),
                request.zone(),
                request.startTime(),
                request.endTime()
        );

        // Une offre avec des réservations ne peut pas
        // être modifiée librement.
        if (Double.compare(
                offer.getQuantityKwh(),
                offer.getRemainingKwh()
        ) != 0) {
            throw new IllegalStateException(
                    "Offer has reserved energy"
            );
        }

        offer.setQuantityKwh(request.quantityKwh());
        offer.setRemainingKwh(request.quantityKwh());
        offer.setPricePerKwh(request.pricePerKwh());
        offer.setZone(request.zone());
        offer.setStartTime(request.startTime());
        offer.setEndTime(request.endTime());

        return repository.save(offer);
    }

    // BUSINESS VALIDATION
    private void validateOffer(
            Double quantity,
            BigDecimal price,
            String zone,
            LocalDateTime start,
            LocalDateTime end
    ) {

        if (quantity == null ||
                !Double.isFinite(quantity) ||
                quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (price == null ||
                price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }

        if (zone == null || zone.isBlank()) {
            throw new IllegalArgumentException(
                    "Zone is required"
            );
        }

        if (start == null || end == null ||
                !start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Invalid availability period"
            );
        }

        if (!end.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Offer end time must be in the future"
            );
        }
    }
}


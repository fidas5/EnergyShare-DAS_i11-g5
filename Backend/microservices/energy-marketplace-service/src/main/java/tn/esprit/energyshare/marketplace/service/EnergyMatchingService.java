package tn.esprit.energyshare.marketplace.service;

import org.springframework.stereotype.Service;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.entity.EnergyRequest;
import tn.esprit.energyshare.marketplace.enums.OfferStatus;
import tn.esprit.energyshare.marketplace.enums.RequestStatus;
import tn.esprit.energyshare.marketplace.exception.ResourceNotFoundException;
import tn.esprit.energyshare.marketplace.repository.EnergyOfferRepository;
import tn.esprit.energyshare.marketplace.repository.EnergyRequestRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class EnergyMatchingService {

    private final EnergyOfferRepository offerRepository;
    private final EnergyRequestRepository requestRepository;

    public EnergyMatchingService(
            EnergyOfferRepository offerRepository,
            EnergyRequestRepository requestRepository) {
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
    }

    public List<EnergyOffer> findMatchingOffers(String requestId) {

        EnergyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Energy request not found: " + requestId
                        )
                );

        if (request.getStatus() != RequestStatus.OPEN
                && request.getStatus() != RequestStatus.PARTIALLY_FULFILLED) {
            throw new IllegalStateException(
                    "This request is not open for matching"
            );
        }

        return offerRepository.findByStatus(OfferStatus.ACTIVE)
                .stream()
                .filter(offer ->
                        offer.getRemainingKwh() != null &&
                                offer.getRemainingKwh() >= request.getRequestedKwh()
                )
                .filter(offer ->
                        offer.getPricePerKwh() != null &&
                                offer.getPricePerKwh()
                                        .compareTo(request.getMaxPrice()) <= 0
                )
                .filter(offer ->
                        offer.getZone() != null &&
                                offer.getZone().trim().equalsIgnoreCase(
                                        request.getZone().trim()
                                )
                )
                .filter(offer ->
                        !offer.getStartTime().isAfter(request.getStartTime())
                                && !offer.getEndTime().isBefore(request.getEndTime())
                )
                .filter(offer ->
                        !offer.getSellerHouseholdId().equals(
                                request.getBuyerHouseholdId()
                        )
                )
                .sorted(Comparator.comparing(EnergyOffer::getPricePerKwh))
                .toList();
    }
}

package tn.esprit.energyshare.marketplace.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.enums.OfferStatus;

import java.util.List;

public interface EnergyOfferRepository
        extends MongoRepository<EnergyOffer, String> {

    List<EnergyOffer> findByStatus(
            OfferStatus status
    );

    List<EnergyOffer> findBySellerHouseholdId(
            String sellerHouseholdId
    );

    List<EnergyOffer> findByZoneAndStatus(
            String zone,
            OfferStatus status
    );
}

package tn.esprit.energyshare.marketplace.repository;

import tn.esprit.energyshare.marketplace.entity.EnergyReservation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EnergyReservationRepository
        extends MongoRepository<EnergyReservation, String> {

    List<EnergyReservation> findByOfferId(String offerId);

    List<EnergyReservation> findByBuyerHouseholdId(
            String buyerHouseholdId
    );
}

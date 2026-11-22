package tn.esprit.energyshare.marketplace.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.energyshare.marketplace.entity.EnergyRequest;
import tn.esprit.energyshare.marketplace.enums.RequestStatus;

import java.util.List;

public interface EnergyRequestRepository
        extends MongoRepository<EnergyRequest, String> {

    List<EnergyRequest> findByBuyerHouseholdId(String buyerHouseholdId);

    List<EnergyRequest> findByStatus(RequestStatus status);
}

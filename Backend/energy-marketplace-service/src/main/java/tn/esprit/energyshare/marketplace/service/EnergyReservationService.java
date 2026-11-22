
package tn.esprit.energyshare.marketplace.service;

import tn.esprit.energyshare.marketplace.dto.CreateReservationRequest;
import tn.esprit.energyshare.marketplace.entity.EnergyOffer;
import tn.esprit.energyshare.marketplace.entity.EnergyRequest;
import tn.esprit.energyshare.marketplace.entity.EnergyReservation;
import tn.esprit.energyshare.marketplace.enums.OfferStatus;
import tn.esprit.energyshare.marketplace.enums.RequestStatus;
import tn.esprit.energyshare.marketplace.enums.ReservationStatus;
import tn.esprit.energyshare.marketplace.exception.ResourceNotFoundException;
import tn.esprit.energyshare.marketplace.repository.EnergyReservationRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyReservationService {

    private static final double EPS = 0.000001;

    private final EnergyReservationRepository repository;
    private final MongoTemplate mongoTemplate;

    public EnergyReservationService(
            EnergyReservationRepository repository,
            MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public EnergyReservation createReservation(
            CreateReservationRequest dto) {

        if (dto.offerId() == null || dto.offerId().isBlank()
                || dto.buyerHouseholdId() == null
                || dto.buyerHouseholdId().isBlank()
                || dto.reservedKwh() == null
                || !Double.isFinite(dto.reservedKwh())
                || dto.reservedKwh() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid reservation data");
        }

        LocalDateTime now = LocalDateTime.now();

        EnergyOffer offer = mongoTemplate.findById(
                dto.offerId(), EnergyOffer.class);

        if (offer == null) {
            throw new ResourceNotFoundException(
                    "Offer not found: " + dto.offerId());
        }

        if (offer.getSellerHouseholdId()
                .equals(dto.buyerHouseholdId())) {
            throw new IllegalArgumentException(
                    "Cannot reserve your own offer");
        }

        if (offer.getStatus() != OfferStatus.ACTIVE
                || offer.getStartTime() == null
                || offer.getEndTime() == null
                || !offer.getEndTime().isAfter(now)) {
            throw new IllegalStateException(
                    "Offer is not available");
        }

        String requestId = dto.requestId();

        if (requestId != null && !requestId.isBlank()) {

            EnergyRequest request = mongoTemplate.findById(
                    requestId, EnergyRequest.class);

            if (request == null) {
                throw new ResourceNotFoundException(
                        "Request not found: " + requestId);
            }

            if (!request.getBuyerHouseholdId()
                    .equals(dto.buyerHouseholdId())) {
                throw new IllegalArgumentException(
                        "Request belongs to another household");
            }

            if (request.getStatus() != RequestStatus.OPEN
                    && request.getStatus()
                    != RequestStatus.PARTIALLY_FULFILLED) {
                throw new IllegalStateException(
                        "Request is not open");
            }

            // Validate matching criteria.
            if (offer.getZone() == null
                    || request.getZone() == null
                    || !offer.getZone().trim()
                    .equalsIgnoreCase(request.getZone().trim())
                    || offer.getPricePerKwh()
                    .compareTo(request.getMaxPrice()) > 0
                    || offer.getStartTime()
                    .isAfter(request.getStartTime())
                    || offer.getEndTime()
                    .isBefore(request.getEndTime())) {

                throw new IllegalStateException(
                        "Offer does not match this request");
            }

            Double previous = request.getReservedKwh();

            if (previous == null) {
                throw new IllegalStateException(
                        "Request needs data migration");
            }

            double total = previous + dto.reservedKwh();

            if (total > request.getRequestedKwh() + EPS) {
                throw new IllegalStateException(
                        "Reservation exceeds requested quantity");
            }

            RequestStatus newStatus =
                    total >= request.getRequestedKwh() - EPS
                            ? RequestStatus.FULFILLED
                            : RequestStatus.PARTIALLY_FULFILLED;

            // Conditional update prevents stale totals.
            Query requestQuery = new Query(
                    Criteria.where("_id").is(requestId)
                            .and("reservedKwh").is(previous)
                            .and("status").is(request.getStatus())
            );

            Update requestUpdate = new Update()
                    .inc("reservedKwh", dto.reservedKwh())
                    .set("status", newStatus);

            var result = mongoTemplate.updateFirst(
                    requestQuery, requestUpdate,
                    EnergyRequest.class);

            if (result.getModifiedCount() != 1) {
                throw new IllegalStateException(
                        "Request changed concurrently");
            }
        } else {
            requestId = null;
        }

        // Atomically reserve quantity on the offer.
        Query offerQuery = new Query(
                Criteria.where("_id").is(dto.offerId())
                        .and("status").is(OfferStatus.ACTIVE)
                        .and("remainingKwh").gte(dto.reservedKwh())
                        .and("endTime").gt(now)
        );

        Update offerUpdate = new Update()
                .inc("remainingKwh", -dto.reservedKwh());

        var offerResult = mongoTemplate.updateFirst(
                offerQuery, offerUpdate, EnergyOffer.class);

        if (offerResult.getModifiedCount() != 1) {
            throw new IllegalStateException(
                    "Insufficient available energy");
        }

        EnergyReservation reservation =
                new EnergyReservation();

        reservation.setOfferId(dto.offerId());
        reservation.setRequestId(requestId);
        reservation.setBuyerHouseholdId(
                dto.buyerHouseholdId());
        reservation.setReservedKwh(dto.reservedKwh());
        reservation.setCreatedAt(now);
        reservation.setExpiresAt(now.plusMinutes(30));
        reservation.setStatus(ReservationStatus.PENDING);

        return repository.save(reservation);
    }

    public EnergyReservation getById(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found: " + id));
    }

    public List<EnergyReservation> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void cancelReservation(String id) {

        EnergyReservation reservation = getById(id);

        // Change status exactly once.
        Query reservationQuery = new Query(
                Criteria.where("_id").is(id)
                        .and("status").is(ReservationStatus.PENDING)
        );

        var changed = mongoTemplate.updateFirst(
                reservationQuery,
                new Update().set(
                        "status", ReservationStatus.CANCELLED),
                EnergyReservation.class);

        if (changed.getModifiedCount() != 1) {
            throw new IllegalStateException(
                    "Only pending reservations can be cancelled");
        }

        // Return energy to the offer.
        var offerResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id")
                        .is(reservation.getOfferId())),
                new Update().inc(
                        "remainingKwh", reservation.getReservedKwh()),
                EnergyOffer.class);

        if (offerResult.getModifiedCount() != 1) {
            throw new IllegalStateException(
                    "Associated offer cannot be updated");
        }

        // Update request only if reservation is linked.
        if (reservation.getRequestId() != null) {

            EnergyRequest request = mongoTemplate.findById(
                    reservation.getRequestId(),
                    EnergyRequest.class);

            if (request == null
                    || request.getReservedKwh() == null) {
                throw new IllegalStateException(
                        "Associated request is invalid");
            }

            double remaining = request.getReservedKwh()
                    - reservation.getReservedKwh();

            if (remaining < -EPS) {
                throw new IllegalStateException(
                        "Inconsistent reserved quantity");
            }

            remaining = Math.max(0.0, remaining);

            RequestStatus newStatus =
                    remaining < EPS
                            ? RequestStatus.OPEN
                            : RequestStatus.PARTIALLY_FULFILLED;

            if (request.getStatus() == RequestStatus.CANCELLED) {
                newStatus = RequestStatus.CANCELLED;
            }

            var requestResult = mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id")
                            .is(request.getId())
                            .and("reservedKwh")
                            .is(request.getReservedKwh())),
                    new Update()
                            .inc("reservedKwh",
                                    -reservation.getReservedKwh())
                            .set("status", newStatus),
                    EnergyRequest.class);

            if (requestResult.getModifiedCount() != 1) {
                throw new IllegalStateException(
                        "Request changed concurrently");
            }
        }
    }
}

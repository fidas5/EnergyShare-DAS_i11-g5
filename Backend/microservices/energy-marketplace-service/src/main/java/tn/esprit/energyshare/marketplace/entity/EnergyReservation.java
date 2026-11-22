package tn.esprit.energyshare.marketplace.entity;

import tn.esprit.energyshare.marketplace.enums.ReservationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "energy_reservations")
public class EnergyReservation {

    @Id
    private String id;

    private String offerId;
    private String requestId;
    private String buyerHouseholdId;
    private Double reservedKwh;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private ReservationStatus status;

    public EnergyReservation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBuyerHouseholdId() {
        return buyerHouseholdId;
    }
    public void setBuyerHouseholdId(String buyerHouseholdId) {
        this.buyerHouseholdId = buyerHouseholdId;
    }

    public Double getReservedKwh() { return reservedKwh; }
    public void setReservedKwh(Double reservedKwh) {
        this.reservedKwh = reservedKwh;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}


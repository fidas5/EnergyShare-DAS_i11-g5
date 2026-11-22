package tn.esprit.energyshare.marketplace.entity;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tn.esprit.energyshare.marketplace.enums.OfferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "energy_offers")
public class EnergyOffer {

    @Id
    private String id;

    private String sellerHouseholdId;

    private Double quantityKwh;

    private Double remainingKwh;

    private BigDecimal pricePerKwh;

    private String zone;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private OfferStatus status;

    public EnergyOffer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSellerHouseholdId() {
        return sellerHouseholdId;
    }

    public void setSellerHouseholdId(
            String sellerHouseholdId) {
        this.sellerHouseholdId = sellerHouseholdId;
    }

    public Double getQuantityKwh() {
        return quantityKwh;
    }

    public void setQuantityKwh(Double quantityKwh) {
        this.quantityKwh = quantityKwh;
    }

    public Double getRemainingKwh() {
        return remainingKwh;
    }

    public void setRemainingKwh(Double remainingKwh) {
        this.remainingKwh = remainingKwh;
    }

    public BigDecimal getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(BigDecimal pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }
}

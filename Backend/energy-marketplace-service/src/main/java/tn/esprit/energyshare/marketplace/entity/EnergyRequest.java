package tn.esprit.energyshare.marketplace.entity;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tn.esprit.energyshare.marketplace.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "energy_requests")
public class EnergyRequest {

    @Id
    private String id;

    private String buyerHouseholdId;
    private Double requestedKwh;
    private BigDecimal maxPrice;
    private String zone;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RequestStatus status;

    public EnergyRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuyerHouseholdId() { return buyerHouseholdId; }
    public void setBuyerHouseholdId(String buyerHouseholdId) {
        this.buyerHouseholdId = buyerHouseholdId;
    }

    public Double getRequestedKwh() { return requestedKwh; }
    public void setRequestedKwh(Double requestedKwh) {
        this.requestedKwh = requestedKwh;
    }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    private Double reservedKwh = 0.0;

    public Double getReservedKwh() {
        return reservedKwh;
    }

    public void setReservedKwh(Double reservedKwh) {
        this.reservedKwh = reservedKwh;
    }
}

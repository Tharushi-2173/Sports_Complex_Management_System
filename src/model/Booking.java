
package model;

import java.time.LocalDateTime;


public abstract class Booking 
{
    
    private Long id;
    private Long memberId;
    private Long facilityId;
    private Long coachId; // optional
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status = BookingStatus.CONFIRMED;
    private double facilityFee;
    private double coachFee;
    private double totalFee;

    // ABSTRACTION: Abstract method that must be implemented by child classes
    // This enforces a contract while allowing different implementations
    public abstract double calculateCost(double hourlyRate);

    // INHERITANCE: Common getter/setter methods inherited by all child classes
   
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

    public Long getCoachId() { return coachId; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public double getFacilityFee() { return facilityFee; }
    public void setFacilityFee(double facilityFee) { this.facilityFee = facilityFee; }

    public double getCoachFee() { return coachFee; }
    public void setCoachFee(double coachFee) { this.coachFee = coachFee; }

    public double getTotalFee() { return totalFee; }
    public void setTotalFee(double totalFee) { this.totalFee = totalFee; }
    
}



package service;

import model.FacilityBooking;
import model.TrainingBooking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    Long createFacilityBooking(FacilityBooking booking);
    Long createTrainingBooking(TrainingBooking booking);
    void cancel(Long bookingId);
    List<Object> getFacilityAvailability(Long facilityId, LocalDateTime from, LocalDateTime to);
}



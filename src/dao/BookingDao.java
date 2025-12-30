package dao;

import model.BookingStatus;
import model.FacilityBooking;
import model.TrainingBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingDao {
    Long createFacilityBooking(FacilityBooking booking);
    Long createTrainingBooking(TrainingBooking booking);
    void updateStatus(Long bookingId, BookingStatus status);
    Optional<FacilityBooking> findFacilityBookingById(Long id);
    Optional<TrainingBooking> findTrainingBookingById(Long id);
    List<Object> findAll();
    List<Object> findByFacilityAndRange(Long facilityId, LocalDateTime start, LocalDateTime end);
    boolean existsOverlap(Long facilityId, LocalDateTime start, LocalDateTime end);
}



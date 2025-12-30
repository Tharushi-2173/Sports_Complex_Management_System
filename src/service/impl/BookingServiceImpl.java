package service.impl;

import dao.BookingDao;
import dao.FacilityDao;
import model.*;
import service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

public class BookingServiceImpl implements BookingService {
    private final BookingDao bookingDao;
    private final FacilityDao facilityDao;

    public BookingServiceImpl(BookingDao bookingDao, FacilityDao facilityDao) {
        this.bookingDao = bookingDao;
        this.facilityDao = facilityDao;
    }

    @Override
    public Long createFacilityBooking(FacilityBooking booking) {
        validateTimes(booking.getStartTime(), booking.getEndTime());
        ensureFacilityAvailable(booking.getFacilityId());
        ensureNoOverlap(booking.getFacilityId(), booking.getStartTime(), booking.getEndTime());
        return bookingDao.createFacilityBooking(booking);
    }

    @Override
    public Long createTrainingBooking(TrainingBooking booking) {
        validateTimes(booking.getStartTime(), booking.getEndTime());
        ensureFacilityAvailable(booking.getFacilityId());
        ensureNoOverlap(booking.getFacilityId(), booking.getStartTime(), booking.getEndTime());
        return bookingDao.createTrainingBooking(booking);
    }

    @Override
    public void cancel(Long bookingId) {
        bookingDao.updateStatus(bookingId, BookingStatus.CANCELLED);
    }

    @Override
    public List<Object> getFacilityAvailability(Long facilityId, LocalDateTime from, LocalDateTime to) {
        return bookingDao.findByFacilityAndRange(facilityId, from, to);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("Start and end required");
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time");
    }

    private void ensureFacilityAvailable(Long facilityId) {
        Facility f = facilityDao.findById(facilityId).orElseThrow(() -> new IllegalArgumentException("Facility not found"));
        if (f.getStatus() != FacilityStatus.AVAILABLE) {
            throw new IllegalStateException("Facility not available");
        }
    }

    private void ensureNoOverlap(Long facilityId, LocalDateTime start, LocalDateTime end) {
        if (bookingDao.existsOverlap(facilityId, start, end)) {
            throw new IllegalStateException("Overlapping booking exists for this facility and time range");
        }
    }
}



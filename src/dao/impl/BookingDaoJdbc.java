package dao.impl;

import dao.BookingDao;
import db.ConnectionManager;
import model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDaoJdbc implements BookingDao {
    @Override
    public Long createFacilityBooking(FacilityBooking booking) {
        String sql = "INSERT INTO bookings(member_id, facility_id, coach_id, start_time, end_time, type, status, facility_fee, coach_fee, total_fee) VALUES(?,?,?,?,?,'FACILITY',?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getMemberId());
            ps.setLong(2, booking.getFacilityId());
            if (booking.getCoachId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, booking.getCoachId());
            ps.setTimestamp(4, Timestamp.valueOf(booking.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(booking.getEndTime()));
            ps.setString(6, booking.getStatus().name());
            ps.setDouble(7, booking.getFacilityFee());
            ps.setDouble(8, booking.getCoachFee());
            ps.setDouble(9, booking.getTotalFee());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert facility booking", e);
        }
    }

    @Override
    public Long createTrainingBooking(TrainingBooking booking) {
        String sql = "INSERT INTO bookings(member_id, facility_id, coach_id, start_time, end_time, type, status, facility_fee, coach_fee, total_fee) VALUES(?,?,?,?,?,'TRAINING',?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getMemberId());
            ps.setLong(2, booking.getFacilityId());
            if (booking.getCoachId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, booking.getCoachId());
            ps.setTimestamp(4, Timestamp.valueOf(booking.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(booking.getEndTime()));
            ps.setString(6, booking.getStatus().name());
            ps.setDouble(7, booking.getFacilityFee());
            ps.setDouble(8, booking.getCoachFee());
            ps.setDouble(9, booking.getTotalFee());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert training booking", e);
        }
    }

    @Override
    public void updateStatus(Long bookingId, BookingStatus status) {
        String sql = "UPDATE bookings SET status=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update booking status", e);
        }
    }

    @Override
    public Optional<FacilityBooking> findFacilityBookingById(Long id) {
        String sql = "SELECT * FROM bookings WHERE id=? AND type='FACILITY'";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapFacility(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find facility booking", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TrainingBooking> findTrainingBookingById(Long id) {
        String sql = "SELECT * FROM bookings WHERE id=? AND type='TRAINING'";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapTraining(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find training booking", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Object> findAll() {
        String sql = "SELECT * FROM bookings ORDER BY start_time DESC";
        List<Object> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("type");
                if ("FACILITY".equals(type)) list.add(mapFacility(rs)); else list.add(mapTraining(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list bookings", e);
        }
        return list;
    }

    @Override
    public List<Object> findByFacilityAndRange(Long facilityId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM bookings WHERE facility_id=? AND NOT (end_time<=? OR start_time>=?) ORDER BY start_time";
        List<Object> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, facilityId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    if ("FACILITY".equals(type)) list.add(mapFacility(rs)); else list.add(mapTraining(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query bookings by range", e);
        }
        return list;
    }

    @Override
    public boolean existsOverlap(Long facilityId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE facility_id=? AND status='CONFIRMED' AND NOT (end_time<=? OR start_time>=?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, facilityId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check booking overlap", e);
        }
        return true;
    }

    private FacilityBooking mapFacility(ResultSet rs) throws SQLException {
        FacilityBooking b = new FacilityBooking();
        fillCommon(rs, b);
        return b;
    }

    private TrainingBooking mapTraining(ResultSet rs) throws SQLException {
        TrainingBooking b = new TrainingBooking();
        fillCommon(rs, b);
        return b;
    }

    private void fillCommon(ResultSet rs, Booking b) throws SQLException {
        b.setId(rs.getLong("id"));
        b.setMemberId(rs.getLong("member_id"));
        b.setFacilityId(rs.getLong("facility_id"));
        long coach = rs.getLong("coach_id");
        if (!rs.wasNull()) b.setCoachId(coach);
        Timestamp s = rs.getTimestamp("start_time");
        Timestamp e = rs.getTimestamp("end_time");
        if (s != null) b.setStartTime(s.toLocalDateTime());
        if (e != null) b.setEndTime(e.toLocalDateTime());
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        try {
            b.setFacilityFee(rs.getDouble("facility_fee"));
            b.setCoachFee(rs.getDouble("coach_fee"));
            b.setTotalFee(rs.getDouble("total_fee"));
        } catch (SQLException ignore) {
            // Backward compatibility if columns not present
        }
    }
}



package dao.impl;

import dao.PaymentDao;
import db.ConnectionManager;
import model.Payment;
import model.PaymentMethod;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDaoJdbc implements PaymentDao {
    @Override
    public Long create(Payment payment) {
        String sql = "INSERT INTO payments(booking_id, user_id, amount, discount, paid_at, method, reference) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getBookingId());
            ps.setLong(2, payment.getUserId());
            ps.setDouble(3, payment.getAmount());
            ps.setDouble(4, payment.getDiscount());
            ps.setTimestamp(5, Timestamp.valueOf(payment.getPaidAt() != null ? payment.getPaidAt() : LocalDateTime.now()));
            ps.setString(6, payment.getMethod().name());
            ps.setString(7, payment.getReference());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        String sql = "SELECT * FROM payments WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Payment> findByBooking(Long bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id=? ORDER BY paid_at DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list payments by booking", e);
        }
        return list;
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY paid_at DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list payments", e);
        }
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setBookingId(rs.getLong("booking_id"));
        p.setAmount(rs.getDouble("amount"));
        try { p.setUserId(rs.getLong("user_id")); } catch (SQLException ignore) {}
        p.setDiscount(rs.getDouble("discount"));
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) p.setPaidAt(paidAt.toLocalDateTime());
        p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
        p.setReference(rs.getString("reference"));
        return p;
    }
}



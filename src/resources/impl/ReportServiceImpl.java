package service.impl;

import db.ConnectionManager;
import service.ReportService;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReportServiceImpl implements ReportService {
    @Override
    public Map<String, Long> mostUsedFacilities(LocalDate from, LocalDate to) {
        String sql = "SELECT f.name, COUNT(*) cnt FROM bookings b JOIN facilities f ON b.facility_id=f.id WHERE b.status='CONFIRMED' AND DATE(b.start_time) BETWEEN ? AND ? GROUP BY f.name ORDER BY cnt DESC";
        Map<String, Long> map = new LinkedHashMap<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getLong(2));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return map;
    }

    @Override
    public Map<String, Long> busiestHours(LocalDate from, LocalDate to) {
        String sql = "SELECT DATE_FORMAT(start_time, '%H:00') hour_block, COUNT(*) cnt FROM bookings WHERE status='CONFIRMED' AND DATE(start_time) BETWEEN ? AND ? GROUP BY hour_block ORDER BY cnt DESC";
        Map<String, Long> map = new LinkedHashMap<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getLong(2));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return map;
    }

    @Override
    public Map<String, Double> incomeByMonth(int year) {
        String sql = "SELECT DATE_FORMAT(paid_at, '%Y-%m') ym, SUM(amount - discount) total FROM payments WHERE YEAR(paid_at)=? GROUP BY ym ORDER BY ym";
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return map;
    }

    @Override
    public List<String[]> rawPayments(LocalDate from, LocalDate to) {
        String sql = "SELECT id, booking_id, amount, discount, method, reference, paid_at FROM payments WHERE DATE(paid_at) BETWEEN ? AND ? ORDER BY paid_at";
        List<String[]> rows = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getLong("id")),
                        String.valueOf(rs.getLong("booking_id")),
                        String.format(Locale.US, "%.2f", rs.getDouble("amount")),
                        String.format(Locale.US, "%.2f", rs.getDouble("discount")),
                        rs.getString("method"),
                        rs.getString("reference"),
                        String.valueOf(rs.getTimestamp("paid_at"))
                    });
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return rows;
    }
}



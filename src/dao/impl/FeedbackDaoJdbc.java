package dao.impl;

import dao.FeedbackDao;
import db.ConnectionManager;
import model.Feedback;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDaoJdbc implements FeedbackDao {
    @Override
    public Long create(Feedback feedback) {
        String sql = "INSERT INTO feedback(user_id, facility_id, rating, comments) VALUES(?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, feedback.getUserId());
            if (feedback.getFacilityId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, feedback.getFacilityId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComments());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert feedback", e);
        }
    }

    @Override
    public List<Feedback> findAll() {
        String sql = "SELECT * FROM feedback ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list feedback", e);
        }
        return list;
    }

    @Override
    public List<Feedback> findByFacility(Long facilityId) {
        String sql = "SELECT * FROM feedback WHERE facility_id=? ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list feedback by facility", e);
        }
        return list;
    }

    @Override
    public List<Feedback> findByUser(Long userId) {
        String sql = "SELECT * FROM feedback WHERE user_id=? ORDER BY created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list feedback by user", e);
        }
        return list;
    }

    private Feedback map(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setId(rs.getLong("id"));
        f.setUserId(rs.getLong("user_id"));
        long facilityId = rs.getLong("facility_id");
        if (!rs.wasNull()) f.setFacilityId(facilityId);
        f.setRating(rs.getInt("rating"));
        f.setComments(rs.getString("comments"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) f.setCreatedAt(Instant.ofEpochMilli(created.getTime()));
        return f;
    }
}



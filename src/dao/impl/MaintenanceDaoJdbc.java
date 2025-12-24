package dao.impl;

import dao.MaintenanceDao;
import db.ConnectionManager;
import model.MaintenanceRequest;
import model.MaintenanceStatus;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceDaoJdbc implements MaintenanceDao {
    @Override
    public Long create(MaintenanceRequest request) {
        String sql = "INSERT INTO maintenance_requests(facility_id, requested_by, title, description, status) VALUES(?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, request.getFacilityId());
            ps.setLong(2, request.getRequestedBy());
            ps.setString(3, request.getTitle());
            ps.setString(4, request.getDescription());
            ps.setString(5, request.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert maintenance request", e);
        }
    }

    @Override
    public void update(MaintenanceRequest request) {
        String sql = "UPDATE maintenance_requests SET facility_id=?, requested_by=?, title=?, description=?, status=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, request.getFacilityId());
            ps.setLong(2, request.getRequestedBy());
            ps.setString(3, request.getTitle());
            ps.setString(4, request.getDescription());
            ps.setString(5, request.getStatus().name());
            ps.setLong(6, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update maintenance request", e);
        }
    }

    @Override
    public Optional<MaintenanceRequest> findById(Long id) {
        String sql = "SELECT * FROM maintenance_requests WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find maintenance request", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MaintenanceRequest> findAll() {
        String sql = "SELECT * FROM maintenance_requests ORDER BY created_at DESC";
        List<MaintenanceRequest> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list maintenance requests", e);
        }
        return list;
    }

    @Override
    public List<MaintenanceRequest> findByStatus(MaintenanceStatus status) {
        String sql = "SELECT * FROM maintenance_requests WHERE status=? ORDER BY created_at DESC";
        List<MaintenanceRequest> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list maintenance requests by status", e);
        }
        return list;
    }

    private MaintenanceRequest map(ResultSet rs) throws SQLException {
        MaintenanceRequest m = new MaintenanceRequest();
        m.setId(rs.getLong("id"));
        m.setFacilityId(rs.getLong("facility_id"));
        m.setRequestedBy(rs.getLong("requested_by"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setStatus(MaintenanceStatus.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) m.setCreatedAt(Instant.ofEpochMilli(created.getTime()));
        if (updated != null) m.setUpdatedAt(Instant.ofEpochMilli(updated.getTime()));
        return m;
    }
}



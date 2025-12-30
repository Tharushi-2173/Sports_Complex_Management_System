package dao.impl;

import dao.FacilityDao;
import db.ConnectionManager;
import model.Facility;
import model.FacilityStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacilityDaoJdbc implements FacilityDao {
    @Override
    public Long create(Facility facility) {
        String sql = "INSERT INTO facilities(name, description, hourly_rate, status) VALUES(?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, facility.getName());
            ps.setString(2, facility.getDescription());
            ps.setDouble(3, facility.getHourlyRate());
            ps.setString(4, facility.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert facility", e);
        }
    }

    @Override
    public void update(Facility facility) {
        String sql = "UPDATE facilities SET name=?, description=?, hourly_rate=?, status=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, facility.getName());
            ps.setString(2, facility.getDescription());
            ps.setDouble(3, facility.getHourlyRate());
            ps.setString(4, facility.getStatus().name());
            ps.setLong(5, facility.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update facility", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM facilities WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete facility", e);
        }
    }

    @Override
    public Optional<Facility> findById(Long id) {
        String sql = "SELECT * FROM facilities WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find facility", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Facility> findByName(String name) {
        String sql = "SELECT * FROM facilities WHERE name=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find facility by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Facility> findAll() {
        String sql = "SELECT * FROM facilities ORDER BY name";
        List<Facility> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list facilities", e);
        }
        return list;
    }

    @Override
    public List<Facility> findByStatus(FacilityStatus status) {
        String sql = "SELECT * FROM facilities WHERE status=? ORDER BY name";
        List<Facility> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list facilities by status", e);
        }
        return list;
    }

    private Facility map(ResultSet rs) throws SQLException {
        Facility f = new Facility();
        f.setId(rs.getLong("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        f.setHourlyRate(rs.getDouble("hourly_rate"));
        f.setStatus(FacilityStatus.valueOf(rs.getString("status")));
        return f;
    }
}



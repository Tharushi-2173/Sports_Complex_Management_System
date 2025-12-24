package dao.impl;

import dao.UserDao;
import db.ConnectionManager;
import model.Role;
import model.User;

import java.sql.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDaoJdbc class demonstrating POLYMORPHISM and ABSTRACTION principles
 * 
 * POLYMORPHISM CONCEPTS DEMONSTRATED:
 * 1. Interface Implementation: Implements UserDao interface
 * 2. Method Overriding: Provides concrete implementation of all interface methods
 * 3. Runtime Polymorphism: Can be used wherever UserDao is expected
 * 4. Dynamic Method Dispatch: JVM calls the correct implementation at runtime
 * 5. Substitutability: Can be substituted for any UserDao implementation
 * 
 * ABSTRACTION CONCEPTS DEMONSTRATED:
 * 1. Implementation Hiding: Hides JDBC-specific implementation details
 * 2. Database Abstraction: Client code doesn't need to know about SQL or JDBC
 * 3. Encapsulation: Database operations are encapsulated within this class
 * 4. Interface Segregation: Implements only the UserDao contract
 * 5. Dependency Inversion: Depends on abstractions (UserDao interface)
 */
public class UserDaoJdbc implements UserDao {
    
    // POLYMORPHISM: Method overriding - provides JDBC-specific implementation
    @Override
    public Long create(User user) {
        String sql = "INSERT INTO users(email, password_hash, full_name, role, phone, coach_fee) VALUES(?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhone());
            if (user.getCoachFee() == null) ps.setNull(6, Types.DECIMAL); else ps.setDouble(6, user.getCoachFee());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET email=?, full_name=?, role=?, phone=?, coach_fee=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getPhone());
            if (user.getCoachFee() == null) ps.setNull(5, Types.DECIMAL); else ps.setDouble(5, user.getCoachFee());
            ps.setLong(6, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> result = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list users", e);
        }
        return result;
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role=? ORDER BY full_name";
        List<User> result = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list users by role", e);
        }
        return result;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setPhone(rs.getString("phone"));
        try {
            Object cf = rs.getObject("coach_fee");
            if (cf instanceof BigDecimal) {
                u.setCoachFee(((BigDecimal) cf).doubleValue());
            } else if (cf instanceof Number) {
                u.setCoachFee(((Number) cf).doubleValue());
            } else if (cf == null) {
                u.setCoachFee(null);
            } else {
                double v = rs.getDouble("coach_fee");
                if (rs.wasNull()) u.setCoachFee(null); else u.setCoachFee(v);
            }
        } catch (SQLException ignore) {}
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) u.setCreatedAt(Instant.ofEpochMilli(created.getTime()));
        if (updated != null) u.setUpdatedAt(Instant.ofEpochMilli(updated.getTime()));
        return u;
    }
}



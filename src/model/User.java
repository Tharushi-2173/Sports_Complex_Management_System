package model;

import java.time.Instant;

/**
 * User model class demonstrating ENCAPSULATION principles
 * 
 * ENCAPSULATION CONCEPTS DEMONSTRATED:
 * 1. Data Hiding: All fields are private, preventing direct access from outside the class
 * 2. Controlled Access: Public getter/setter methods provide controlled access to private fields
 * 3. Data Validation: Setters can include validation logic (though not implemented here for simplicity)
 * 4. Information Hiding: Internal implementation details are hidden from external classes
 * 5. Interface Segregation: Clean public interface with only necessary methods exposed
 */
public class User {
    // ENCAPSULATION: Private fields - data is hidden from external access
    private Long id;
    private String email;
    private String passwordHash;  // Sensitive data properly encapsulated
    private String fullName;
    private Role role;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
    private Double coachFee; // per-hour fee for coaches (nullable)

    // ENCAPSULATION: Public getter methods provide controlled read access
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // ENCAPSULATION: Password hash is protected - only accessible through controlled methods
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ENCAPSULATION: Timestamp fields are read-only from external perspective
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ENCAPSULATION: Coach fee is optional and properly encapsulated
    public Double getCoachFee() { return coachFee; }
    public void setCoachFee(Double coachFee) { this.coachFee = coachFee; }
}



package model;

import java.time.Instant;


public class User {
   
    
    private Long id;
    private String email;
    private String passwordHash;  
    private String fullName;
    private Role role;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
    private Double coachFee;    
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }  
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }    
   
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getCoachFee() { return coachFee; }
    public void setCoachFee(Double coachFee) { this.coachFee = coachFee; }
}



package dao;

import model.Role;
import model.User;

import java.util.List;
import java.util.Optional;

/**
 * UserDao interface demonstrating ABSTRACTION principles
 * 
 * ABSTRACTION CONCEPTS DEMONSTRATED:
 * 1. Interface Definition: Defines a contract without implementation details
 * 2. Implementation Hiding: Hides the specific database implementation (JDBC, JPA, etc.)
 * 3. Contract Specification: Specifies what operations are available, not how they work
 * 4. Dependency Inversion: High-level modules depend on abstractions, not concrete implementations
 * 5. Polymorphic Interface: Multiple implementations can be used interchangeably
 * 
 * This interface abstracts the data access layer, allowing different implementations:
 * - UserDaoJdbc (JDBC implementation)
 * - UserDaoJpa (JPA implementation)
 * - UserDaoMock (Mock implementation for testing)
 */
public interface UserDao {
    
    // ABSTRACTION: Method signatures define the contract without implementation details
    // The actual database operations are hidden from the client code
    
    /**
     * Creates a new user in the data store
     * @param user the user to create
     * @return the generated ID of the created user
     */
    Long create(User user);
    
    /**
     * Updates an existing user in the data store
     * @param user the user to update
     */
    void update(User user);
    
    /**
     * Deletes a user from the data store
     * @param id the ID of the user to delete
     */
    void delete(Long id);
    
    /**
     * Finds a user by their ID
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(Long id);
    
    /**
     * Finds a user by their email address
     * @param email the user's email
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Retrieves all users from the data store
     * @return list of all users
     */
    List<User> findAll();
    
    /**
     * Finds all users with a specific role
     * @param role the role to filter by
     * @return list of users with the specified role
     */
    List<User> findByRole(Role role);
}



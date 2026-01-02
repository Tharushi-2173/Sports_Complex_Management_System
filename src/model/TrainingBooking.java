
package model;

import java.time.Duration;

/**
 * TrainingBooking class demonstrating INHERITANCE and POLYMORPHISM principles
 * 
 * INHERITANCE CONCEPTS DEMONSTRATED:
 * 1. Child Class: Extends the abstract Booking class
 * 2. Code Reuse: Inherits all common fields and methods from parent class
 * 3. Method Overriding: Implements the abstract calculateCost() method
 * 4. IS-A Relationship: TrainingBooking IS-A Booking
 * 5. Single Inheritance: Java supports single inheritance (extends one class)
 * 
 * POLYMORPHISM CONCEPTS DEMONSTRATED:
 * 1. Method Overriding: Provides different implementation of calculateCost() than FacilityBooking
 * 2. Runtime Polymorphism: Can be treated as Booking at runtime
 * 3. Dynamic Method Dispatch: JVM calls the correct method based on actual object type
 * 4. Polymorphic Behavior: Same interface, different implementation
 * 5. Substitutability: Can be used wherever Booking is expected
 */
public class TrainingBooking extends Booking {
    
    // POLYMORPHISM: Method overriding - provides different implementation for training bookings
    // This demonstrates how the same method signature can have different behaviors
    @Override
    public double calculateCost(double hourlyRate) {
        // INHERITANCE: Uses inherited methods getStartTime() and getEndTime()
        Duration d = Duration.between(getStartTime(), getEndTime());
        double hours = d.toMinutes() / 60.0;
        if (hours < 0) hours = 0;
        // POLYMORPHISM: Different calculation logic for training bookings (includes coaching multiplier)
        double coachingFeeMultiplier = 1.25; // 25% premium for training sessions
        return Math.round(hours * hourlyRate * coachingFeeMultiplier * 100.0) / 100.0;
    }
}



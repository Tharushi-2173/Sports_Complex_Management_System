package model;

import java.time.Duration;

/**
 * FacilityBooking class demonstrating INHERITANCE and POLYMORPHISM principles
 * 
 * INHERITANCE CONCEPTS DEMONSTRATED:
 * 1. Child Class: Extends the abstract Booking class
 * 2. Code Reuse: Inherits all common fields and methods from parent class
 * 3. Method Overriding: Implements the abstract calculateCost() method
 * 4. IS-A Relationship: FacilityBooking IS-A Booking
 * 5. Single Inheritance: Java supports single inheritance (extends one class)
 * 
 * POLYMORPHISM CONCEPTS DEMONSTRATED:
 * 1. Method Overriding: Provides specific implementation of calculateCost()
 * 2. Runtime Polymorphism: Can be treated as Booking at runtime
 * 3. Dynamic Method Dispatch: JVM calls the correct method based on actual object type
 * 4. Polymorphic Behavior: Same interface, different implementation
 * 5. Substitutability: Can be used wherever Booking is expected
 */
public class FacilityBooking extends Booking {
    
    // POLYMORPHISM: Method overriding - provides specific implementation for facility bookings
    // This method will be called when calculateCost() is invoked on a FacilityBooking instance
    @Override
    public double calculateCost(double hourlyRate) {
        // INHERITANCE: Uses inherited methods getStartTime() and getEndTime()
        Duration d = Duration.between(getStartTime(), getEndTime());
        double hours = d.toMinutes() / 60.0;
        if (hours < 0) hours = 0;
        // POLYMORPHISM: Specific calculation logic for facility bookings
        return Math.round(hours * hourlyRate * 100.0) / 100.0;
    }
}




package model;

import java.time.Duration;


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



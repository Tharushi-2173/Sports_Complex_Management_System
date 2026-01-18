package model;

import java.time.Duration;

public class FacilityBooking extends Booking {
   
    @Override
    public double calculateCost(double hourlyRate) {
       
        Duration d = Duration.between(getStartTime(), getEndTime());
        double hours = d.toMinutes() / 60.0;
        if (hours < 0) 
            hours = 0;
        
       
        return Math.round(hours * hourlyRate * 100.0) / 100.0;
    }
}



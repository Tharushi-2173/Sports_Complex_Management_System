package dao;

import model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Long create(Payment payment);
    Optional<Payment> findById(Long id);
    List<Payment> findByBooking(Long bookingId);
    List<Payment> findAll();
}



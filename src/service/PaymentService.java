package service;

import model.Payment;

import java.util.List;

public interface PaymentService {
    Long record(Payment payment);
    List<Payment> listAll();
    List<Payment> listByBooking(Long bookingId);
}



package service.impl;

import dao.PaymentDao;
import model.Payment;
import service.PaymentService;

import java.util.List;

public class PaymentServiceImpl implements PaymentService {
    private final PaymentDao paymentDao;

    public PaymentServiceImpl(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Override
    public Long record(Payment payment) {
        if (payment.getBookingId() == null) throw new IllegalArgumentException("Booking id required");
        if (payment.getAmount() < 0) throw new IllegalArgumentException("Amount must be >= 0");
        return paymentDao.create(payment);
    }

    @Override
    public List<Payment> listAll() { return paymentDao.findAll(); }

    @Override
    public List<Payment> listByBooking(Long bookingId) { return paymentDao.findByBooking(bookingId); }
}



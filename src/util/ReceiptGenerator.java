package util;

import model.Payment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ReceiptGenerator {
    public static File generateTextReceipt(Payment payment) throws IOException {
        String fileName = "receipt-" + UUID.randomUUID() + ".txt";
        File f = new File(fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            bw.write("=== Sports Complex Receipt ===\n");
            bw.write("Payment ID: " + payment.getId() + "\n");
            bw.write("Booking ID: " + payment.getBookingId() + "\n");
            bw.write("Amount: " + String.format("%.2f", payment.getAmount()) + "\n");
            bw.write("Discount: " + String.format("%.2f", payment.getDiscount()) + "\n");
            bw.write("Method: " + payment.getMethod() + "\n");
            bw.write("Reference: " + (payment.getReference() == null ? "-" : payment.getReference()) + "\n");
            if (payment.getPaidAt() != null) {
                bw.write("Paid At: " + payment.getPaidAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n");
            }
            bw.write("==============================\n");
        }
        return f;
    }
}



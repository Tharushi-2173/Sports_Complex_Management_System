package service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Long> mostUsedFacilities(LocalDate from, LocalDate to);
    Map<String, Long> busiestHours(LocalDate from, LocalDate to);
    Map<String, Double> incomeByMonth(int year);
    List<String[]> rawPayments(LocalDate from, LocalDate to);
}



package ui.screens;

import ui.AppColors;
import util.CsvExporter;
import dao.impl.BookingDaoJdbc;
import dao.impl.FacilityDaoJdbc;
import dao.impl.PaymentDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.Payment;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {
    // DAO instances for comprehensive reports
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final BookingDaoJdbc bookingDao = new BookingDaoJdbc();
    private final PaymentDaoJdbc paymentDao = new PaymentDaoJdbc();
    private final FacilityDaoJdbc facilityDao = new FacilityDaoJdbc();
    
    // Report display area
    private JTextArea reportArea;

    public ReportsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel - Clean title only
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel title = new JLabel("📊 Reports");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Generate and export comprehensive business reports");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);

        // Report generation controls section - single row with all controls
        JPanel controlsSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controlsSection.setBackground(AppColors.CARD);
        controlsSection.setBorder(new EmptyBorder(8, 20, 8, 20));
        
        // Report type selection
        JLabel typeLabel = new JLabel("📊 Report Type:");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 13f));
        typeLabel.setForeground(AppColors.TEXT_PRIMARY);
        controlsSection.add(typeLabel);
        
        JComboBox<String> reportTypeCombo = new JComboBox<>(new String[]{
            "📈 Comprehensive Dashboard Report",
            "👥 Members Report",
            "📅 Bookings Report", 
            "💰 Payments Report",
            "🏢 Facilities Report",
            "🔧 Maintenance Report",
            "💬 Feedback Report"
        });
        reportTypeCombo.setFont(reportTypeCombo.getFont().deriveFont(Font.PLAIN, 12f));
        reportTypeCombo.setPreferredSize(new Dimension(250, 30));
        reportTypeCombo.setBackground(AppColors.BG_DARK);
        reportTypeCombo.setForeground(AppColors.TEXT_PRIMARY);
        reportTypeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        controlsSection.add(reportTypeCombo);
        
        // Date Range Picker Label
        JLabel dateLabel = new JLabel("📅 Date Range:");
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 13f));
        dateLabel.setForeground(AppColors.TEXT_PRIMARY);
        controlsSection.add(dateLabel);
        
        // Full DateRangePicker component
        ui.components.DateRangePicker dateRangePicker = new ui.components.DateRangePicker();
        dateRangePicker.setPreferredSize(new Dimension(400, 180));
        dateRangePicker.setMaximumSize(new Dimension(400, 180));
        controlsSection.add(dateRangePicker);
        
        // Format selection
        JLabel formatLabel = new JLabel("📄 Format:");
        formatLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD, 13f));
        formatLabel.setForeground(AppColors.TEXT_PRIMARY);
        controlsSection.add(formatLabel);
        
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"CSV", "TXT"});
        formatCombo.setFont(formatCombo.getFont().deriveFont(Font.PLAIN, 12f));
        formatCombo.setPreferredSize(new Dimension(80, 30));
        formatCombo.setBackground(AppColors.BG_DARK);
        formatCombo.setForeground(AppColors.TEXT_PRIMARY);
        formatCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        controlsSection.add(formatCombo);
        
        // Generate Report button
        JButton generateButton = createStyledButton("📊 Generate Report", AppColors.SUCCESS);
        generateButton.setPreferredSize(new Dimension(150, 35));
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD, 13f));
        
        generateButton.addActionListener(e -> {
            String reportType = (String) reportTypeCombo.getSelectedItem();
            String format = (String) formatCombo.getSelectedItem();
            
            // Validate date range before proceeding
            if (!dateRangePicker.isValidRange()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid date range before generating the report.", 
                    "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String fromDateStr = dateRangePicker.getFromDateString();
            String toDateStr = dateRangePicker.getToDateString();
            
            try {
                generateReport(reportType, format, fromDateStr, toDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error generating report: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        controlsSection.add(generateButton);
        
        add(controlsSection, BorderLayout.CENTER);

        // Content Panel for displaying results
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AppColors.CARD);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Report display area
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(reportArea.getFont().deriveFont(12f));
        reportArea.setBackground(AppColors.BG_DARK);
        reportArea.setForeground(AppColors.TEXT_PRIMARY);
        reportArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        reportArea.setText("Welcome to the Reports Panel!\n\n" +
                   "📊 Use the controls above to generate comprehensive reports:\n" +
                   "• Select a report type from the dropdown\n" +
                   "• Choose your date range using the date picker\n" +
                   "• Select export format (CSV or TXT)\n" +
                   "• Click 'Generate Report' to create detailed reports\n\n" +
                   "Generated reports will be displayed below and CSV files are saved automatically.\n" +
                   "A sample report will be generated automatically on startup.");
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(null);
        scrollPane.setBackground(AppColors.CARD);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.SOUTH);
        
        // Store reference to report area for updating
        this.reportArea = reportArea;
        
        // Auto-generate a sample report on startup
        SwingUtilities.invokeLater(() -> {
            try {
                String reportContent = generateReportContent("📈 Comprehensive Dashboard Report", "CSV", 
                    java.time.LocalDateTime.now().minusMonths(1).toLocalDate().toString(),
                    java.time.LocalDateTime.now().toLocalDate().toString());
                reportArea.setText(reportContent);
                reportArea.setCaretPosition(0);
            } catch (Exception e) {
                // If auto-generation fails, keep the welcome message
            }
        });
    }
    
    private void generateReport(String reportType, String format, String fromDate, String toDate) throws Exception {
        try {
            // Generate report content for display
            String reportContent = generateReportContent(reportType, format, fromDate, toDate);
            
            // Generate CSV file
            String fileName = generateReportData(reportType, format, fromDate, toDate);
            
            // Display report content in the panel immediately
            reportArea.setText(reportContent);
            reportArea.setCaretPosition(0); // Scroll to top
            
            // Show success message with file location
            JOptionPane.showMessageDialog(this, 
                "Report generated successfully!\n\nReport content is displayed below.\nCSV file saved as: " + fileName, 
                "Report Complete", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateReportContent(String reportType, String format, String fromDate, String toDate) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Add report header
        content.append("=".repeat(80)).append("\n");
        content.append("                    SPORTS COMPLEX MANAGEMENT SYSTEM\n");
        content.append("                           REPORT GENERATION\n");
        content.append("=".repeat(80)).append("\n\n");
        
        content.append("Report Type: ").append(reportType).append("\n");
        content.append("Date Range: ").append(fromDate).append(" to ").append(toDate).append("\n");
        content.append("Format: ").append(format).append("\n");
        content.append("Generated: ").append(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        content.append("-".repeat(80)).append("\n");
        
        // Generate specific report content
        switch (reportType) {
            case "📈 Comprehensive Dashboard Report":
                generateComprehensiveReportContent(content, fromDate, toDate);
                break;
            case "👥 Members Report":
                generateMembersReportContent(content, fromDate, toDate);
                break;
            case "📅 Bookings Report":
                generateBookingsReportContent(content, fromDate, toDate);
                break;
            case "💰 Payments Report":
                generatePaymentsReportContent(content, fromDate, toDate);
                break;
            case "🏢 Facilities Report":
                generateFacilitiesReportContent(content, fromDate, toDate);
                break;
            case "🔧 Maintenance Report":
                generateMaintenanceReportContent(content, fromDate, toDate);
                break;
            case "💬 Feedback Report":
                generateFeedbackReportContent(content, fromDate, toDate);
                break;
            default:
                content.append("Unknown report type selected.\n");
        }
        
        content.append("\n").append("=".repeat(80)).append("\n");
        content.append("End of Report\n");
        
        return content.toString();
    }
    
    private String generateReportData(String reportType, String format, String fromDate, String toDate) throws Exception {
        String fileName = "report_" + System.currentTimeMillis() + "." + format.toLowerCase();
        java.io.File file = new java.io.File(fileName);
        
        switch (reportType) {
            case "📈 Comprehensive Dashboard Report":
                generateComprehensiveReportData(file, format);
                break;
            case "👥 Members Report":
                generateMembersReportData(file, format);
                break;
            case "📅 Bookings Report":
                generateBookingsReportData(file, format, fromDate, toDate);
                break;
            case "💰 Payments Report":
                generatePaymentsReportData(file, format, fromDate, toDate);
                break;
            case "🏢 Facilities Report":
                generateFacilitiesReportData(file, format);
                break;
            case "🔧 Maintenance Report":
                generateMaintenanceReportData(file, format);
                break;
            case "💬 Feedback Report":
                generateFeedbackReportData(file, format);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
        
        return fileName;
    }
    
    private void generateComprehensiveReportData(java.io.File file, String format) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "Report Type", "Count", "Details"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        // Add statistics
        List<User> allUsers = userDao.findAll();
        List<Object> allBookings = bookingDao.findAll();
        List<Payment> allPayments = paymentDao.findAll();
        List<model.Facility> allFacilities = facilityDao.findAll();
        
        rows.add(java.util.Arrays.asList("Total Members", String.valueOf(allUsers.size()), "All registered users"));
        rows.add(java.util.Arrays.asList("Total Bookings", String.valueOf(allBookings.size()), "All booking records"));
        rows.add(java.util.Arrays.asList("Total Payments", String.valueOf(allPayments.size()), "All payment records"));
        rows.add(java.util.Arrays.asList("Total Facilities", String.valueOf(allFacilities.size()), "All facility records"));
        
        // Add revenue summary
        double totalRevenue = allPayments.stream()
            .filter(p -> p.getPaidAt() != null)
            .mapToDouble(Payment::getAmount)
            .sum();
        rows.add(java.util.Arrays.asList("Total Revenue", String.format("$%.2f", totalRevenue), "All paid amounts"));
        
        // Add pending items
        long pendingPayments = allPayments.stream()
            .filter(p -> p.getPaidAt() == null)
            .count();
        rows.add(java.util.Arrays.asList("Pending Payments", String.valueOf(pendingPayments), "Unpaid amounts"));
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generateMembersReportData(java.io.File file, String format) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "Email", "Full Name", "Role", "Phone", "Coach Fee"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        List<User> allUsers = userDao.findAll();
        for (User user : allUsers) {
            rows.add(java.util.Arrays.asList(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getPhone() != null ? user.getPhone() : "",
                user.getCoachFee() != null ? String.format("%.2f", user.getCoachFee()) : ""
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generateBookingsReportData(java.io.File file, String format, String fromDate, String toDate) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "Type", "Member", "Facility", "Coach", "Start Time", "End Time", "Status", "Total Fee"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        List<Object> allBookings = bookingDao.findAll();
        for (Object booking : allBookings) {
            String memberName = "";
            String facilityName = "";
            String coachName = "";
            String startTime = "";
            String endTime = "";
            String status = "";
            String totalFee = "";
            String type = "";
            
            if (booking instanceof model.FacilityBooking) {
                model.FacilityBooking fb = (model.FacilityBooking) booking;
                type = "FACILITY";
                memberName = userDao.findById(fb.getMemberId()).map(User::getFullName).orElse("Unknown");
                facilityName = facilityDao.findById(fb.getFacilityId()).map(model.Facility::getName).orElse("Unknown");
                coachName = fb.getCoachId() != null ? userDao.findById(fb.getCoachId()).map(User::getFullName).orElse("") : "";
                startTime = fb.getStartTime().toString();
                endTime = fb.getEndTime().toString();
                status = fb.getStatus().name();
                totalFee = String.format("%.2f", fb.getTotalFee());
            } else if (booking instanceof model.TrainingBooking) {
                model.TrainingBooking tb = (model.TrainingBooking) booking;
                type = "TRAINING";
                memberName = userDao.findById(tb.getMemberId()).map(User::getFullName).orElse("Unknown");
                facilityName = facilityDao.findById(tb.getFacilityId()).map(model.Facility::getName).orElse("Unknown");
                coachName = tb.getCoachId() != null ? userDao.findById(tb.getCoachId()).map(User::getFullName).orElse("") : "";
                startTime = tb.getStartTime().toString();
                endTime = tb.getEndTime().toString();
                status = tb.getStatus().name();
                totalFee = String.format("%.2f", tb.getTotalFee());
            }
            
            rows.add(java.util.Arrays.asList(
                String.valueOf(getBookingId(booking)),
                type,
                memberName,
                facilityName,
                coachName,
                startTime,
                endTime,
                status,
                totalFee
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generatePaymentsReportData(java.io.File file, String format, String fromDate, String toDate) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "User", "Booking", "Amount", "Discount", "Paid At", "Method", "Reference"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        List<Payment> allPayments = paymentDao.findAll();
        for (Payment payment : allPayments) {
            String userName = userDao.findById(payment.getUserId()).map(User::getFullName).orElse("Unknown");
            String paidAt = payment.getPaidAt() != null ? payment.getPaidAt().toString() : "Not Paid";
            
            rows.add(java.util.Arrays.asList(
                String.valueOf(payment.getId()),
                userName,
                String.valueOf(payment.getBookingId()),
                String.format("%.2f", payment.getAmount()),
                String.format("%.2f", payment.getDiscount()),
                paidAt,
                payment.getMethod().name(),
                payment.getReference() != null ? payment.getReference() : ""
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generateFacilitiesReportData(java.io.File file, String format) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "Name", "Hourly Rate", "Status"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        List<model.Facility> allFacilities = facilityDao.findAll();
        for (model.Facility facility : allFacilities) {
            rows.add(java.util.Arrays.asList(
                String.valueOf(facility.getId()),
                facility.getName(),
                String.format("%.2f", facility.getHourlyRate()),
                facility.getStatus().name()
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generateMaintenanceReportData(java.io.File file, String format) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "Facility", "Requested By", "Title", "Status", "Created At"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        dao.impl.MaintenanceDaoJdbc maintenanceDao = new dao.impl.MaintenanceDaoJdbc();
        List<model.MaintenanceRequest> allRequests = maintenanceDao.findAll();
        for (model.MaintenanceRequest request : allRequests) {
            String facilityName = facilityDao.findById(request.getFacilityId()).map(model.Facility::getName).orElse("Unknown");
            String requestedByName = userDao.findById(request.getRequestedBy()).map(User::getFullName).orElse("Unknown");
            String createdAt = request.getCreatedAt() != null ? request.getCreatedAt().toString() : "Unknown";
            
            rows.add(java.util.Arrays.asList(
                String.valueOf(request.getId()),
                facilityName,
                requestedByName,
                request.getTitle(),
                request.getStatus().name(),
                createdAt
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private void generateFeedbackReportData(java.io.File file, String format) throws Exception {
        java.util.List<String> header = java.util.Arrays.asList(
            "ID", "User", "Facility", "Rating", "Comments", "Created At"
        );
        
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        
        dao.impl.FeedbackDaoJdbc feedbackDao = new dao.impl.FeedbackDaoJdbc();
        List<model.Feedback> allFeedback = feedbackDao.findAll();
        for (model.Feedback feedback : allFeedback) {
            String userName = userDao.findById(feedback.getUserId()).map(User::getFullName).orElse("Unknown");
            String facilityName = feedback.getFacilityId() != null ? 
                facilityDao.findById(feedback.getFacilityId()).map(model.Facility::getName).orElse("Unknown") : "General";
            String createdAt = feedback.getCreatedAt() != null ? feedback.getCreatedAt().toString() : "Unknown";
            
            rows.add(java.util.Arrays.asList(
                String.valueOf(feedback.getId()),
                userName,
                facilityName,
                String.valueOf(feedback.getRating()),
                feedback.getComments() != null ? feedback.getComments() : "",
                createdAt
            ));
        }
        
        CsvExporter.export(file, header, rows);
    }
    
    private Long getBookingId(Object booking) {
        if (booking instanceof model.FacilityBooking) {
            return ((model.FacilityBooking) booking).getId();
        } else if (booking instanceof model.TrainingBooking) {
            return ((model.TrainingBooking) booking).getId();
        }
        return null;
    }
    
    // Content generation methods for display
    private void generateComprehensiveReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("COMPREHENSIVE DASHBOARD REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        // Get statistics
        List<User> allUsers = userDao.findAll();
        List<Object> allBookings = bookingDao.findAll();
        List<model.Payment> allPayments = paymentDao.findAll();
        List<model.Facility> allFacilities = facilityDao.findAll();
        
        content.append("SUMMARY STATISTICS:\n");
        content.append("• Total Members: ").append(allUsers.size()).append("\n");
        content.append("• Total Bookings: ").append(allBookings.size()).append("\n");
        content.append("• Total Payments: ").append(allPayments.size()).append("\n");
        content.append("• Total Facilities: ").append(allFacilities.size()).append("\n\n");
        
        content.append("RECENT ACTIVITY:\n");
        content.append("• Latest Bookings: ").append(Math.min(5, allBookings.size())).append(" recent entries\n");
        content.append("• Recent Payments: ").append(Math.min(5, allPayments.size())).append(" recent transactions\n");
    }
    
    private void generateMembersReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("MEMBERS REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        List<User> allUsers = userDao.findAll();
        content.append("Total Members: ").append(allUsers.size()).append("\n\n");
        
        content.append("MEMBER DETAILS:\n");
        content.append(String.format("%-5s %-20s %-30s %-15s %-10s\n", "ID", "Name", "Email", "Phone", "Role"));
        content.append("-".repeat(80)).append("\n");
        
        for (User user : allUsers) {
            content.append(String.format("%-5d %-20s %-30s %-15s %-10s\n", 
                user.getId(), 
                user.getFullName(), 
                user.getEmail(), 
                user.getPhone() != null ? user.getPhone() : "N/A",
                user.getRole().name()));
        }
    }
    
    private void generateBookingsReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("BOOKINGS REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        List<Object> allBookings = bookingDao.findAll();
        content.append("Total Bookings: ").append(allBookings.size()).append("\n\n");
        
        content.append("BOOKING DETAILS:\n");
        content.append(String.format("%-5s %-15s %-20s %-15s %-10s\n", "ID", "Member", "Facility", "Date", "Status"));
        content.append("-".repeat(70)).append("\n");
        
        for (Object booking : allBookings) {
            String memberName = userDao.findById(((model.Booking) booking).getMemberId()).map(User::getFullName).orElse("Unknown");
            String facilityName = "Unknown";
            if (booking instanceof model.FacilityBooking) {
                facilityName = facilityDao.findById(((model.FacilityBooking) booking).getFacilityId()).map(model.Facility::getName).orElse("Unknown");
            } else if (booking instanceof model.TrainingBooking) {
                facilityName = facilityDao.findById(((model.TrainingBooking) booking).getFacilityId()).map(model.Facility::getName).orElse("Unknown");
            }
            String bookingDate = ((model.Booking) booking).getStartTime() != null ? ((model.Booking) booking).getStartTime().toLocalDate().toString() : "N/A";
            
            content.append(String.format("%-5d %-15s %-20s %-15s %-10s\n", 
                getBookingId(booking), 
                memberName, 
                facilityName, 
                bookingDate,
                ((model.Booking) booking).getStatus().name()));
        }
    }
    
    private void generatePaymentsReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("PAYMENTS REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        List<model.Payment> allPayments = paymentDao.findAll();
        content.append("Total Payments: ").append(allPayments.size()).append("\n\n");
        
        content.append("PAYMENT DETAILS:\n");
        content.append(String.format("%-5s %-20s %-15s %-10s %-15s\n", "ID", "Member", "Amount", "Method", "Status"));
        content.append("-".repeat(70)).append("\n");
        
        for (model.Payment payment : allPayments) {
            String memberName = userDao.findById(payment.getUserId()).map(User::getFullName).orElse("Unknown");
            content.append(String.format("%-5d %-20s %-15.2f %-10s %-15s\n", 
                payment.getId(), 
                memberName, 
                payment.getAmount(), 
                payment.getMethod().name(),
                payment.getPaidAt() != null ? "PAID" : "PENDING"));
        }
    }
    
    private void generateFacilitiesReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("FACILITIES REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        List<model.Facility> allFacilities = facilityDao.findAll();
        content.append("Total Facilities: ").append(allFacilities.size()).append("\n\n");
        
        content.append("FACILITY DETAILS:\n");
        content.append(String.format("%-5s %-25s %-15s %-10s %-15s\n", "ID", "Name", "Status", "Rate/Hour", "Status"));
        content.append("-".repeat(75)).append("\n");
        
        for (model.Facility facility : allFacilities) {
            content.append(String.format("%-5d %-25s %-15s %-10.2f %-15s\n", 
                facility.getId(), 
                facility.getName(), 
                facility.getStatus().name(), 
                facility.getHourlyRate(),
                facility.getStatus().name()));
        }
    }
    
    private void generateMaintenanceReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("MAINTENANCE REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        dao.impl.MaintenanceDaoJdbc maintenanceDao = new dao.impl.MaintenanceDaoJdbc();
        List<model.MaintenanceRequest> allRequests = maintenanceDao.findAll();
        content.append("Total Maintenance Requests: ").append(allRequests.size()).append("\n\n");
        
        content.append("MAINTENANCE DETAILS:\n");
        content.append(String.format("%-5s %-20s %-20s %-25s %-10s\n", "ID", "Facility", "Requested By", "Title", "Status"));
        content.append("-".repeat(85)).append("\n");
        
        for (model.MaintenanceRequest request : allRequests) {
            String facilityName = facilityDao.findById(request.getFacilityId()).map(model.Facility::getName).orElse("Unknown");
            String requestedByName = userDao.findById(request.getRequestedBy()).map(User::getFullName).orElse("Unknown");
            
            content.append(String.format("%-5d %-20s %-20s %-25s %-10s\n", 
                request.getId(), 
                facilityName, 
                requestedByName, 
                request.getTitle(),
                request.getStatus().name()));
        }
    }
    
    private void generateFeedbackReportContent(StringBuilder content, String fromDate, String toDate) {
        content.append("FEEDBACK REPORT\n");
        content.append("-".repeat(40)).append("\n\n");
        
        dao.impl.FeedbackDaoJdbc feedbackDao = new dao.impl.FeedbackDaoJdbc();
        List<model.Feedback> allFeedback = feedbackDao.findAll();
        content.append("Total Feedback Entries: ").append(allFeedback.size()).append("\n\n");
        
        content.append("FEEDBACK DETAILS:\n");
        content.append(String.format("%-5s %-20s %-20s %-5s %-30s\n", "ID", "User", "Facility", "Rating", "Comments"));
        content.append("-".repeat(85)).append("\n");
        
        for (model.Feedback feedback : allFeedback) {
            String userName = userDao.findById(feedback.getUserId()).map(User::getFullName).orElse("Unknown");
            String facilityName = facilityDao.findById(feedback.getFacilityId()).map(model.Facility::getName).orElse("Unknown");
            String comments = feedback.getComments() != null ? feedback.getComments() : "No comments";
            if (comments.length() > 30) {
                comments = comments.substring(0, 27) + "...";
            }
            
            content.append(String.format("%-5d %-20s %-20s %-5d %-30s\n", 
                feedback.getId(), 
                userName, 
                facilityName, 
                feedback.getRating(),
                comments));
        }
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
}



package ui.screens;

import dao.impl.BookingDaoJdbc;
import dao.impl.FacilityDaoJdbc;
import dao.impl.PaymentDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.BookingStatus;
import model.Payment;
import model.User;
import ui.AppColors;
import ui.components.NotificationPanel;
import ui.components.StatisticsPanel;
import util.CsvExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * DashboardPanel class demonstrating POLYMORPHISM and ENCAPSULATION principles
 * 
 * POLYMORPHISM CONCEPTS DEMONSTRATED:
 * 1. Component Polymorphism: Uses JPanel, JLabel, JButton - all are Components
 * 2. Layout Manager Polymorphism: Uses BorderLayout, FlowLayout - all implement LayoutManager
 * 3. Event Handler Polymorphism: ActionListener implementations for different buttons
 * 4. Method Overriding: Overrides JPanel methods and implements interfaces
 * 5. Runtime Polymorphism: Components behave differently based on their actual type
 * 
 * ENCAPSULATION CONCEPTS DEMONSTRATED:
 * 1. Data Hiding: Private fields for panels and DAO instances
 * 2. Controlled Access: Public methods for external interaction, private for internal logic
 * 3. Component Encapsulation: UI components are encapsulated within this panel
 * 4. State Management: Dashboard state is managed internally
 * 5. Implementation Hiding: Complex UI logic is hidden from external classes
 */
public class DashboardPanel extends JPanel {
    
    // ENCAPSULATION: Private fields - UI components are hidden from external access
    private StatisticsPanel statisticsPanel;
    private NotificationPanel notificationPanel;
    
    // ENCAPSULATION: DAO instances are private and managed internally
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final BookingDaoJdbc bookingDao = new BookingDaoJdbc();
    private final PaymentDaoJdbc paymentDao = new PaymentDaoJdbc();
    private final FacilityDaoJdbc facilityDao = new FacilityDaoJdbc();
    
    // ENCAPSULATION: Constructor encapsulates initialization logic
    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
        setupLayout();
        loadSampleData();
    }
    
    // ENCAPSULATION: Private method - component initialization is hidden
    private void initializeComponents() {
        statisticsPanel = new StatisticsPanel();
        notificationPanel = new NotificationPanel();
    }
    
    // ENCAPSULATION: Private method - layout setup is hidden from external access
    private void setupLayout() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("🏠 Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Overview of your sports complex management system");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(AppColors.BG);
        
        // Statistics panel (left side)
        contentPanel.add(statisticsPanel, BorderLayout.CENTER);
        
        // Notifications panel (right side)
        JPanel notificationContainer = new JPanel(new BorderLayout());
        notificationContainer.setBackground(AppColors.BG);
        notificationContainer.setPreferredSize(new Dimension(400, 0));
        notificationContainer.add(notificationPanel, BorderLayout.CENTER);
        
        contentPanel.add(notificationContainer, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel quickActionsPanel = createQuickActionsPanel();
        add(quickActionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionsPanel.setBackground(AppColors.CARD);
        actionsPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton refreshStats = createStyledButton("🔄 Refresh Statistics", AppColors.ACCENT);
        JButton viewAllNotifications = createStyledButton("🔔 View All Notifications", AppColors.WARNING);
        
        refreshStats.addActionListener(e -> refreshStatistics());
        viewAllNotifications.addActionListener(e -> showAllNotifications());
        
        actionsPanel.add(refreshStats);
        actionsPanel.add(viewAllNotifications);
        
        return actionsPanel;
    }
    
    private void loadSampleData() {
        // Load sample statistics
        updateStatistics();
        
        // Load sample notifications
        loadSampleNotifications();
    }
    
    private void updateStatistics() {
        try {
            // Get real data from database
            List<User> allUsers = userDao.findAll();
            List<Object> allBookings = bookingDao.findAll();
            List<Payment> allPayments = paymentDao.findAll();
            List<model.Facility> allFacilities = facilityDao.findAll();
            
            // Calculate statistics
            long totalMembers = allUsers.stream()
                .filter(user -> user.getRole() == model.Role.MEMBER)
                .count();
            
            long activeBookings = allBookings.stream()
                .filter(booking -> {
                    if (booking instanceof model.FacilityBooking) {
                        return ((model.FacilityBooking) booking).getStatus() == BookingStatus.CONFIRMED;
                    } else if (booking instanceof model.TrainingBooking) {
                        return ((model.TrainingBooking) booking).getStatus() == BookingStatus.CONFIRMED;
                    }
                    return false;
                })
                .count();
            
            // Calculate monthly revenue (current month)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            double monthlyRevenue = allPayments.stream()
                .filter(payment -> payment.getPaidAt().isAfter(startOfMonth))
                .mapToDouble(Payment::getAmount)
                .sum();
            
            long totalFacilities = allFacilities.size();
            
            long pendingPayments = allPayments.stream()
                .filter(payment -> payment.getAmount() > 0) // Assuming non-zero amount means pending
                .count();
            
            // Calculate utilization rate (simplified)
            double utilizationRate = allFacilities.isEmpty() ? 0.0 : 
                (double) activeBookings / (allFacilities.size() * 30) * 100; // Assuming 30 slots per facility per month
            
            // Update statistics panel
            statisticsPanel.updateStatCard("Total Members", String.valueOf(totalMembers));
            statisticsPanel.updateStatCard("Active Bookings", String.valueOf(activeBookings));
            statisticsPanel.updateStatCard("Monthly Revenue", String.format("$%.2f", monthlyRevenue));
            statisticsPanel.updateStatCard("Total Facilities", String.valueOf(totalFacilities));
            statisticsPanel.updateStatCard("Pending Payments", String.valueOf(pendingPayments));
            statisticsPanel.updateStatCard("Utilization Rate", String.format("%.1f%%", utilizationRate));
            
            // Update charts with real data
            updateChartsWithRealData();
            
        } catch (Exception e) {
            // Fallback to sample data if database error
            statisticsPanel.updateStatCard("Total Members", "0");
            statisticsPanel.updateStatCard("Active Bookings", "0");
            statisticsPanel.updateStatCard("Monthly Revenue", "$0.00");
            statisticsPanel.updateStatCard("Total Facilities", "0");
            statisticsPanel.updateStatCard("Pending Payments", "0");
            statisticsPanel.updateStatCard("Utilization Rate", "0%");
        }
    }
    
    private void loadSampleNotifications() {
        try {
            // Load real notifications from database
            List<Object> allBookings = bookingDao.findAll();
            List<Payment> allPayments = paymentDao.findAll();
            List<User> allUsers = userDao.findAll();
            
            // Add upcoming booking notifications (next 24 hours)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            
            for (Object booking : allBookings) {
                LocalDateTime bookingTime = null;
                String memberName = "";
                String facilityName = "";
                
                if (booking instanceof model.FacilityBooking) {
                    model.FacilityBooking fb = (model.FacilityBooking) booking;
                    if (fb.getStatus() == BookingStatus.CONFIRMED) {
                        bookingTime = fb.getStartTime();
                        memberName = allUsers.stream()
                            .filter(user -> user.getId().equals(fb.getMemberId()))
                            .map(User::getFullName)
                            .findFirst().orElse("Unknown Member");
                        facilityName = facilityDao.findById(fb.getFacilityId())
                            .map(model.Facility::getName)
                            .orElse("Unknown Facility");
                    }
                } else if (booking instanceof model.TrainingBooking) {
                    model.TrainingBooking tb = (model.TrainingBooking) booking;
                    if (tb.getStatus() == BookingStatus.CONFIRMED) {
                        bookingTime = tb.getStartTime();
                        memberName = allUsers.stream()
                            .filter(user -> user.getId().equals(tb.getMemberId()))
                            .map(User::getFullName)
                            .findFirst().orElse("Unknown Member");
                        facilityName = facilityDao.findById(tb.getFacilityId())
                            .map(model.Facility::getName)
                            .orElse("Unknown Facility");
                    }
                }
                
                if (bookingTime != null && bookingTime.isAfter(now) && bookingTime.isBefore(tomorrow)) {
                    notificationPanel.addUpcomingBookingNotification(memberName, facilityName, bookingTime);
                }
            }
            
            // Add pending payment notifications
            for (Payment payment : allPayments) {
                if (payment.getPaidAt() == null) { // Check if payment is actually pending
                    String userName = allUsers.stream()
                        .filter(user -> user.getId().equals(payment.getUserId()))
                        .map(User::getFullName)
                        .findFirst().orElse("Unknown User");
                    notificationPanel.addPendingPaymentNotification(userName, payment.getAmount());
                }
            }
            
            // Add maintenance notifications (pending requests)
            dao.impl.MaintenanceDaoJdbc maintenanceDao = new dao.impl.MaintenanceDaoJdbc();
            List<model.MaintenanceRequest> allMaintenanceRequests = maintenanceDao.findAll();
            for (model.MaintenanceRequest request : allMaintenanceRequests) {
                if (request.getStatus() == model.MaintenanceStatus.OPEN) {
                    String facilityName = facilityDao.findById(request.getFacilityId())
                        .map(model.Facility::getName)
                        .orElse("Unknown Facility");
                    notificationPanel.addMaintenanceNotification(facilityName, request.getTitle());
                }
            }
            
        } catch (Exception e) {
            // Fallback to sample notifications if database error
            notificationPanel.addUpcomingBookingNotification("John Doe", "Tennis Court 1", 
                LocalDateTime.now().plusHours(2));
            notificationPanel.addPendingPaymentNotification("Jane Smith", 150.00);
            notificationPanel.addMaintenanceNotification("Swimming Pool", "Filter replacement needed");
        }
    }
    
    private void updateChartsWithRealData() {
        try {
            // Generate bookings data for the last 6 months
            Map<String, Number> bookingsData = new LinkedHashMap<>();
            LocalDateTime now = LocalDateTime.now();
            
            for (int i = 5; i >= 0; i--) {
                LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                
                long monthlyBookings = bookingDao.findAll().stream()
                    .filter(booking -> {
                        LocalDateTime bookingTime = null;
                        if (booking instanceof model.FacilityBooking) {
                            bookingTime = ((model.FacilityBooking) booking).getStartTime();
                        } else if (booking instanceof model.TrainingBooking) {
                            bookingTime = ((model.TrainingBooking) booking).getStartTime();
                        }
                        return bookingTime != null && bookingTime.isAfter(monthStart) && bookingTime.isBefore(monthEnd);
                    })
                    .count();
                
                String monthName = monthStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
                bookingsData.put(monthName, monthlyBookings);
            }
            
            statisticsPanel.updateChart("Bookings", bookingsData);
            
            // Generate revenue data for the last 6 months
            Map<String, Number> revenueData = new LinkedHashMap<>();
            
            for (int i = 5; i >= 0; i--) {
                LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                
                double monthlyRevenue = paymentDao.findAll().stream()
                    .filter(payment -> payment.getPaidAt().isAfter(monthStart) && payment.getPaidAt().isBefore(monthEnd))
                    .mapToDouble(Payment::getAmount)
                    .sum();
                
                String monthName = monthStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
                revenueData.put(monthName, monthlyRevenue);
            }
            
            statisticsPanel.updateChart("Revenue", revenueData);
            
        } catch (Exception e) {
            // Fallback to sample data
            Map<String, Number> sampleBookingsData = new LinkedHashMap<>();
            sampleBookingsData.put("Jan", 15);
            sampleBookingsData.put("Feb", 23);
            sampleBookingsData.put("Mar", 18);
            sampleBookingsData.put("Apr", 31);
            sampleBookingsData.put("May", 27);
            sampleBookingsData.put("Jun", 35);
            
            Map<String, Number> sampleRevenueData = new LinkedHashMap<>();
            sampleRevenueData.put("Jan", 1250.0);
            sampleRevenueData.put("Feb", 1890.0);
            sampleRevenueData.put("Mar", 1450.0);
            sampleRevenueData.put("Apr", 2100.0);
            sampleRevenueData.put("May", 1750.0);
            sampleRevenueData.put("Jun", 2300.0);
            
            statisticsPanel.updateChart("Bookings", sampleBookingsData);
            statisticsPanel.updateChart("Revenue", sampleRevenueData);
        }
    }
    
    private void refreshStatistics() {
        // This would refresh statistics from the database
        updateStatistics();
        JOptionPane.showMessageDialog(this, "Statistics refreshed successfully!", 
            "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateQuickReport() {
        // Create a dialog for report options
        JDialog reportDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Report", true);
        reportDialog.setSize(700, 750);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout());
        reportDialog.getContentPane().setBackground(AppColors.BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📊 Generate Report");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Select report type and generate comprehensive data export");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        reportDialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel with improved layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AppColors.BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Main form panel with vertical layout
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(AppColors.CARD);
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Report type selection section
        JPanel reportTypeSection = new JPanel(new BorderLayout(10, 5));
        reportTypeSection.setBackground(AppColors.CARD);
        reportTypeSection.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel typeLabel = new JLabel("📊 Report Type");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 14f));
        typeLabel.setForeground(AppColors.TEXT_PRIMARY);
        typeLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        reportTypeSection.add(typeLabel, BorderLayout.NORTH);
        
        JComboBox<String> reportTypeCombo = new JComboBox<>(new String[]{
            "📈 Comprehensive Dashboard Report",
            "👥 Members Report",
            "📅 Bookings Report", 
            "💰 Payments Report",
            "🏢 Facilities Report",
            "🔧 Maintenance Report",
            "💬 Feedback Report"
        });
        reportTypeCombo.setFont(reportTypeCombo.getFont().deriveFont(Font.PLAIN, 13f));
        reportTypeCombo.setPreferredSize(new Dimension(500, 40));
        reportTypeCombo.setMaximumSize(new Dimension(500, 40));
        reportTypeCombo.setBackground(AppColors.BG_DARK);
        reportTypeCombo.setForeground(AppColors.TEXT_PRIMARY);
        reportTypeCombo.setBorder(new EmptyBorder(8, 12, 8, 12));
        reportTypeSection.add(reportTypeCombo, BorderLayout.CENTER);
        
        formPanel.add(reportTypeSection);
        
        // Date range selection section
        JPanel dateRangeSection = new JPanel(new BorderLayout(10, 5));
        dateRangeSection.setBackground(AppColors.CARD);
        dateRangeSection.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel dateLabel = new JLabel("📅 Date Range");
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 14f));
        dateLabel.setForeground(AppColors.TEXT_PRIMARY);
        dateLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        dateRangeSection.add(dateLabel, BorderLayout.NORTH);
        
        // Create the enhanced date range picker
        ui.components.DateRangePicker dateRangePicker = new ui.components.DateRangePicker();
        dateRangePicker.setPreferredSize(new Dimension(500, 200));
        dateRangePicker.setMaximumSize(new Dimension(500, 200));
        dateRangeSection.add(dateRangePicker, BorderLayout.CENTER);
        
        formPanel.add(dateRangeSection);
        
        // Format selection section
        JPanel formatSection = new JPanel(new BorderLayout(10, 5));
        formatSection.setBackground(AppColors.CARD);
        formatSection.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel formatLabel = new JLabel("📄 Export Format");
        formatLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD, 14f));
        formatLabel.setForeground(AppColors.TEXT_PRIMARY);
        formatLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        formatSection.add(formatLabel, BorderLayout.NORTH);
        
        // Create a container panel for the dropdown with better visibility
        JPanel formatContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        formatContainer.setBackground(AppColors.CARD);
        
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"CSV", "TXT"});
        formatCombo.setFont(formatCombo.getFont().deriveFont(Font.PLAIN, 13f));
        formatCombo.setPreferredSize(new Dimension(250, 40));
        formatCombo.setMaximumSize(new Dimension(250, 40));
        formatCombo.setBackground(AppColors.BG_DARK);
        formatCombo.setForeground(AppColors.TEXT_PRIMARY);
        formatCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        formatCombo.setFocusable(true);
        formatCombo.setEditable(false);
        
        // Add hover effect for better visibility
        formatCombo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formatCombo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.ACCENT, 2),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formatCombo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        formatContainer.add(formatCombo);
        formatSection.add(formatContainer, BorderLayout.CENTER);
        
        formPanel.add(formatSection);
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        reportDialog.add(contentPanel, BorderLayout.CENTER);
        
        // Footer with buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(AppColors.CARD);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton generateButton = createStyledButton("📊 Generate Report", AppColors.SUCCESS);
        JButton cancelButton = createStyledButton("✕ Cancel", AppColors.TEXT_SECONDARY);
        
        generateButton.addActionListener(e -> {
            String reportType = (String) reportTypeCombo.getSelectedItem();
            String format = (String) formatCombo.getSelectedItem();
            
            // Validate date range before proceeding
            if (!dateRangePicker.isValidRange()) {
                JOptionPane.showMessageDialog(reportDialog, 
                    "Please enter a valid date range before generating the report.", 
                    "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String fromDateStr = dateRangePicker.getFromDateString();
            String toDateStr = dateRangePicker.getToDateString();
            
            try {
                generateReport(reportType, format, fromDateStr, toDateStr);
                reportDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(reportDialog, 
                    "Error generating report: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> reportDialog.dispose());
        
        footerPanel.add(cancelButton);
        footerPanel.add(generateButton);
        
        reportDialog.add(footerPanel, BorderLayout.SOUTH);
        
        reportDialog.setVisible(true);
    }
    
    private void generateReport(String reportType, String format, String fromDate, String toDate) throws Exception {
        // Show progress dialog
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generating Report", true);
        progressDialog.setSize(300, 150);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout());
        
        JLabel progressLabel = new JLabel("Generating report...", JLabel.CENTER);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 14f));
        progressLabel.setForeground(AppColors.TEXT_PRIMARY);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        progressDialog.add(progressLabel, BorderLayout.CENTER);
        progressDialog.add(progressBar, BorderLayout.SOUTH);
        progressDialog.setVisible(true);
        
        // Generate report in background
        SwingUtilities.invokeLater(() -> {
            try {
                String fileName = generateReportData(reportType, format, fromDate, toDate);
                progressDialog.dispose();
                
                JOptionPane.showMessageDialog(this, 
                    "Report generated successfully!\nFile saved as: " + fileName, 
                    "Report Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                progressDialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Error generating report: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private String generateReportData(String reportType, String format, String fromDate, String toDate) throws Exception {
        String fileName = "report_" + System.currentTimeMillis() + "." + format.toLowerCase();
        java.io.File file = new java.io.File(fileName);
        
        switch (reportType) {
            case "📈 Comprehensive Dashboard Report":
                generateComprehensiveReport(file, format);
                break;
            case "👥 Members Report":
                generateMembersReport(file, format);
                break;
            case "📅 Bookings Report":
                generateBookingsReport(file, format, fromDate, toDate);
                break;
            case "💰 Payments Report":
                generatePaymentsReport(file, format, fromDate, toDate);
                break;
            case "🏢 Facilities Report":
                generateFacilitiesReport(file, format);
                break;
            case "🔧 Maintenance Report":
                generateMaintenanceReport(file, format);
                break;
            case "💬 Feedback Report":
                generateFeedbackReport(file, format);
                break;
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
        
        return fileName;
    }
    
    private void generateComprehensiveReport(java.io.File file, String format) throws Exception {
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
    
    private void generateMembersReport(java.io.File file, String format) throws Exception {
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
    
    private void generateBookingsReport(java.io.File file, String format, String fromDate, String toDate) throws Exception {
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
    
    private void generatePaymentsReport(java.io.File file, String format, String fromDate, String toDate) throws Exception {
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
    
    private void generateFacilitiesReport(java.io.File file, String format) throws Exception {
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
    
    private void generateMaintenanceReport(java.io.File file, String format) throws Exception {
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
    
    private void generateFeedbackReport(java.io.File file, String format) throws Exception {
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
    
    private void showAllNotifications() {
        // Create a dialog to show all notifications
        JDialog notificationDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "All Notifications", true);
        notificationDialog.setSize(600, 500);
        notificationDialog.setLocationRelativeTo(this);
        notificationDialog.setLayout(new BorderLayout());
        notificationDialog.getContentPane().setBackground(AppColors.BG);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🔔 All Notifications");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("View and manage all system notifications");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        notificationDialog.add(headerPanel, BorderLayout.NORTH);
        
        // Notifications content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AppColors.BG);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create a scrollable list of notifications
        JPanel notificationsList = new JPanel();
        notificationsList.setLayout(new BoxLayout(notificationsList, BoxLayout.Y_AXIS));
        notificationsList.setBackground(AppColors.BG);
        
        // Load real notifications from database
        loadRealNotifications(notificationsList);
        
        JScrollPane scrollPane = new JScrollPane(notificationsList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(AppColors.BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        notificationDialog.add(contentPanel, BorderLayout.CENTER);
        
        // Footer with actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(AppColors.CARD);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton markAllRead = createStyledButton("✓ Mark All Read", AppColors.SUCCESS);
        JButton clearAll = createStyledButton("🗑️ Clear All", AppColors.ERROR);
        JButton closeButton = createStyledButton("✕ Close", AppColors.TEXT_SECONDARY);
        
        markAllRead.addActionListener(e -> {
            JOptionPane.showMessageDialog(notificationDialog, "All notifications marked as read!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        clearAll.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(notificationDialog, 
                "Are you sure you want to clear all notifications?", 
                "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                notificationsList.removeAll();
                notificationsList.revalidate();
                notificationsList.repaint();
                JOptionPane.showMessageDialog(notificationDialog, "All notifications cleared!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        closeButton.addActionListener(e -> notificationDialog.dispose());
        
        footerPanel.add(markAllRead);
        footerPanel.add(clearAll);
        footerPanel.add(closeButton);
        
        notificationDialog.add(footerPanel, BorderLayout.SOUTH);
        
        notificationDialog.setVisible(true);
    }
    
    private void loadRealNotifications(JPanel notificationsList) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 1. Upcoming Bookings (next 24 hours)
            bookingDao.findAll().stream()
                .filter(booking -> {
                    LocalDateTime bookingTime = null;
                    BookingStatus status = null;
                    if (booking instanceof model.FacilityBooking) {
                        model.FacilityBooking fb = (model.FacilityBooking) booking;
                        bookingTime = fb.getStartTime();
                        status = fb.getStatus();
                    } else if (booking instanceof model.TrainingBooking) {
                        model.TrainingBooking tb = (model.TrainingBooking) booking;
                        bookingTime = tb.getStartTime();
                        status = tb.getStatus();
                    }
                    return bookingTime != null && 
                           bookingTime.isAfter(now) && 
                           bookingTime.isBefore(now.plusHours(24)) &&
                           status == BookingStatus.CONFIRMED;
                })
                .forEach(booking -> {
                    try {
                        String memberName = "";
                        String facilityName = "";
                        LocalDateTime bookingTime = null;
                        
                        if (booking instanceof model.FacilityBooking) {
                            model.FacilityBooking fb = (model.FacilityBooking) booking;
                            memberName = userDao.findById(fb.getMemberId()).map(User::getFullName).orElse("Unknown Member");
                            facilityName = facilityDao.findById(fb.getFacilityId()).map(model.Facility::getName).orElse("Unknown Facility");
                            bookingTime = fb.getStartTime();
                        } else if (booking instanceof model.TrainingBooking) {
                            model.TrainingBooking tb = (model.TrainingBooking) booking;
                            memberName = userDao.findById(tb.getMemberId()).map(User::getFullName).orElse("Unknown Member");
                            facilityName = facilityDao.findById(tb.getFacilityId()).map(model.Facility::getName).orElse("Unknown Facility");
                            bookingTime = tb.getStartTime();
                        }
                        
                        if (bookingTime != null) {
                            long hoursUntil = java.time.Duration.between(now, bookingTime).toHours();
                            String timeText = hoursUntil == 1 ? "in 1 hour" : "in " + hoursUntil + " hours";
                            
                            addNotificationToList(notificationsList, 
                                "📅 Upcoming Booking", 
                                memberName + " has a " + facilityName + " booking " + timeText,
                                timeText,
                                AppColors.ACCENT);
                        }
                    } catch (Exception e) {
                        // Skip this notification if there's an error
                    }
                });
            
            // 2. Pending Payments (unpaid bookings)
            paymentDao.findAll().stream()
                .filter(payment -> payment.getPaidAt() == null)
                .forEach(payment -> {
                    try {
                        String userName = userDao.findById(payment.getUserId()).map(User::getFullName).orElse("Unknown User");
                        String timeAgo = payment.getPaidAt() != null ? 
                            formatTimeAgo(payment.getPaidAt().atZone(java.time.ZoneId.systemDefault()).toInstant()) : 
                            "Unknown time";
                        
                        addNotificationToList(notificationsList,
                            "💰 Pending Payment",
                            userName + " has a pending payment of $" + String.format("%.2f", payment.getAmount()),
                            timeAgo,
                            AppColors.WARNING);
                    } catch (Exception e) {
                        // Skip this notification if there's an error
                    }
                });
            
            // 3. Maintenance Requests (pending status)
            dao.impl.MaintenanceDaoJdbc maintenanceDao = new dao.impl.MaintenanceDaoJdbc();
            maintenanceDao.findAll().stream()
                .filter(request -> request.getStatus() == model.MaintenanceStatus.OPEN)
                .forEach(request -> {
                    try {
                        String facilityName = facilityDao.findById(request.getFacilityId()).map(model.Facility::getName).orElse("Unknown Facility");
                        String timeAgo = formatTimeAgo(request.getCreatedAt());
                        
                        addNotificationToList(notificationsList,
                            "🔧 Maintenance Request",
                            facilityName + ": " + request.getTitle(),
                            timeAgo,
                            AppColors.ERROR);
                    } catch (Exception e) {
                        // Skip this notification if there's an error
                    }
                });
            
            // 4. Recent Bookings (confirmed in last 24 hours)
            bookingDao.findAll().stream()
                .filter(booking -> {
                    LocalDateTime bookingTime = null;
                    BookingStatus status = null;
                    if (booking instanceof model.FacilityBooking) {
                        model.FacilityBooking fb = (model.FacilityBooking) booking;
                        bookingTime = fb.getStartTime();
                        status = fb.getStatus();
                    } else if (booking instanceof model.TrainingBooking) {
                        model.TrainingBooking tb = (model.TrainingBooking) booking;
                        bookingTime = tb.getStartTime();
                        status = tb.getStatus();
                    }
                    return bookingTime != null && 
                           bookingTime.isAfter(now.minusHours(24)) &&
                           bookingTime.isBefore(now) &&
                           status == BookingStatus.CONFIRMED;
                })
                .forEach(booking -> {
                    try {
                        String memberName = "";
                        String facilityName = "";
                        LocalDateTime bookingTime = null;
                        
                        if (booking instanceof model.FacilityBooking) {
                            model.FacilityBooking fb = (model.FacilityBooking) booking;
                            memberName = userDao.findById(fb.getMemberId()).map(User::getFullName).orElse("Unknown Member");
                            facilityName = facilityDao.findById(fb.getFacilityId()).map(model.Facility::getName).orElse("Unknown Facility");
                            bookingTime = fb.getStartTime();
                        } else if (booking instanceof model.TrainingBooking) {
                            model.TrainingBooking tb = (model.TrainingBooking) booking;
                            memberName = userDao.findById(tb.getMemberId()).map(User::getFullName).orElse("Unknown Member");
                            facilityName = facilityDao.findById(tb.getFacilityId()).map(model.Facility::getName).orElse("Unknown Facility");
                            bookingTime = tb.getStartTime();
                        }
                        
                        if (bookingTime != null) {
                            long hoursAgo = java.time.Duration.between(bookingTime, now).toHours();
                            String timeText = hoursAgo == 1 ? "1 hour ago" : hoursAgo + " hours ago";
                            
                            addNotificationToList(notificationsList,
                                "✅ Recent Booking",
                                memberName + "'s " + facilityName + " booking completed",
                                timeText,
                                AppColors.SUCCESS);
                        }
                    } catch (Exception e) {
                        // Skip this notification if there's an error
                    }
                });
            
            // 5. Low facility availability (less than 3 available slots today)
            facilityDao.findAll().stream()
                .forEach(facility -> {
                    try {
                        long bookedSlots = bookingDao.findAll().stream()
                            .filter(booking -> {
                                LocalDateTime bookingTime = null;
                                BookingStatus status = null;
                                if (booking instanceof model.FacilityBooking) {
                                    model.FacilityBooking fb = (model.FacilityBooking) booking;
                                    bookingTime = fb.getStartTime();
                                    status = fb.getStatus();
                                } else if (booking instanceof model.TrainingBooking) {
                                    model.TrainingBooking tb = (model.TrainingBooking) booking;
                                    bookingTime = tb.getStartTime();
                                    status = tb.getStatus();
                                }
                                return bookingTime != null && 
                                       bookingTime.toLocalDate().equals(now.toLocalDate()) &&
                                       getFacilityId(booking).equals(facility.getId()) &&
                                       status == BookingStatus.CONFIRMED;
                            })
                            .count();
                        
                        // Assuming max 8 slots per facility per day
                        if (bookedSlots >= 6) { // Less than 3 slots available
                            addNotificationToList(notificationsList,
                                "📊 High Demand",
                                facility.getName() + " has high booking demand today",
                                "Today",
                                AppColors.ACCENT);
                        }
                    } catch (Exception e) {
                        // Skip this notification if there's an error
                    }
                });
            
        } catch (Exception e) {
            // If there's any error loading notifications, show a fallback message
            addNotificationToList(notificationsList,
                "⚠️ System Notice",
                "Unable to load notifications from database",
                "Now",
                AppColors.WARNING);
        }
    }
    
    private Long getFacilityId(Object booking) {
        if (booking instanceof model.FacilityBooking) {
            return ((model.FacilityBooking) booking).getFacilityId();
        } else if (booking instanceof model.TrainingBooking) {
            return ((model.TrainingBooking) booking).getFacilityId();
        }
        return null;
    }
    
    private String formatTimeAgo(java.time.Instant instant) {
        if (instant == null) return "Unknown time";
        
        LocalDateTime time = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        
        long hours = java.time.Duration.between(time, now).toHours();
        long days = java.time.Duration.between(time, now).toDays();
        
        if (days > 0) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (hours > 0) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else {
            return "Just now";
        }
    }
    
    private void addNotificationToList(JPanel parent, String type, String message, String time, Color color) {
        JPanel notificationItem = new JPanel(new BorderLayout());
        notificationItem.setBackground(AppColors.CARD);
        notificationItem.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        notificationItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Left side - icon and type
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(AppColors.CARD);
        
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD, 14f));
        typeLabel.setForeground(color);
        leftPanel.add(typeLabel, BorderLayout.NORTH);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 11f));
        timeLabel.setForeground(AppColors.TEXT_SECONDARY);
        leftPanel.add(timeLabel, BorderLayout.SOUTH);
        
        // Right side - message and actions
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppColors.CARD);
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 13f));
        messageLabel.setForeground(AppColors.TEXT_PRIMARY);
        rightPanel.add(messageLabel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setBackground(AppColors.CARD);
        
        JButton markReadButton = new JButton("✓");
        markReadButton.setFont(markReadButton.getFont().deriveFont(Font.BOLD, 10f));
        markReadButton.setPreferredSize(new Dimension(25, 20));
        markReadButton.setBackground(AppColors.SUCCESS);
        markReadButton.setForeground(Color.WHITE);
        markReadButton.setFocusPainted(false);
        markReadButton.setBorder(new EmptyBorder(2, 4, 2, 4));
        markReadButton.setToolTipText("Mark as read");
        
        JButton dismissButton = new JButton("✕");
        dismissButton.setFont(dismissButton.getFont().deriveFont(Font.BOLD, 10f));
        dismissButton.setPreferredSize(new Dimension(25, 20));
        dismissButton.setBackground(AppColors.ERROR);
        dismissButton.setForeground(Color.WHITE);
        dismissButton.setFocusPainted(false);
        dismissButton.setBorder(new EmptyBorder(2, 4, 2, 4));
        dismissButton.setToolTipText("Dismiss");
        
        markReadButton.addActionListener(e -> {
            notificationItem.setBackground(AppColors.BG_DARK);
            JOptionPane.showMessageDialog(parent, "Notification marked as read!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        dismissButton.addActionListener(e -> {
            parent.remove(notificationItem);
            parent.revalidate();
            parent.repaint();
        });
        
        actionPanel.add(markReadButton);
        actionPanel.add(dismissButton);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        
        notificationItem.add(leftPanel, BorderLayout.WEST);
        notificationItem.add(rightPanel, BorderLayout.CENTER);
        
        parent.add(notificationItem);
        parent.add(Box.createVerticalStrut(8));
    }
    
    // POLYMORPHISM: Method demonstrates polymorphism through parameter types and return types
    // This method creates buttons that can be used polymorphically as JButton instances
    private JButton createStyledButton(String text, Color backgroundColor) {
        // POLYMORPHISM: JButton can be treated as Component, AbstractButton, etc.
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // POLYMORPHISM: MouseAdapter demonstrates interface polymorphism
        // Different MouseAdapter implementations can be used interchangeably
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
    
    // Public methods for external access
    public NotificationPanel getNotificationPanel() {
        return notificationPanel;
    }
    
    public StatisticsPanel getStatisticsPanel() {
        return statisticsPanel;
    }
}

package ui.components;

import ui.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateRangePicker extends JPanel {
    private JTextField fromDateField;
    private JTextField toDateField;
    private JLabel statusLabel;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public DateRangePicker() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateStatus();
    }
    
    private void initializeComponents() {
        // From date field
        fromDateField = new JTextField(12);
        fromDateField.setFont(fromDateField.getFont().deriveFont(Font.PLAIN, 13f));
        fromDateField.setBackground(AppColors.BG_DARK);
        fromDateField.setForeground(AppColors.TEXT_PRIMARY);
        fromDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        fromDateField.setText(LocalDateTime.now().minusMonths(1).toLocalDate().toString());
        fromDateField.setToolTipText("Enter start date in YYYY-MM-DD format");
        
        // To date field
        toDateField = new JTextField(12);
        toDateField.setFont(toDateField.getFont().deriveFont(Font.PLAIN, 13f));
        toDateField.setBackground(AppColors.BG_DARK);
        toDateField.setForeground(AppColors.TEXT_PRIMARY);
        toDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        toDateField.setText(LocalDateTime.now().toLocalDate().toString());
        toDateField.setToolTipText("Enter end date in YYYY-MM-DD format");
        
        // Status label for validation feedback
        statusLabel = new JLabel("✓ Valid date range");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        statusLabel.setForeground(AppColors.SUCCESS);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 5));
        setBackground(AppColors.CARD);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.ACCENT, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        
        JLabel titleLabel = new JLabel("📅 Select Date Range");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        headerPanel.add(statusLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Date fields panel
        JPanel dateFieldsPanel = new JPanel(new GridBagLayout());
        dateFieldsPanel.setBackground(AppColors.CARD);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // From date
        JLabel fromLabel = new JLabel("📆 From Date:");
        fromLabel.setFont(fromLabel.getFont().deriveFont(Font.BOLD, 12f));
        fromLabel.setForeground(AppColors.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        dateFieldsPanel.add(fromLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateFieldsPanel.add(fromDateField, gbc);
        
        // To date
        JLabel toLabel = new JLabel("📆 To Date:");
        toLabel.setFont(toLabel.getFont().deriveFont(Font.BOLD, 12f));
        toLabel.setForeground(AppColors.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        dateFieldsPanel.add(toLabel, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateFieldsPanel.add(toDateField, gbc);
        
        add(dateFieldsPanel, BorderLayout.CENTER);
        
        // Quick selection buttons
        JPanel quickButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        quickButtonsPanel.setBackground(AppColors.CARD);
        
        // Today button
        JButton todayBtn = createQuickButton("Today", () -> {
            LocalDate today = LocalDate.now();
            fromDateField.setText(today.toString());
            toDateField.setText(today.toString());
            updateStatus();
        });
        
        // Yesterday button
        JButton yesterdayBtn = createQuickButton("Yesterday", () -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            fromDateField.setText(yesterday.toString());
            toDateField.setText(yesterday.toString());
            updateStatus();
        });
        
        // This week button
        JButton weekBtn = createQuickButton("This Week", () -> {
            LocalDate startOfWeek = LocalDate.now().minusDays(7);
            fromDateField.setText(startOfWeek.toString());
            toDateField.setText(LocalDate.now().toString());
            updateStatus();
        });
        
        // This month button
        JButton monthBtn = createQuickButton("This Month", () -> {
            LocalDate startOfMonth = LocalDate.now().minusMonths(1);
            fromDateField.setText(startOfMonth.toString());
            toDateField.setText(LocalDate.now().toString());
            updateStatus();
        });
        
        // Last 3 months button
        JButton quarterBtn = createQuickButton("Last 3 Months", () -> {
            LocalDate startOfQuarter = LocalDate.now().minusMonths(3);
            fromDateField.setText(startOfQuarter.toString());
            toDateField.setText(LocalDate.now().toString());
            updateStatus();
        });
        
        // This year button
        JButton yearBtn = createQuickButton("This Year", () -> {
            LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
            fromDateField.setText(startOfYear.toString());
            toDateField.setText(LocalDate.now().toString());
            updateStatus();
        });
        
        quickButtonsPanel.add(todayBtn);
        quickButtonsPanel.add(yesterdayBtn);
        quickButtonsPanel.add(weekBtn);
        quickButtonsPanel.add(monthBtn);
        quickButtonsPanel.add(quarterBtn);
        quickButtonsPanel.add(yearBtn);
        
        add(quickButtonsPanel, BorderLayout.SOUTH);
    }
    
    private JButton createQuickButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 10f));
        button.setPreferredSize(new Dimension(80, 28));
        button.setBackground(AppColors.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(4, 8, 4, 8));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(AppColors.ACCENT.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(AppColors.ACCENT);
            }
        });
        
        button.addActionListener(e -> {
            action.run();
            // Visual feedback
            button.setBackground(AppColors.SUCCESS);
            Timer timer = new Timer(200, evt -> button.setBackground(AppColors.ACCENT));
            timer.setRepeats(false);
            timer.start();
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        // Add document listeners for real-time validation
        fromDateField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
        });
        
        toDateField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
        });
        
        // Add focus listeners for better UX
        fromDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fromDateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.ACCENT, 2),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fromDateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        toDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                toDateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.ACCENT, 2),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                toDateField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim(), dateFormatter);
                LocalDate toDate = LocalDate.parse(toDateField.getText().trim(), dateFormatter);
                
                if (fromDate.isAfter(toDate)) {
                    statusLabel.setText("⚠️ From date must be before To date");
                    statusLabel.setForeground(AppColors.WARNING);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppColors.WARNING, 2),
                        new EmptyBorder(15, 15, 15, 15)
                    ));
                } else if (fromDate.isAfter(LocalDate.now())) {
                    statusLabel.setText("⚠️ From date cannot be in the future");
                    statusLabel.setForeground(AppColors.WARNING);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppColors.WARNING, 2),
                        new EmptyBorder(15, 15, 15, 15)
                    ));
                } else {
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
                    statusLabel.setText(String.format("✓ Valid range (%d days)", daysBetween));
                    statusLabel.setForeground(AppColors.SUCCESS);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppColors.ACCENT, 2),
                        new EmptyBorder(15, 15, 15, 15)
                    ));
                }
            } catch (DateTimeParseException e) {
                statusLabel.setText("❌ Invalid date format (use YYYY-MM-DD)");
                statusLabel.setForeground(AppColors.ERROR);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppColors.ERROR, 2),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });
    }
    
    // Public methods for getting date values
    public LocalDate getFromDate() {
        try {
            return LocalDate.parse(fromDateField.getText().trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            return LocalDate.now().minusMonths(1);
        }
    }
    
    public LocalDate getToDate() {
        try {
            return LocalDate.parse(toDateField.getText().trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }
    
    public String getFromDateString() {
        return fromDateField.getText().trim();
    }
    
    public String getToDateString() {
        return toDateField.getText().trim();
    }
    
    public boolean isValidRange() {
        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText().trim(), dateFormatter);
            LocalDate toDate = LocalDate.parse(toDateField.getText().trim(), dateFormatter);
            return !fromDate.isAfter(toDate) && !fromDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public void setFromDate(LocalDate date) {
        fromDateField.setText(date.toString());
        updateStatus();
    }
    
    public void setToDate(LocalDate date) {
        toDateField.setText(date.toString());
        updateStatus();
    }
}

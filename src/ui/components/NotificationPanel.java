package ui.components;

import ui.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationPanel extends JPanel {
    private JPanel notificationList;
    private List<Notification> notifications;
    private JLabel notificationCount;
    
    public NotificationPanel() {
        this.notifications = new ArrayList<>();
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        notificationList = new JPanel();
        notificationList.setLayout(new BoxLayout(notificationList, BoxLayout.Y_AXIS));
        notificationList.setBackground(AppColors.BG);
        
        notificationCount = new JLabel("0");
        notificationCount.setFont(notificationCount.getFont().deriveFont(Font.BOLD, 12f));
        notificationCount.setForeground(AppColors.ERROR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("🔔 Notifications");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        // Notification count badge
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        countPanel.setBackground(AppColors.CARD);
        
        JLabel countLabel = new JLabel("Unread:");
        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 12f));
        countLabel.setForeground(AppColors.TEXT_SECONDARY);
        countPanel.add(countLabel);
        countPanel.add(notificationCount);
        
        headerPanel.add(countPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Notification list
        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(AppColors.BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(AppColors.CARD);
        actionPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JButton markAllRead = createStyledButton("✓ Mark All Read", AppColors.SUCCESS);
        JButton clearAll = createStyledButton("🗑️ Clear All", AppColors.ERROR);
        
        markAllRead.addActionListener(e -> markAllAsRead());
        clearAll.addActionListener(e -> clearAllNotifications());
        
        actionPanel.add(markAllRead);
        actionPanel.add(clearAll);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    public void addNotification(NotificationType type, String title, String message) {
        Notification notification = new Notification(type, title, message, LocalDateTime.now());
        notifications.add(notification);
        updateNotificationList();
        updateNotificationCount();
    }
    
    public void addUpcomingBookingNotification(String memberName, String facilityName, LocalDateTime bookingTime) {
        String title = "📅 Upcoming Booking";
        String message = String.format("Booking for %s at %s in %s", 
            memberName, facilityName, formatTimeUntil(bookingTime));
        addNotification(NotificationType.BOOKING, title, message);
    }
    
    public void addPendingPaymentNotification(String memberName, double amount) {
        String title = "💰 Pending Payment";
        String message = String.format("Payment of $%.2f pending for %s", amount, memberName);
        addNotification(NotificationType.PAYMENT, title, message);
    }
    
    public void addMaintenanceNotification(String facilityName, String issue) {
        String title = "🔧 Maintenance Required";
        String message = String.format("Maintenance needed for %s: %s", facilityName, issue);
        addNotification(NotificationType.MAINTENANCE, title, message);
    }
    
    private void updateNotificationList() {
        notificationList.removeAll();
        
        for (Notification notification : notifications) {
            JPanel notificationCard = createNotificationCard(notification);
            notificationList.add(notificationCard);
            notificationList.add(Box.createVerticalStrut(10));
        }
        
        notificationList.revalidate();
        notificationList.repaint();
    }
    
    private JPanel createNotificationCard(Notification notification) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Left side - icon and content
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(AppColors.CARD);
        
        // Icon and title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(AppColors.CARD);
        
        JLabel iconLabel = new JLabel(notification.getType().getIcon());
        iconLabel.setFont(iconLabel.getFont().deriveFont(16f));
        iconLabel.setForeground(notification.getType().getColor());
        topPanel.add(iconLabel);
        
        JLabel titleLabel = new JLabel(notification.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        topPanel.add(titleLabel);
        
        leftPanel.add(topPanel, BorderLayout.NORTH);
        
        // Message
        JLabel messageLabel = new JLabel(notification.getMessage());
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 12f));
        messageLabel.setForeground(AppColors.TEXT_SECONDARY);
        leftPanel.add(messageLabel, BorderLayout.CENTER);
        
        card.add(leftPanel, BorderLayout.CENTER);
        
        // Right side - timestamp and actions
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppColors.CARD);
        
        JLabel timeLabel = new JLabel(formatTimestamp(notification.getTimestamp()));
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 10f));
        timeLabel.setForeground(AppColors.TEXT_SECONDARY);
        rightPanel.add(timeLabel, BorderLayout.NORTH);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(AppColors.CARD);
        
        if (!notification.isRead()) {
            JButton markRead = createSmallButton("✓", AppColors.SUCCESS);
            markRead.addActionListener(e -> markAsRead(notification));
            actionPanel.add(markRead);
        }
        
        JButton dismiss = createSmallButton("✕", AppColors.ERROR);
        dismiss.addActionListener(e -> dismissNotification(notification));
        actionPanel.add(dismiss);
        
        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        
        card.add(rightPanel, BorderLayout.EAST);
        
        // Highlight unread notifications
        if (!notification.isRead()) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(notification.getType().getColor(), 2),
                new EmptyBorder(13, 13, 13, 13)
            ));
        }
        
        return card;
    }
    
    private JButton createSmallButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 10f));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(2, 6, 2, 6));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(25, 20));
        
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
    
    private void markAsRead(Notification notification) {
        notification.markAsRead();
        updateNotificationList();
        updateNotificationCount();
    }
    
    private void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.markAsRead();
        }
        updateNotificationList();
        updateNotificationCount();
    }
    
    private void dismissNotification(Notification notification) {
        notifications.remove(notification);
        updateNotificationList();
        updateNotificationCount();
    }
    
    private void clearAllNotifications() {
        notifications.clear();
        updateNotificationList();
        updateNotificationCount();
    }
    
    private void updateNotificationCount() {
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        notificationCount.setText(String.valueOf(unreadCount));
    }
    
    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
    }
    
    private String formatTimeUntil(LocalDateTime bookingTime) {
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(now, bookingTime).toHours();
        
        if (hours < 1) {
            long minutes = java.time.Duration.between(now, bookingTime).toMinutes();
            return minutes + " minutes";
        } else if (hours < 24) {
            return hours + " hours";
        } else {
            long days = hours / 24;
            return days + " days";
        }
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
    
    // Inner classes
    public static class Notification {
        private NotificationType type;
        private String title;
        private String message;
        private LocalDateTime timestamp;
        private boolean read;
        
        public Notification(NotificationType type, String title, String message, LocalDateTime timestamp) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.read = false;
        }
        
        // Getters and setters
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void markAsRead() { this.read = true; }
    }
    
    public enum NotificationType {
        BOOKING("📅", AppColors.ACCENT),
        PAYMENT("💰", AppColors.WARNING),
        MAINTENANCE("🔧", AppColors.ERROR),
        SYSTEM("⚙️", AppColors.PRIMARY);
        
        private final String icon;
        private final Color color;
        
        NotificationType(String icon, Color color) {
            this.icon = icon;
            this.color = color;
        }
        
        public String getIcon() { return icon; }
        public Color getColor() { return color; }
    }
}

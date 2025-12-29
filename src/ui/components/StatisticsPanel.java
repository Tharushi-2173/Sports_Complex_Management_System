package ui.components;

import ui.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private ChartPanel bookingsChart;
    private ChartPanel revenueChart;
    
    public StatisticsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Statistics cards
        JPanel statsPanel = createStatsCards();
        add(statsPanel, BorderLayout.CENTER);
        
        // Charts panel
        JPanel chartsPanel = createChartsPanel();
        add(chartsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.CARD);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("📊 Statistics Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Real-time insights and analytics");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        header.add(subtitle, BorderLayout.SOUTH);
        
        return header;
    }
    
    private JPanel createStatsCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsPanel.setBackground(AppColors.BG);
        cardsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Total Members Card
        cardsPanel.add(createStatCard("👥", "Total Members", "0", AppColors.PRIMARY));
        
        // Active Bookings Card
        cardsPanel.add(createStatCard("📅", "Active Bookings", "0", AppColors.SUCCESS));
        
        // Monthly Revenue Card
        cardsPanel.add(createStatCard("💰", "Monthly Revenue", "$0.00", AppColors.ACCENT));
        
        // Facilities Card
        cardsPanel.add(createStatCard("🏢", "Total Facilities", "0", AppColors.WARNING));
        
        // Pending Payments Card
        cardsPanel.add(createStatCard("⏳", "Pending Payments", "0", AppColors.ERROR));
        
        // Utilization Rate Card
        cardsPanel.add(createStatCard("📈", "Utilization Rate", "0%", AppColors.PRIMARY_LIGHT));
        
        return cardsPanel;
    }
    
    private JPanel createStatCard(String icon, String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Icon and title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(AppColors.CARD);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(iconLabel.getFont().deriveFont(24f));
        iconLabel.setForeground(accentColor);
        topPanel.add(iconLabel);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        topPanel.add(titleLabel);
        
        card.add(topPanel, BorderLayout.NORTH);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsPanel.setBackground(AppColors.BG);
        chartsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Bookings Chart
        bookingsChart = new ChartPanel("📅 Bookings Trend", "Monthly booking statistics", AppColors.ACCENT, true);
        chartsPanel.add(createChartContainer(bookingsChart, "📅 Bookings Trend", "Monthly booking statistics"));
        
        // Revenue Chart
        revenueChart = new ChartPanel("💰 Revenue Trend", "Monthly revenue statistics", AppColors.SUCCESS, false);
        chartsPanel.add(createChartContainer(revenueChart, "💰 Revenue Trend", "Monthly revenue statistics"));
        
        return chartsPanel;
    }
    
    private JPanel createChartContainer(ChartPanel chart, String title, String subtitle) {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(AppColors.CARD);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.CARD);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        subtitleLabel.setForeground(AppColors.TEXT_SECONDARY);
        header.add(subtitleLabel, BorderLayout.SOUTH);
        
        chartPanel.add(header, BorderLayout.NORTH);
        chartPanel.add(chart, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    // Public methods to update statistics
    public void updateStatCard(String cardTitle, String newValue) {
        // Find and update the specific stat card
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Component[] subComponents = panel.getComponents();
                for (Component subComp : subComponents) {
                    if (subComp instanceof JPanel) {
                        JPanel card = (JPanel) subComp;
                        Component[] cardComponents = card.getComponents();
                        for (Component cardComp : cardComponents) {
                            if (cardComp instanceof JPanel) {
                                JPanel topPanel = (JPanel) cardComp;
                                Component[] topComponents = topPanel.getComponents();
                                for (Component topComp : topComponents) {
                                    if (topComp instanceof JLabel) {
                                        JLabel label = (JLabel) topComp;
                                        if (cardTitle.equals(label.getText())) {
                                            // Found the card, now update the value
                                            Component[] allCardComponents = card.getComponents();
                                            for (Component allComp : allCardComponents) {
                                                if (allComp instanceof JLabel && allComp != topComp) {
                                                    JLabel valueLabel = (JLabel) allComp;
                                                    valueLabel.setText(newValue);
                                                    card.repaint();
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void updateChart(String chartTitle, Map<String, Number> data) {
        if (chartTitle.contains("Bookings") && bookingsChart != null) {
            bookingsChart.updateData(data);
        } else if (chartTitle.contains("Revenue") && revenueChart != null) {
            revenueChart.updateData(data);
        }
    }
}

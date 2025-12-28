package ui.components;

import ui.AppColors;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ChartPanel extends JPanel {
    private Map<String, Number> data;
    private Color chartColor;
    private boolean isBarChart;
    
    public ChartPanel(String title, String subtitle, Color chartColor, boolean isBarChart) {
        this.chartColor = chartColor;
        this.isBarChart = isBarChart;
        this.data = new LinkedHashMap<>();
        
        setPreferredSize(new Dimension(300, 200));
        setBackground(AppColors.BG_DARK);
        setBorder(BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1));
    }
    
    public void updateData(Map<String, Number> newData) {
        this.data = newData != null ? newData : new LinkedHashMap<>();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw background
        g2d.setColor(AppColors.BG_DARK);
        g2d.fillRect(0, 0, width, height);
        
        if (data.isEmpty()) {
            // Draw placeholder text
            g2d.setColor(AppColors.TEXT_SECONDARY);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "No data available";
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height + fm.getAscent()) / 2;
            g2d.drawString(text, x, y);
            g2d.dispose();
            return;
        }
        
        // Calculate chart dimensions
        int margin = 40;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        
        if (isBarChart) {
            drawBarChart(g2d, margin, chartWidth, chartHeight);
        } else {
            drawLineChart(g2d, margin, chartWidth, chartHeight);
        }
        
        g2d.dispose();
    }
    
    private void drawBarChart(Graphics2D g2d, int margin, int chartWidth, int chartHeight) {
        List<Map.Entry<String, Number>> entries = List.copyOf(data.entrySet());
        if (entries.isEmpty()) return;
        
        // Find max value for scaling
        double maxValue = entries.stream()
            .mapToDouble(entry -> entry.getValue().doubleValue())
            .max()
            .orElse(1.0);
        
        int barWidth = chartWidth / entries.size();
        int barSpacing = Math.max(1, barWidth / 10);
        int actualBarWidth = barWidth - barSpacing;
        
        // Draw bars
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Number> entry = entries.get(i);
            double value = entry.getValue().doubleValue();
            int barHeight = (int) ((value / maxValue) * chartHeight);
            
            int x = margin + i * barWidth + barSpacing / 2;
            int y = margin + chartHeight - barHeight;
            
            // Draw bar
            g2d.setColor(chartColor);
            g2d.fillRect(x, y, actualBarWidth, barHeight);
            
            // Draw bar border
            g2d.setColor(chartColor.darker());
            g2d.drawRect(x, y, actualBarWidth, barHeight);
            
            // Draw value label on top of bar
            if (barHeight > 15) {
                g2d.setColor(AppColors.TEXT_PRIMARY);
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 10f));
                FontMetrics fm = g2d.getFontMetrics();
                String valueText = formatValue(value);
                int textX = x + (actualBarWidth - fm.stringWidth(valueText)) / 2;
                int textY = y - 5;
                g2d.drawString(valueText, textX, textY);
            }
            
            // Draw label below bar
            g2d.setColor(AppColors.TEXT_SECONDARY);
            g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 9f));
            FontMetrics fm = g2d.getFontMetrics();
            String label = entry.getKey();
            int labelX = x + (actualBarWidth - fm.stringWidth(label)) / 2;
            int labelY = margin + chartHeight + 15;
            g2d.drawString(label, labelX, labelY);
        }
        
        // Draw Y-axis
        g2d.setColor(AppColors.BORDER_LIGHT);
        g2d.drawLine(margin, margin, margin, margin + chartHeight);
        
        // Draw X-axis
        g2d.drawLine(margin, margin + chartHeight, margin + chartWidth, margin + chartHeight);
    }
    
    private void drawLineChart(Graphics2D g2d, int margin, int chartWidth, int chartHeight) {
        List<Map.Entry<String, Number>> entries = List.copyOf(data.entrySet());
        if (entries.size() < 2) return;
        
        // Find max and min values for scaling
        double maxValue = entries.stream()
            .mapToDouble(entry -> entry.getValue().doubleValue())
            .max()
            .orElse(1.0);
        double minValue = entries.stream()
            .mapToDouble(entry -> entry.getValue().doubleValue())
            .min()
            .orElse(0.0);
        
        double valueRange = maxValue - minValue;
        if (valueRange == 0) valueRange = 1.0;
        
        // Draw grid lines
        g2d.setColor(AppColors.BORDER_LIGHT);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2}, 0));
        
        // Draw horizontal grid lines
        for (int i = 0; i <= 5; i++) {
            int y = margin + (i * chartHeight) / 5;
            g2d.drawLine(margin, y, margin + chartWidth, y);
            
            // Draw Y-axis labels
            double value = maxValue - (i * valueRange) / 5;
            g2d.setColor(AppColors.TEXT_SECONDARY);
            g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 8f));
            FontMetrics fm = g2d.getFontMetrics();
            String valueText = formatValue(value);
            int textX = margin - fm.stringWidth(valueText) - 5;
            int textY = y + fm.getAscent() / 2;
            g2d.drawString(valueText, textX, textY);
            g2d.setColor(AppColors.BORDER_LIGHT);
        }
        
        // Draw line
        g2d.setColor(chartColor);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int[] xPoints = new int[entries.size()];
        int[] yPoints = new int[entries.size()];
        
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Number> entry = entries.get(i);
            double value = entry.getValue().doubleValue();
            
            xPoints[i] = margin + (i * chartWidth) / (entries.size() - 1);
            yPoints[i] = margin + (int) (((maxValue - value) / valueRange) * chartHeight);
            
            // Draw data points
            g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
        }
        
        // Draw connecting lines
        for (int i = 0; i < entries.size() - 1; i++) {
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
        
        // Draw X-axis labels
        g2d.setColor(AppColors.TEXT_SECONDARY);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 9f));
        FontMetrics fm = g2d.getFontMetrics();
        
        for (int i = 0; i < entries.size(); i++) {
            String label = entries.get(i).getKey();
            int labelX = xPoints[i] - fm.stringWidth(label) / 2;
            int labelY = margin + chartHeight + 15;
            g2d.drawString(label, labelX, labelY);
        }
        
        // Draw axes
        g2d.setColor(AppColors.BORDER_LIGHT);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(margin, margin, margin, margin + chartHeight);
        g2d.drawLine(margin, margin + chartHeight, margin + chartWidth, margin + chartHeight);
    }
    
    private String formatValue(double value) {
        if (value >= 1000000) {
            return String.format("%.1fM", value / 1000000);
        } else if (value >= 1000) {
            return String.format("%.1fK", value / 1000);
        } else if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }
}

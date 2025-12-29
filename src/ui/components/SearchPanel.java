package ui.components;

import ui.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private Consumer<String> onSearchCallback;
    private Timer searchTimer;
    
    public SearchPanel(Consumer<String> onSearchCallback) {
        this.onSearchCallback = onSearchCallback;
        this.searchTimer = new Timer(300, e -> performSearch());
        this.searchTimer.setRepeats(false);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Search field
        searchField = new JTextField(20);
        searchField.setFont(searchField.getFont().deriveFont(Font.PLAIN, 13f));
        searchField.setBackground(AppColors.BG_DARK);
        searchField.setForeground(AppColors.TEXT_PRIMARY);
        searchField.setBorder(new EmptyBorder(6, 8, 6, 8));
        searchField.setToolTipText("Enter search term...");
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBackground(AppColors.CARD);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(searchLabel.getFont().deriveFont(Font.BOLD, 13f));
        searchLabel.setForeground(AppColors.TEXT_PRIMARY);
        add(searchLabel);
        
        add(searchField);
        
        JButton searchButton = createStyledButton("🔍", AppColors.ACCENT);
        searchButton.setPreferredSize(new Dimension(35, 30));
        searchButton.setToolTipText("Search");
        add(searchButton);
        
        JButton clearButton = createStyledButton("❌", AppColors.ERROR);
        clearButton.setPreferredSize(new Dimension(35, 30));
        clearButton.setToolTipText("Clear search");
        add(clearButton);
        
        // Add event handlers
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearSearch());
    }
    
    private void setupEventHandlers() {
        // Real-time search as user types
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchTimer.stop();
                    performSearch();
                } else {
                    // Debounced search - restart timer
                    searchTimer.restart();
                }
            }
        });
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            onSearchCallback.accept("");
            return;
        }
        
        // Send just the search query without type prefix
        onSearchCallback.accept(query);
    }
    
    private void clearSearch() {
        searchField.setText("");
        onSearchCallback.accept("");
    }
    
    public String getSearchQuery() {
        return searchField.getText().trim();
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(4, 8, 4, 8));
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

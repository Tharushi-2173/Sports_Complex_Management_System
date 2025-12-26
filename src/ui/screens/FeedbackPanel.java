package ui.screens;

import dao.impl.FacilityDaoJdbc;
import dao.impl.FeedbackDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.Feedback;
import model.Facility;
import model.Role;
import model.User;
import ui.AppColors;
import ui.components.SearchPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FeedbackPanel extends JPanel {
    private final FeedbackDaoJdbc dao = new FeedbackDaoJdbc();
    private final User currentUser;
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final FacilityDaoJdbc facilityDao = new FacilityDaoJdbc();
    private final DefaultTableModel model = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
        public String getColumnName(int column) {
            if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                String[] userCols = {"ID","Facility","Rating","Comments"};
                return column < userCols.length ? userCols[column] : "";
            } else {
                String[] adminCols = {"ID","User","Facility","Rating","Comments"};
                return column < adminCols.length ? adminCols[column] : "";
            }
        }
        public int getColumnCount() {
            return currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH) ? 4 : 5;
        }
    };
    private List<Feedback> allFeedback;
    private String currentSearchQuery = "";

    public FeedbackPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("💬 Feedback");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Share your experience and suggestions");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AppColors.BG);
        
        // Search Panel
        SearchPanel searchPanel = new SearchPanel(this::onSearch);
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(AppColors.CARD);
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(table.getFont().deriveFont(13f));
        table.setSelectionBackground(AppColors.PRIMARY_LIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(AppColors.BORDER_LIGHT);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setBackground(AppColors.CARD);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Actions Panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actions.setBackground(AppColors.CARD);
        actions.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JButton add = createStyledButton("💬 Submit Feedback", AppColors.SUCCESS);
        actions.add(add);
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        refresh();
    }
    
    private void onSearch(String searchQuery) {
        currentSearchQuery = searchQuery;
        refresh();
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

    private void refresh() {
        model.setRowCount(0);
        allFeedback = dao.findAll();
        
        // Apply user role filtering
        if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
            allFeedback.removeIf(f -> f.getUserId() == null || !f.getUserId().equals(currentUser.getId()));
        }
        
        // Apply search filtering
        List<Feedback> filteredFeedback = allFeedback;
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredFeedback = allFeedback.stream()
                .filter(feedback -> {
                    try {
                        String userName = userDao.findById(feedback.getUserId()).map(User::getFullName).orElse("");
                        String facilityName = feedback.getFacilityId() == null ? "" : 
                            facilityDao.findById(feedback.getFacilityId()).map(Facility::getName).orElse("");
                        String comments = feedback.getComments() != null ? feedback.getComments() : "";
                        String rating = String.valueOf(feedback.getRating());
                        
                        return userName.toLowerCase().contains(query) ||
                               facilityName.toLowerCase().contains(query) ||
                               comments.toLowerCase().contains(query) ||
                               rating.contains(query);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        for (Feedback f : filteredFeedback) {
            String userName = userDao.findById(f.getUserId()).map(User::getFullName).orElse(String.valueOf(f.getUserId()));
            String facilityName = f.getFacilityId() == null ? "" : facilityDao.findById(f.getFacilityId()).map(Facility::getName).orElse(String.valueOf(f.getFacilityId()));
            if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                model.addRow(new Object[]{f.getId(), facilityName, f.getRating(), f.getComments()});
            } else {
                model.addRow(new Object[]{f.getId(), userName, facilityName, f.getRating(), f.getComments()});
            }
        }
    }

    private void onAdd() {
        class Option { final Long id; final String label; Option(Long id, String label) { this.id = id; this.label = label; } public String toString() { return label; } }
        
        JComboBox<Option> userBox = new JComboBox<>();
        JComboBox<Option> facilityBox = new JComboBox<>();
        JComboBox<String> ratingBox = new JComboBox<>(new String[]{"1 - Poor", "2 - Fair", "3 - Good", "4 - Very Good", "5 - Excellent"});
        JTextArea comments = new JTextArea(4, 20);
        
        if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
            // For members, only show their own data and make it readonly
            userBox.addItem(new Option(currentUser.getId(), currentUser.getFullName() + " (" + currentUser.getEmail() + ")"));
            userBox.setEnabled(false);
        } else {
            for (User u : userDao.findByRole(Role.MEMBER)) {
                userBox.addItem(new Option(u.getId(), u.getFullName() + " (" + u.getEmail() + ")"));
            }
        }
        
        facilityBox.addItem(new Option(null, "(None)"));
        for (Facility f : facilityDao.findAll()) {
            facilityBox.addItem(new Option(f.getId(), f.getName()));
        }
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        userBox.setFont(inputFont);
        facilityBox.setFont(inputFont);
        ratingBox.setFont(inputFont);
        comments.setFont(inputFont);
        userBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        facilityBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        ratingBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        comments.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // User field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("User:");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(userBox, gbc);
        
        // Facility field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel facilityLabel = new JLabel("Facility (optional):");
        facilityLabel.setFont(facilityLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(facilityLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(facilityBox, gbc);
        
        // Rating field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel ratingLabel = new JLabel("Rating:");
        ratingLabel.setFont(ratingLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(ratingLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(ratingBox, gbc);
        
        // Comments field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel commentsLabel = new JLabel("Comments:");
        commentsLabel.setFont(commentsLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(commentsLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comments.setLineWrap(true);
        comments.setWrapStyleWord(true);
        formPanel.add(comments, gbc);
        
        p.add(formPanel, BorderLayout.CENTER);
        
        int res = JOptionPane.showConfirmDialog(this, p, "💬 Submit Feedback", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Feedback f = new Feedback();
                Option uSel = (Option) userBox.getSelectedItem();
                if (uSel != null) f.setUserId(uSel.id);
                Option facSel = (Option) facilityBox.getSelectedItem();
                if (facSel != null && facSel.id != null) f.setFacilityId(facSel.id);
                
                // Parse rating from dropdown selection
                String selectedRating = (String) ratingBox.getSelectedItem();
                int ratingValue = Integer.parseInt(selectedRating.split(" - ")[0]);
                f.setRating(ratingValue);
                
                f.setComments(comments.getText().trim());
                dao.create(f);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}



package ui.screens;

import dao.impl.MaintenanceDaoJdbc;
import dao.impl.FacilityDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.MaintenanceRequest;
import model.MaintenanceStatus;
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

public class MaintenancePanel extends JPanel {
    private final MaintenanceDaoJdbc dao = new MaintenanceDaoJdbc();
    private final FacilityDaoJdbc facilityDao = new FacilityDaoJdbc();
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final DefaultTableModel model = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
        public String getColumnName(int column) {
            if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                String[] userCols = {"ID","Facility","Title","Status"};
                return column < userCols.length ? userCols[column] : "";
            } else {
                String[] adminCols = {"ID","Facility","Requested By","Title","Status"};
                return column < adminCols.length ? adminCols[column] : "";
            }
        }
        public int getColumnCount() {
            return currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH) ? 4 : 5;
        }
    };
    private final User currentUser;
    private List<MaintenanceRequest> allMaintenanceRequests;
    private String currentSearchQuery = "";

	public MaintenancePanel(User user) {
		this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("🔧 Maintenance");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Manage maintenance requests and track progress");
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

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		actions.setBackground(AppColors.CARD);
		actions.setBorder(new EmptyBorder(15, 20, 15, 20));
		
		JButton add = createStyledButton("🔧 New Request", AppColors.SUCCESS);
		JButton progress = createStyledButton("⏳ Mark In Progress", AppColors.WARNING);
		JButton resolve = createStyledButton("✅ Mark Resolved", AppColors.ACCENT);
		
		actions.add(add);
		if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
			actions.add(progress);
			actions.add(resolve);
		}
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        progress.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) updateStatus(getId(row), MaintenanceStatus.IN_PROGRESS); });
        resolve.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) updateStatus(getId(row), MaintenanceStatus.RESOLVED); });

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

    private Long getId(int row) { return ((Number) model.getValueAt(row, 0)).longValue(); }

    private void refresh() {
        model.setRowCount(0);
        allMaintenanceRequests = dao.findAll();
        
        // Apply user role filtering
        if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
            allMaintenanceRequests.removeIf(r -> r.getRequestedBy() == null || !r.getRequestedBy().equals(currentUser.getId()));
        }
        
        // Apply search filtering
        List<MaintenanceRequest> filteredRequests = allMaintenanceRequests;
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredRequests = allMaintenanceRequests.stream()
                .filter(request -> {
                    try {
                        String facilityName = facilityDao.findById(request.getFacilityId()).map(Facility::getName).orElse("");
                        String requestedByName = userDao.findById(request.getRequestedBy()).map(User::getFullName).orElse("");
                        String title = request.getTitle();
                        String status = request.getStatus().name();
                        
                        return facilityName.toLowerCase().contains(query) ||
                               requestedByName.toLowerCase().contains(query) ||
                               title.toLowerCase().contains(query) ||
                               status.toLowerCase().contains(query);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        for (MaintenanceRequest r : filteredRequests) {
            String facilityName = facilityDao.findById(r.getFacilityId()).map(Facility::getName).orElse(String.valueOf(r.getFacilityId()));
            String requestedByName = userDao.findById(r.getRequestedBy()).map(User::getFullName).orElse(String.valueOf(r.getRequestedBy()));
            if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                model.addRow(new Object[]{r.getId(), facilityName, r.getTitle(), r.getStatus().name()});
            } else {
                model.addRow(new Object[]{r.getId(), facilityName, requestedByName, r.getTitle(), r.getStatus().name()});
            }
        }
    }

    private void onAdd() {
        class Option { final Long id; final String label; Option(Long id, String label) { this.id = id; this.label = label; } public String toString() { return label; } }
        
        JComboBox<Option> facilityBox = new JComboBox<>();
        JComboBox<Option> requesterBox = new JComboBox<>();
        JTextField title = new JTextField(20);
        JTextArea description = new JTextArea(4, 20);
        
        for (Facility f : facilityDao.findAll()) {
            facilityBox.addItem(new Option(f.getId(), f.getName()));
        }
        
        if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
            // For members, only show their own data and make it readonly
            requesterBox.addItem(new Option(currentUser.getId(), currentUser.getFullName() + " (" + currentUser.getEmail() + ")"));
            requesterBox.setEnabled(false);
        } else {
            for (User u : userDao.findByRole(Role.MEMBER)) {
                requesterBox.addItem(new Option(u.getId(), u.getFullName() + " (" + u.getEmail() + ")"));
            }
        }
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        facilityBox.setFont(inputFont);
        requesterBox.setFont(inputFont);
        title.setFont(inputFont);
        description.setFont(inputFont);
        facilityBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        requesterBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        title.setBorder(new EmptyBorder(8, 12, 8, 12));
        description.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Facility field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel facilityLabel = new JLabel("Facility:");
        facilityLabel.setFont(facilityLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(facilityLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(facilityBox, gbc);
        
        // Requested By field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel requesterLabel = new JLabel("Requested By:");
        requesterLabel.setFont(requesterLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(requesterLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(requesterBox, gbc);
        
        // Title field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(title, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(descLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        formPanel.add(description, gbc);
        
        p.add(formPanel, BorderLayout.CENTER);
        
        int res = JOptionPane.showConfirmDialog(this, p, "🔧 New Maintenance Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                MaintenanceRequest r = new MaintenanceRequest();
                Option fSel = (Option) facilityBox.getSelectedItem();
                Option uSel = (Option) requesterBox.getSelectedItem();
                if (fSel != null) r.setFacilityId(fSel.id);
                if (uSel != null) r.setRequestedBy(uSel.id);
                r.setTitle(title.getText().trim());
                r.setDescription(description.getText());
                dao.create(r);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStatus(Long id, MaintenanceStatus status) {
        dao.findById(id).ifPresent(r -> { r.setStatus(status); dao.update(r); refresh(); });
    }
}



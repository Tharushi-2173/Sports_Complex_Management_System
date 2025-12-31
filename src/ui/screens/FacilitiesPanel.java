package ui.screens;

import dao.impl.FacilityDaoJdbc;
import model.Facility;
import model.FacilityStatus;
import model.Role;
import model.User;
import ui.AppColors;
import ui.components.SearchPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FacilitiesPanel extends JPanel {
    private final FacilityDaoJdbc dao = new FacilityDaoJdbc();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Name","Rate","Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final User currentUser;
    private List<Facility> allFacilities;
    private String currentSearchQuery = "";

    public FacilitiesPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("🏢 Facilities");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Manage sports facilities and their availability");
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
        
        JButton add = createStyledButton("➕ Add Facility", AppColors.SUCCESS);
        JButton edit = createStyledButton("✏️ Edit Facility", AppColors.ACCENT);
        JButton remove = createStyledButton("🗑️ Delete Facility", AppColors.ERROR);
        
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            actions.add(add); 
            actions.add(edit); 
            actions.add(remove);
        }
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) onEdit(getId(row)); });
        remove.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) { dao.delete(getId(row)); refresh(); } });

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
        allFacilities = dao.findAll();
        
        // Apply search filter
        List<Facility> filteredFacilities = allFacilities;
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredFacilities = allFacilities.stream()
                .filter(facility -> 
                    facility.getName().toLowerCase().contains(query) ||
                    facility.getStatus().name().toLowerCase().contains(query) ||
                    String.valueOf(facility.getHourlyRate()).contains(query)
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        for (Facility f : filteredFacilities) {
            model.addRow(new Object[]{f.getId(), f.getName(), f.getHourlyRate(), f.getStatus().name()});
        }
    }

    private void onAdd() {
        Facility f = promptFacility(null);
        if (f != null) { dao.create(f); refresh(); }
    }

    private void onEdit(Long id) {
        Facility existing = dao.findById(id).orElse(null);
        if (existing == null) return;
        Facility updated = promptFacility(existing);
        if (updated != null) { updated.setId(id); dao.update(updated); refresh(); }
    }

    private Facility promptFacility(Facility base) {
        JTextField name = new JTextField(base != null ? base.getName() : "");
        JTextField rate = new JTextField(base != null ? String.valueOf(base.getHourlyRate()) : "");
        JComboBox<FacilityStatus> status = new JComboBox<>(FacilityStatus.values());
        if (base != null) status.setSelectedItem(base.getStatus());
        JTextArea description = new JTextArea(4, 30);
        description.setText(base != null ? base.getDescription() : "");
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(description);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        name.setFont(inputFont);
        rate.setFont(inputFont);
        description.setFont(inputFont);
        name.setBorder(new EmptyBorder(8, 12, 8, 12));
        rate.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Facility Name:");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(name, gbc);
        
        // Rate field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel rateLabel = new JLabel("Hourly Rate ($):");
        rateLabel.setFont(rateLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(rateLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(rate, gbc);
        
        // Status field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(statusLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(status, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(descLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(sp, gbc);
        
        p.add(formPanel, BorderLayout.CENTER);
        
        int res = JOptionPane.showConfirmDialog(this, p, base == null ? "➕ Add Facility" : "✏️ Edit Facility", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Facility f = new Facility();
            f.setName(name.getText().trim());
            try { f.setHourlyRate(Double.parseDouble(rate.getText().trim())); } catch (Exception ex) { f.setHourlyRate(0); }
            f.setStatus((FacilityStatus) status.getSelectedItem());
            f.setDescription(description.getText());
            return f;
        }
        return null;
    }
}



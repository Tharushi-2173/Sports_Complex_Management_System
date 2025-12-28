package ui.screens;

import dao.impl.UserDaoJdbc;
import model.Role;
import model.User;
import ui.AppColors;
import ui.components.SearchPanel;
import util.PasswordHasher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Email","Name","Role","Phone","Coach Fee"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private List<User> allUsers;
    private String currentSearchQuery = "";

    public UsersPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("👥 Users");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Manage user accounts and permissions");
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
        
        JButton add = createStyledButton("➕ Add User", AppColors.SUCCESS);
        JButton edit = createStyledButton("✏️ Edit User", AppColors.ACCENT);
        JButton remove = createStyledButton("🗑️ Delete User", AppColors.ERROR);
        
        actions.add(add); 
        actions.add(edit); 
        actions.add(remove);
        add(actions, BorderLayout.SOUTH);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) onEdit(getId(row));
        });
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { userDao.delete(getId(row)); refresh(); }
        });

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
        allUsers = userDao.findAll();
        
        // Apply search filter
        List<User> filteredUsers = allUsers;
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredUsers = allUsers.stream()
                .filter(user -> 
                    user.getEmail().toLowerCase().contains(query) ||
                    user.getFullName().toLowerCase().contains(query) ||
                    user.getRole().name().toLowerCase().contains(query) ||
                    (user.getPhone() != null && user.getPhone().toLowerCase().contains(query))
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        for (User u : filteredUsers) {
            String fee = u.getCoachFee() == null ? "" : String.format("%.2f", u.getCoachFee());
            model.addRow(new Object[]{u.getId(), u.getEmail(), u.getFullName(), u.getRole().name(), u.getPhone(), fee});
        }
    }

    private void onAdd() {
        User u = promptUser(null);
        if (u != null) {
            userDao.create(u);
            refresh();
        }
    }

    private void onEdit(Long id) {
        User existing = userDao.findById(id).orElse(null);
        if (existing == null) return;
        User updated = promptUser(existing);
        if (updated != null) {
            updated.setId(id);
            userDao.update(updated);
            refresh();
        }
    }

    private User promptUser(User base) {
        JTextField email = new JTextField(base != null ? base.getEmail() : "");
        JTextField name = new JTextField(base != null ? base.getFullName() : "");
        JComboBox<Role> role = new JComboBox<>(Role.values());
        if (base != null) role.setSelectedItem(base.getRole());
        JTextField phone = new JTextField(base != null ? base.getPhone() : "");
        JPasswordField password = new JPasswordField();
        JLabel coachFeeLabel = new JLabel("Coach Fee ($/hour):");
        JTextField coachFee = new JTextField(base != null && base.getCoachFee() != null ? String.valueOf(base.getCoachFee()) : "");
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        email.setFont(inputFont);
        name.setFont(inputFont);
        phone.setFont(inputFont);
        password.setFont(inputFont);
        coachFee.setFont(inputFont);
        email.setBorder(new EmptyBorder(8, 12, 8, 12));
        name.setBorder(new EmptyBorder(8, 12, 8, 12));
        phone.setBorder(new EmptyBorder(8, 12, 8, 12));
        password.setBorder(new EmptyBorder(8, 12, 8, 12));
        coachFee.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(emailLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(email, gbc);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(name, gbc);
        
        // Role field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(role, gbc);
        
        // Phone field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(phoneLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(phoneLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(phone, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel passwordLabel = new JLabel(base == null ? "Password:" : "Password (leave blank to keep):");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(password, gbc);
        
        // Coach Fee field
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        coachFeeLabel.setFont(coachFeeLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(coachFeeLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(coachFee, gbc);
        
        p.add(formPanel, BorderLayout.CENTER);

        boolean isCoachInit = (base != null ? base.getRole() == Role.COACH : role.getSelectedItem() == Role.COACH);
        coachFeeLabel.setVisible(isCoachInit);
        coachFee.setVisible(isCoachInit);
        role.addItemListener(e -> {
            Role r = (Role) role.getSelectedItem();
            boolean show = r == Role.COACH;
            coachFeeLabel.setVisible(show);
            coachFee.setVisible(show);
            p.revalidate();
            p.repaint();
        });
        int res = JOptionPane.showConfirmDialog(this, p, base == null ? "➕ Add User" : "✏️ Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            User u = new User();
            u.setEmail(email.getText().trim());
            u.setFullName(name.getText().trim());
            u.setRole((Role) role.getSelectedItem());
            u.setPhone(phone.getText().trim());
            char[] pw = password.getPassword();
            String pwd = pw == null ? "" : new String(pw).trim();
            if (base == null) {
                if (pwd.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password is required for new users.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                u.setPasswordHash(PasswordHasher.hashWithRandomSalt(pwd));
            } else {
                if (pwd.isEmpty()) {
                    u.setPasswordHash(base.getPasswordHash());
                } else {
                    u.setPasswordHash(PasswordHasher.hashWithRandomSalt(pwd));
                }
            }
            if (u.getRole() == Role.COACH) {
                try { String cf = coachFee.getText().trim(); u.setCoachFee(cf.isEmpty() ? null : Double.parseDouble(cf)); } catch (Exception ex) { u.setCoachFee(null); }
            } else {
                u.setCoachFee(null);
            }
            return u;
        }
        return null;
    }
}



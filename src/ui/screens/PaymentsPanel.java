package ui.screens;

import dao.impl.BookingDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.User;
import dao.impl.PaymentDaoJdbc;
import model.Payment;
import model.PaymentMethod;
import ui.AppColors;
import ui.components.SearchPanel;
import model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentsPanel extends JPanel {
    private final PaymentDaoJdbc dao = new PaymentDaoJdbc();
    private final BookingDaoJdbc bookingDao = new BookingDaoJdbc();
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final DefaultTableModel model = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
        public String getColumnName(int column) {
            if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
                String[] memberCols = {"ID","Booking","Amount","Discount","Paid At","Method","Reference"};
                return column < memberCols.length ? memberCols[column] : "";
            } else if (currentUser != null && currentUser.getRole() == Role.COACH) {
                String[] coachCols = {"ID","Booking","Amount","Discount","Paid At","Method","Reference"};
                return column < coachCols.length ? coachCols[column] : "";
            } else {
                String[] adminCols = {"ID","User","Booking","Amount","Discount","Paid At","Method","Reference"};
                return column < adminCols.length ? adminCols[column] : "";
            }
        }
        public int getColumnCount() {
            if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                return 7;
            } else {
                return 8;
            }
        }
    };
    private final User currentUser;
    private List<Payment> allPayments;
    private String currentSearchQuery = "";

    public PaymentsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("💳 Payments");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Track and manage payment records");
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
        
        JButton record = createStyledButton("💰 Record Payment", AppColors.SUCCESS);
        JButton receipt = createStyledButton("🧾 Generate Receipt", AppColors.ACCENT);
        
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            actions.add(record);
        }
        actions.add(receipt);
        add(actions, BorderLayout.SOUTH);

        record.addActionListener(e -> onRecord());
        receipt.addActionListener(e -> onReceipt(table));
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
        allPayments = dao.findAll();
        
        // Apply user role filtering
        if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
            allPayments.removeIf(p -> p.getUserId() == null || !p.getUserId().equals(currentUser.getId()));
        }
        
        // Apply search filtering
        List<Payment> filteredPayments = allPayments;
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredPayments = allPayments.stream()
                .filter(payment -> {
                    try {
                        String userName = payment.getUserId() == null ? "" : 
                            userDao.findById(payment.getUserId()).map(User::getFullName).orElse("");
                        String method = payment.getMethod().name();
                        String reference = payment.getReference() != null ? payment.getReference() : "";
                        
                        return userName.toLowerCase().contains(query) ||
                               method.toLowerCase().contains(query) ||
                               reference.toLowerCase().contains(query) ||
                               String.valueOf(payment.getAmount()).contains(query);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        for (Payment p : filteredPayments) {
            String userName = p.getUserId() == null ? "" : userDao.findById(p.getUserId()).map(User::getFullName).orElse(String.valueOf(p.getUserId()));
            bookingDao.findFacilityBookingById(p.getBookingId()).ifPresent(b -> {
                if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                    model.addRow(new Object[]{p.getId(), "#" + b.getId() + " FAC " + b.getFacilityId() + " " + b.getStartTime(), p.getAmount(), p.getDiscount(), p.getPaidAt(), p.getMethod().name(), p.getReference()});
                } else {
                    model.addRow(new Object[]{p.getId(), userName, "#" + b.getId() + " FAC " + b.getFacilityId() + " " + b.getStartTime(), p.getAmount(), p.getDiscount(), p.getPaidAt(), p.getMethod().name(), p.getReference()});
                }
            });
            bookingDao.findTrainingBookingById(p.getBookingId()).ifPresent(b -> {
                if (currentUser != null && (currentUser.getRole() == Role.MEMBER || currentUser.getRole() == Role.COACH)) {
                    model.addRow(new Object[]{p.getId(), "#" + b.getId() + " TRN " + b.getFacilityId() + " " + b.getStartTime(), p.getAmount(), p.getDiscount(), p.getPaidAt(), p.getMethod().name(), p.getReference()});
                } else {
                    model.addRow(new Object[]{p.getId(), userName, "#" + b.getId() + " TRN " + b.getFacilityId() + " " + b.getStartTime(), p.getAmount(), p.getDiscount(), p.getPaidAt(), p.getMethod().name(), p.getReference()});
                }
            });
        }
    }

    private void onRecord() {
        class Option { final Long id; final String label; Option(Long id, String label) { this.id = id; this.label = label; } public String toString() { return label; } }
        JComboBox<Option> userBox = new JComboBox<>();
        if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
            // For members, only show their own data and make it readonly
            userBox.addItem(new Option(currentUser.getId(), currentUser.getFullName() + " (" + currentUser.getEmail() + ")"));
            userBox.setEnabled(false);
        } else {
            for (User u : userDao.findAll()) {
                userBox.addItem(new Option(u.getId(), u.getFullName() + " (" + u.getEmail() + ")"));
            }
        }
        JComboBox<Option> bookingBox = new JComboBox<>();
        Runnable reloadBookings = () -> {
            bookingBox.removeAllItems();
            Option selUser = (Option) userBox.getSelectedItem();
            Long memberId = selUser != null ? selUser.id : null;
            for (Object o : bookingDao.findAll()) {
                Long id; String label; Long mId;
                if (o instanceof model.FacilityBooking) {
                    model.FacilityBooking b = (model.FacilityBooking) o;
                    id = b.getId();
                    mId = b.getMemberId();
                    label = "#" + id + " FAC " + b.getFacilityId() + " " + b.getStartTime();
                } else {
                    model.TrainingBooking b = (model.TrainingBooking) o;
                    id = b.getId();
                    mId = b.getMemberId();
                    label = "#" + id + " TRN " + b.getFacilityId() + " " + b.getStartTime();
                }
                if (memberId == null || (mId != null && mId.equals(memberId))) {
                    bookingBox.addItem(new Option(id, label));
                }
            }
        };
        reloadBookings.run();
        userBox.addActionListener(e -> reloadBookings.run());
        JTextField amount = new JTextField();
        JTextField discount = new JTextField("0");
        JComboBox<PaymentMethod> method = new JComboBox<>(PaymentMethod.values());
        JTextField reference = new JTextField();
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        amount.setFont(inputFont);
        discount.setFont(inputFont);
        reference.setFont(inputFont);
        amount.setBorder(new EmptyBorder(8, 12, 8, 12));
        discount.setBorder(new EmptyBorder(8, 12, 8, 12));
        reference.setBorder(new EmptyBorder(8, 12, 8, 12));
        
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
        
        // Booking field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel bookingLabel = new JLabel("Booking:");
        bookingLabel.setFont(bookingLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(bookingLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(bookingBox, gbc);
        
        // Amount field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel amountLabel = new JLabel("Amount ($):");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(amountLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(amount, gbc);
        
        // Discount field
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel discountLabel = new JLabel("Discount ($):");
        discountLabel.setFont(discountLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(discountLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(discount, gbc);
        
        // Method field
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel methodLabel = new JLabel("Payment Method:");
        methodLabel.setFont(methodLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(methodLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(method, gbc);
        
        // Reference field
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel refLabel = new JLabel("Reference:");
        refLabel.setFont(refLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(refLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(reference, gbc);
        
        p.add(formPanel, BorderLayout.CENTER);
        
        int res = JOptionPane.showConfirmDialog(this, p, "💰 Record Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Payment pay = new Payment();
                Option userSel = (Option) userBox.getSelectedItem();
                if (userSel != null) pay.setUserId(userSel.id);
                Option sel = (Option) bookingBox.getSelectedItem();
                if (sel != null) pay.setBookingId(sel.id);
                pay.setAmount(Double.parseDouble(amount.getText().trim()));
                pay.setDiscount(Double.parseDouble(discount.getText().trim()));
                pay.setMethod((PaymentMethod) method.getSelectedItem());
                pay.setReference(reference.getText().trim());
                pay.setPaidAt(LocalDateTime.now());
                dao.create(pay);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onReceipt(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Long id = ((Number) model.getValueAt(row, 0)).longValue();
        dao.findById(id).ifPresent(p -> {
            try {
                java.io.File f = util.ReceiptGenerator.generateTextReceipt(p);
                JOptionPane.showMessageDialog(this, "Saved receipt: " + f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}



package ui.screens;

import dao.impl.BookingDaoJdbc;
import dao.impl.FacilityDaoJdbc;
import dao.impl.UserDaoJdbc;
import model.*;
import ui.AppColors;
import ui.components.SearchPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingsPanel extends JPanel {
    private final BookingDaoJdbc bookingDao = new BookingDaoJdbc();
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    private final FacilityDaoJdbc facilityDao = new FacilityDaoJdbc();
    private final User currentUser;
    private String currentSearchQuery = "";
    private final DefaultTableModel model = new DefaultTableModel() {
        public boolean isCellEditable(int r, int c) { return false; }
        public String getColumnName(int column) {
            if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
                String[] memberCols = {"ID","Type","Facility","Coach","Start","End","Facility Fee","Coach Fee","Total Fee","Status"};
                return column < memberCols.length ? memberCols[column] : "";
            } else if (currentUser != null && currentUser.getRole() == Role.COACH) {
                String[] coachCols = {"ID","Type","Member","Facility","Start","End","Facility Fee","Coach Fee","Total Fee","Status"};
                return column < coachCols.length ? coachCols[column] : "";
            } else {
                String[] adminCols = {"ID","Type","Member","Facility","Coach","Start","End","Facility Fee","Coach Fee","Total Fee","Status"};
                return column < adminCols.length ? adminCols[column] : "";
            }
        }
        public int getColumnCount() {
            if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
                return 10;
            } else if (currentUser != null && currentUser.getRole() == Role.COACH) {
                return 10;
            } else {
                return 11;
            }
        }
    };
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BookingsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12, 12));
        setBackground(AppColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("📅 Bookings & Scheduling");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Manage facility and training bookings");
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
        
        JButton addFacility = createStyledButton("🏢 New Facility Booking", AppColors.SUCCESS);
        JButton addTraining = createStyledButton("🏃 New Training Booking", AppColors.ACCENT);
        JButton cancel = createStyledButton("❌ Cancel Booking", AppColors.ERROR);
        
        actions.add(addFacility); 
        actions.add(addTraining); 
        actions.add(cancel);
        add(actions, BorderLayout.SOUTH);

        addFacility.addActionListener(e -> onAdd(false));
        addTraining.addActionListener(e -> onAdd(true));
        cancel.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) onCancel(getId(row)); });

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
        List<Object> list = bookingDao.findAll();
        
        // Apply user role filtering
        if (currentUser != null) {
            if (currentUser.getRole() == Role.MEMBER) {
                list.removeIf(o -> {
                    if (o instanceof FacilityBooking) return ((FacilityBooking) o).getMemberId() == null || !((FacilityBooking) o).getMemberId().equals(currentUser.getId());
                    if (o instanceof TrainingBooking) return ((TrainingBooking) o).getMemberId() == null || !((TrainingBooking) o).getMemberId().equals(currentUser.getId());
                    return true;
                });
            } else if (currentUser.getRole() == Role.COACH) {
                list.removeIf(o -> {
                    if (o instanceof FacilityBooking) return ((FacilityBooking) o).getCoachId() == null || !((FacilityBooking) o).getCoachId().equals(currentUser.getId());
                    if (o instanceof TrainingBooking) return ((TrainingBooking) o).getCoachId() == null || !((TrainingBooking) o).getCoachId().equals(currentUser.getId());
                    return true;
                });
            }
        }
        
        // Apply search filtering
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            list = list.stream()
                .filter(booking -> {
                    try {
                        String memberName = "";
                        String facilityName = "";
                        String coachName = "";
                        String bookingType = "";
                        
                        if (booking instanceof FacilityBooking) {
                            FacilityBooking fb = (FacilityBooking) booking;
                            memberName = userDao.findById(fb.getMemberId()).map(User::getFullName).orElse("");
                            facilityName = facilityDao.findById(fb.getFacilityId()).map(Facility::getName).orElse("");
                            coachName = fb.getCoachId() != null ? userDao.findById(fb.getCoachId()).map(User::getFullName).orElse("") : "";
                            bookingType = "FACILITY";
                        } else if (booking instanceof TrainingBooking) {
                            TrainingBooking tb = (TrainingBooking) booking;
                            memberName = userDao.findById(tb.getMemberId()).map(User::getFullName).orElse("");
                            facilityName = facilityDao.findById(tb.getFacilityId()).map(Facility::getName).orElse("");
                            coachName = tb.getCoachId() != null ? userDao.findById(tb.getCoachId()).map(User::getFullName).orElse("") : "";
                            bookingType = "TRAINING";
                        }
                        
                        return memberName.toLowerCase().contains(query) ||
                               facilityName.toLowerCase().contains(query) ||
                               coachName.toLowerCase().contains(query) ||
                               bookingType.toLowerCase().contains(query);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        for (Object o : list) {
            if (o instanceof FacilityBooking) {
                FacilityBooking b = (FacilityBooking) o;
				String memberName = userDao.findById(b.getMemberId()).map(User::getFullName).orElse(String.valueOf(b.getMemberId()));
				String facilityName = facilityDao.findById(b.getFacilityId()).map(Facility::getName).orElse(String.valueOf(b.getFacilityId()));
				String coachName = b.getCoachId() == null ? "" : userDao.findById(b.getCoachId()).map(User::getFullName).orElse(String.valueOf(b.getCoachId()));
				if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
					model.addRow(new Object[]{b.getId(), "FACILITY", facilityName, coachName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				} else if (currentUser != null && currentUser.getRole() == Role.COACH) {
					model.addRow(new Object[]{b.getId(), "FACILITY", memberName, facilityName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				} else {
					model.addRow(new Object[]{b.getId(), "FACILITY", memberName, facilityName, coachName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				}
            } else if (o instanceof TrainingBooking) {
                TrainingBooking b = (TrainingBooking) o;
				String memberName = userDao.findById(b.getMemberId()).map(User::getFullName).orElse(String.valueOf(b.getMemberId()));
				String facilityName = facilityDao.findById(b.getFacilityId()).map(Facility::getName).orElse(String.valueOf(b.getFacilityId()));
				String coachName = b.getCoachId() == null ? "" : userDao.findById(b.getCoachId()).map(User::getFullName).orElse(String.valueOf(b.getCoachId()));
				if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
					model.addRow(new Object[]{b.getId(), "TRAINING", facilityName, coachName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				} else if (currentUser != null && currentUser.getRole() == Role.COACH) {
					model.addRow(new Object[]{b.getId(), "TRAINING", memberName, facilityName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				} else {
					model.addRow(new Object[]{b.getId(), "TRAINING", memberName, facilityName, coachName, FMT.format(b.getStartTime()), FMT.format(b.getEndTime()), String.format("%.2f", b.getFacilityFee()), String.format("%.2f", b.getCoachFee()), String.format("%.2f", b.getTotalFee()), b.getStatus().name()});
				}
            }
        }
    }

    private void onAdd(boolean training) {
		class Option {
			final Long id;
			final String label;
			Option(Long id, String label) { this.id = id; this.label = label; }
			public String toString() { return label; }
		}

		JComboBox<Option> memberBox = new JComboBox<>();
		JComboBox<Option> facilityBox = new JComboBox<>();
        JComboBox<Option> coachBox = new JComboBox<>();

		if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
			// For members, only show their own data and make it readonly
			memberBox.addItem(new Option(currentUser.getId(), currentUser.getFullName() + " (" + currentUser.getEmail() + ")"));
			memberBox.setEnabled(false);
		} else {
			for (User u : userDao.findByRole(Role.MEMBER)) {
				memberBox.addItem(new Option(u.getId(), u.getFullName() + " (" + u.getEmail() + ")"));
			}
		}
		for (Facility f : facilityDao.findAll()) {
			facilityBox.addItem(new Option(f.getId(), f.getName()));
		}
        if (training) {
            coachBox.addItem(new Option(null, "(None)"));
            for (User c : userDao.findByRole(Role.COACH)) {
                String fee = c.getCoachFee() == null ? "" : String.format(" - %.2f/hr", c.getCoachFee());
                coachBox.addItem(new Option(c.getId(), c.getFullName() + " (" + c.getEmail() + ")" + fee));
            }
        }
        JTextField date = new JTextField(java.time.LocalDate.now().toString());
        JTextField startTime = new JTextField("09:00");
        JTextField endTime = new JTextField("10:00");
        
        // Style the input fields
        Font inputFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        date.setFont(inputFont);
        startTime.setFont(inputFont);
        endTime.setFont(inputFont);
        date.setBorder(new EmptyBorder(8, 12, 8, 12));
        startTime.setBorder(new EmptyBorder(8, 12, 8, 12));
        endTime.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Member field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel memberLabel = new JLabel("Member:");
        memberLabel.setFont(memberLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(memberLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(memberBox, gbc);
        
        // Facility field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel facilityLabel = new JLabel("Facility:");
        facilityLabel.setFont(facilityLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(facilityLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(facilityBox, gbc);
        
        // Coach field (if training)
        if (training) {
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            JLabel coachLabel = new JLabel("Coach (optional):");
            coachLabel.setFont(coachLabel.getFont().deriveFont(Font.BOLD, 13f));
            formPanel.add(coachLabel, gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(coachBox, gbc);
        }
        
        // Date field
        gbc.gridx = 0; gbc.gridy = training ? 3 : 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(dateLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(date, gbc);
        
        // Start Time field
        gbc.gridx = 0; gbc.gridy = training ? 4 : 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel startLabel = new JLabel("Start Time (HH:mm):");
        startLabel.setFont(startLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(startLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(startTime, gbc);
        
        // End Time field
        gbc.gridx = 0; gbc.gridy = training ? 5 : 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel endLabel = new JLabel("End Time (HH:mm):");
        endLabel.setFont(endLabel.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(endLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(endTime, gbc);

        p.add(formPanel, BorderLayout.CENTER);
        
        // Fee display panel
        JPanel feePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        feePanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JLabel facilityFeeLbl = new JLabel("Facility Fee: $0.00");
        facilityFeeLbl.setFont(facilityFeeLbl.getFont().deriveFont(Font.BOLD, 13f));
        facilityFeeLbl.setForeground(AppColors.TEXT_SECONDARY);
        feePanel.add(facilityFeeLbl);
        
        JLabel coachFeeLbl = new JLabel("Coach Fee: $0.00");
        coachFeeLbl.setFont(coachFeeLbl.getFont().deriveFont(Font.BOLD, 13f));
        coachFeeLbl.setForeground(AppColors.TEXT_SECONDARY);
        if (training) feePanel.add(coachFeeLbl);
        
        JLabel totalFeeLbl = new JLabel("Total Fee: $0.00");
        totalFeeLbl.setFont(totalFeeLbl.getFont().deriveFont(Font.BOLD, 14f));
        totalFeeLbl.setForeground(AppColors.SUCCESS);
        feePanel.add(totalFeeLbl);
        
        p.add(feePanel, BorderLayout.SOUTH);

		Runnable updateFees = () -> {
			try {
				Option fOpt = (Option) facilityBox.getSelectedItem();
				Option cOpt = training ? (Option) coachBox.getSelectedItem() : null;
				LocalDate d = LocalDate.parse(date.getText().trim());
				LocalTime st = LocalTime.parse(startTime.getText().trim());
				LocalTime et = LocalTime.parse(endTime.getText().trim());
				LocalDateTime s = LocalDateTime.of(d, st);
				LocalDateTime e = LocalDateTime.of(d, et);
				double minutes = java.time.Duration.between(s, e).toMinutes();
				double hours = Math.max(0, minutes / 60.0);
				double hourlyRate = 0.0;
				if (fOpt != null) {
					Facility fac = facilityDao.findById(fOpt.id).orElse(null);
					if (fac != null) hourlyRate = fac.getHourlyRate();
				}
				double facilityFeePerHour = Math.round(hourlyRate * 100.0) / 100.0;
				double coachFee = 0.0;
				if (training && cOpt != null && cOpt.id != null) {
					Double coachRate = userDao.findById(cOpt.id).map(User::getCoachFee).orElse(null);
					if (coachRate != null) coachFee = Math.round(coachRate * hours * 100.0) / 100.0; else coachFee = Math.round((facilityFeePerHour * hours * 0.25) * 100.0) / 100.0;
				}
				double totalFee = Math.round((facilityFeePerHour * hours + coachFee) * 100.0) / 100.0;
				facilityFeeLbl.setText("Facility Fee: $" + String.format("%.2f", facilityFeePerHour));
				if (training) coachFeeLbl.setText("Coach Fee: $" + String.format("%.2f", coachFee));
				totalFeeLbl.setText("Total Fee: $" + String.format("%.2f", totalFee));
			} catch (Exception ignore) { }
		};

		facilityBox.addActionListener(e -> updateFees.run());
		if (training) coachBox.addActionListener(e -> updateFees.run());
		DocumentListener doc = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { updateFees.run(); }
			public void removeUpdate(DocumentEvent e) { updateFees.run(); }
			public void changedUpdate(DocumentEvent e) { updateFees.run(); }
		};
		date.getDocument().addDocumentListener(doc);
		startTime.getDocument().addDocumentListener(doc);
		endTime.getDocument().addDocumentListener(doc);

		updateFees.run();
        int res = JOptionPane.showConfirmDialog(this, p, training ? "🏃 New Training Booking" : "🏢 New Facility Booking", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
				Option mOpt = (Option) memberBox.getSelectedItem();
				Option fOpt = (Option) facilityBox.getSelectedItem();
				Option cOpt = (Option) coachBox.getSelectedItem();
				Long m = mOpt != null ? mOpt.id : null;
				Long f = fOpt != null ? fOpt.id : null;
                Long c = cOpt != null ? cOpt.id : null;
                LocalDate d = LocalDate.parse(date.getText().trim());
                LocalTime st = LocalTime.parse(startTime.getText().trim());
                LocalTime et = LocalTime.parse(endTime.getText().trim());
                LocalDateTime s = LocalDateTime.of(d, st);
                LocalDateTime e = LocalDateTime.of(d, et);

				if (m == null || f == null) {
					JOptionPane.showMessageDialog(this, "Please select a member and a facility.", "Validation", JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (!e.isAfter(s)) {
					JOptionPane.showMessageDialog(this, "End time must be after start time.", "Validation", JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (bookingDao.existsOverlap(f, s, e)) {
					JOptionPane.showMessageDialog(this, "Selected time overlaps an existing confirmed booking for this facility.", "Scheduling Conflict", JOptionPane.WARNING_MESSAGE);
					return;
				}

                Facility fac = facilityDao.findById(f).orElse(null);
                double hourlyRate = fac != null ? fac.getHourlyRate() : 0.0;
                double minutes = java.time.Duration.between(s, e).toMinutes();
                double hours = Math.max(0, minutes / 60.0);
                // Facility fee stored as hourly rate; coach fee stored as additive amount
                double facilityFee = Math.round(hourlyRate * 100.0) / 100.0;
                double coachFee = 0.0;
                if (training) {
                    // Prefer coach's own fee (per hour) multiplied by hours; fallback to 25% policy
                    Double coachRate = null;
                    if (c != null) coachRate = userDao.findById(c).map(User::getCoachFee).orElse(null);
                    if (coachRate != null) {
                        coachFee = Math.max(0, Math.round((coachRate * hours) * 100.0) / 100.0);
                    } else {
                        double base = facilityFee * hours;
                        coachFee = Math.max(0, Math.round((base * 0.25) * 100.0) / 100.0);
                    }
                }
                double totalFee = Math.round((facilityFee * hours + coachFee) * 100.0) / 100.0;

                if (training) {
                    TrainingBooking b = new TrainingBooking();
                    b.setMemberId(m); b.setFacilityId(f); b.setCoachId(c); b.setStartTime(s); b.setEndTime(e);
                    b.setFacilityFee(facilityFee); b.setCoachFee(coachFee); b.setTotalFee(totalFee);
                    bookingDao.createTrainingBooking(b);
                } else {
                    FacilityBooking b = new FacilityBooking();
                    b.setMemberId(m); b.setFacilityId(f); b.setCoachId(c); b.setStartTime(s); b.setEndTime(e);
                    b.setFacilityFee(facilityFee); b.setCoachFee(0.0); b.setTotalFee(facilityFee);
                    bookingDao.createFacilityBooking(b);
                }
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onCancel(Long bookingId) {
        try {
            new BookingDaoJdbc().updateStatus(bookingId, BookingStatus.CANCELLED);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to cancel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



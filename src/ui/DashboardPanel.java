package ui;

import model.Role;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    public interface LogoutHandler { void onLogout(); }

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel content = new JPanel(contentLayout);
    private JButton activeNavButton;

    public DashboardPanel(User user, LogoutHandler logoutHandler) {
        setLayout(new BorderLayout());
        setBackground(AppColors.BG);

        add(buildHeader(user, logoutHandler), BorderLayout.NORTH);
        JPanel sidebar = buildSidebar(user, logoutHandler);
        add(sidebar, BorderLayout.WEST);

        content.setBackground(AppColors.BG);
        // Use the enhanced dashboard with real data
        ui.screens.DashboardPanel home = new ui.screens.DashboardPanel();
        content.add(home, "home");
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeader(User user, LogoutHandler logoutHandler) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 60));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        JLabel icon = new JLabel("🏢");
        icon.setFont(icon.getFont().deriveFont(20f));
        icon.setBorder(new EmptyBorder(0, 0, 0, 10));
        leftPanel.add(icon);
        
        JLabel title = new JLabel("Sports Complex Management System");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        leftPanel.add(title);
        
        header.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(userIcon.getFont().deriveFont(16f));
        userIcon.setBorder(new EmptyBorder(0, 0, 0, 8));
        rightPanel.add(userIcon);
        
        JLabel info = new JLabel(user.getFullName() + " • " + user.getRole().name());
        info.setForeground(Color.WHITE);
        info.setFont(info.getFont().deriveFont(Font.BOLD, 14f));
        rightPanel.add(info);
        
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar(User user, LogoutHandler logoutHandler) {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(AppColors.PRIMARY_DARK);
        side.setBorder(new EmptyBorder(20, 16, 20, 16));
        side.setPreferredSize(new Dimension(220, 0));

        JLabel heading = new JLabel("SCMS");
        heading.setForeground(Color.WHITE);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(heading);
        side.add(Box.createVerticalStrut(20));

        side.add(navButton("Dashboard", "home", () -> contentLayout.show(content, "home")));

        if (user.getRole() == Role.ADMIN) {
            side.add(sectionLabel("Management"));
            side.add(navButton("Users", "users", () -> { ensure("users", new ui.screens.UsersPanel()); contentLayout.show(content, "users"); }));
            side.add(navButton("Facilities", "facilities", () -> { ensure("facilities", new ui.screens.FacilitiesPanel(user)); contentLayout.show(content, "facilities"); }));
            side.add(navButton("Bookings", "bookings", () -> { ensure("bookings", new ui.screens.BookingsPanel(user)); contentLayout.show(content, "bookings"); }));
            side.add(navButton("Payments", "payments", () -> { ensure("payments", new ui.screens.PaymentsPanel(user)); contentLayout.show(content, "payments"); })); 
            side.add(navButton("Maintenance", "maintenance", () -> { ensure("maintenance", new ui.screens.MaintenancePanel(user)); contentLayout.show(content, "maintenance"); }));
            side.add(navButton("Feedback", "feedback", () -> { ensure("feedback", new ui.screens.FeedbackPanel(user)); contentLayout.show(content, "feedback"); })); 
           
        }

        if (user.getRole() == Role.COACH) {
            side.add(sectionLabel("Workspace"));
            side.add(navButton("Facilities", "facilities", () -> { ensure("facilities", new ui.screens.FacilitiesPanel(user)); contentLayout.show(content, "facilities"); }));
            side.add(navButton("Bookings", "bookings", () -> { ensure("bookings", new ui.screens.BookingsPanel(user)); contentLayout.show(content, "bookings"); }));
            side.add(navButton("Maintenance", "maintenance", () -> { ensure("maintenance", new ui.screens.MaintenancePanel(user)); contentLayout.show(content, "maintenance"); }));
            side.add(navButton("Feedback", "feedback", () -> { ensure("feedback", new ui.screens.FeedbackPanel(user)); contentLayout.show(content, "feedback"); })); 
        }

        if (user.getRole() == Role.MEMBER) {
            side.add(sectionLabel("Workspace"));
            side.add(navButton("Facilities", "facilities", () -> { ensure("facilities", new ui.screens.FacilitiesPanel(user)); contentLayout.show(content, "facilities"); }));
            side.add(navButton("Bookings", "bookings", () -> { ensure("bookings", new ui.screens.BookingsPanel(user)); contentLayout.show(content, "bookings"); }));
            side.add(navButton("Payments", "payments", () -> { ensure("payments", new ui.screens.PaymentsPanel(user)); contentLayout.show(content, "payments"); })); 
            side.add(navButton("Maintenance", "maintenance", () -> { ensure("maintenance", new ui.screens.MaintenancePanel(user)); contentLayout.show(content, "maintenance"); }));
            side.add(navButton("Feedback", "feedback", () -> { ensure("feedback", new ui.screens.FeedbackPanel(user)); contentLayout.show(content, "feedback"); })); 
        }
       
        side.add(Box.createVerticalGlue());
        side.add(Box.createVerticalStrut(20));
        
        JButton logout = new JButton("🚪 Logout");
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.setBackground(AppColors.ERROR);
        logout.setForeground(Color.WHITE);
        logout.setFont(logout.getFont().deriveFont(Font.BOLD, 14f));
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(8, 16, 8, 16));
        logout.addActionListener(e -> logoutHandler.onLogout());
        side.add(logout);
        return side;
    }

    private JButton navButton(String text, String key, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setBackground(AppColors.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setToolTipText(text);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (b != activeNavButton) {
                    b.setBackground(AppColors.PRIMARY_LIGHT);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (b != activeNavButton) {
                    b.setBackground(AppColors.PRIMARY);
                }
            }
        });
        
        b.addActionListener(e -> {
            setActiveNav(b);
            action.run();
        });
        return b;
    }

    private void setActiveNav(JButton b) {
        if (activeNavButton != null) {
            activeNavButton.setBackground(AppColors.PRIMARY);
        }
        activeNavButton = b;
        activeNavButton.setBackground(AppColors.ACCENT);
    }

    private JComponent sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(10, 6, 4, 6));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        return l;
    }

    private Component ensure(String key, Component comp) {
        for (Component c : content.getComponents()) {
            if (key.equals(c.getName())) {
                return c;
            }
        }
        comp.setName(key);
        content.add(comp, key);
        return comp;
    }

}



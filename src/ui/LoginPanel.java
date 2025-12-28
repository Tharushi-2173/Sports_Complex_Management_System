package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    public interface LoginHandler { void onLogin(String email, String password); }

    public LoginPanel(LoginHandler handler) {
        setLayout(new GridBagLayout());
        setBackground(AppColors.BG);
        
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.CARD);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(450, 600));
        
        // Add subtle shadow effect
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER_LIGHT, 1),
            new EmptyBorder(39, 49, 39, 49)
        ));

        // Header section
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(AppColors.CARD);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        GridBagConstraints headerGc = new GridBagConstraints();
        headerGc.insets = new Insets(10, 10, 10, 10);
        
        JLabel icon = new JLabel("🏢");
        icon.setFont(icon.getFont().deriveFont(56f));
        icon.setForeground(AppColors.PRIMARY);
        headerGc.gridx = 0; headerGc.gridy = 0; headerGc.anchor = GridBagConstraints.CENTER;
        headerPanel.add(icon, headerGc);
        
        JLabel title = new JLabel("Sports Complex Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(AppColors.TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        headerGc.gridx = 0; headerGc.gridy = 1;
        headerPanel.add(title, headerGc);
        
        JLabel subtitle = new JLabel("Please sign in to your account");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(AppColors.TEXT_SECONDARY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerGc.gridx = 0; headerGc.gridy = 2;
        headerPanel.add(subtitle, headerGc);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Form section
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(AppColors.CARD);
        formPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(15, 0, 15, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        
        // Email field
        gc.gridx = 0; gc.gridy = 0;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(emailLabel.getFont().deriveFont(Font.BOLD, 13f));
        emailLabel.setForeground(AppColors.TEXT_PRIMARY);
        formPanel.add(emailLabel, gc);
        
        gc.gridy++;
        JTextField email = new JTextField(20);
        email.setFont(email.getFont().deriveFont(14f));
        email.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        email.setBackground(AppColors.BG_DARK);
        email.setForeground(AppColors.TEXT_PRIMARY);
        email.setToolTipText("Enter your email address");
        formPanel.add(email, gc);

        // Password field
        gc.gridy++;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD, 13f));
        passwordLabel.setForeground(AppColors.TEXT_PRIMARY);
        formPanel.add(passwordLabel, gc);
        
        gc.gridy++;
        JPasswordField password = new JPasswordField(20);
        password.setFont(password.getFont().deriveFont(14f));
        password.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        password.setBackground(AppColors.BG_DARK);
        password.setForeground(AppColors.TEXT_PRIMARY);
        password.setToolTipText("Enter your password");
        formPanel.add(password, gc);

        // Login button
        gc.gridy++;
        gc.insets = new Insets(25, 0, 15, 0);
        JButton login = new JButton("🔐 Sign In");
        login.setFont(login.getFont().deriveFont(Font.BOLD, 16f));
        login.setBackground(AppColors.PRIMARY);
        login.setForeground(Color.WHITE);
        login.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.PRIMARY_DARK, 1),
            new EmptyBorder(14, 20, 14, 20)
        ));
        login.setFocusPainted(false);
        login.setCursor(new Cursor(Cursor.HAND_CURSOR));
        login.setToolTipText("Click to sign in to your account");
        
        // Add hover effect
        login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                login.setBackground(AppColors.PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                login.setBackground(AppColors.PRIMARY);
            }
        });
        
        login.addActionListener(e -> handler.onLogin(email.getText().trim(), new String(password.getPassword())));
        formPanel.add(login, gc);
        
        // Add Enter key support for login
        KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");
        Action loginAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.onLogin(email.getText().trim(), new String(password.getPassword()));
            }
        };
        
        email.getInputMap().put(enterKey, "login");
        email.getActionMap().put("login", loginAction);
        password.getInputMap().put(enterKey, "login");
        password.getActionMap().put("login", loginAction);
        
        card.add(formPanel, BorderLayout.CENTER);

        // Center the card
        GridBagConstraints mainGc = new GridBagConstraints();
        mainGc.anchor = GridBagConstraints.CENTER;
        add(card, mainGc);
    }
}



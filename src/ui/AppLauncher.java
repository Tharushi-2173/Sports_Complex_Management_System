package ui;

import dao.impl.UserDaoJdbc;
import model.User;
import service.impl.AuthServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sports Complex Management System");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1100, 700));
            frame.setLocationRelativeTo(null);
            showLogin(frame);
            frame.setVisible(true);
        });
    }

    private static void showLogin(JFrame frame) {
        AuthServiceImpl auth = new AuthServiceImpl(new UserDaoJdbc());
        LoginPanel login = new LoginPanel((email, password) -> {
            Optional<User> user = auth.login(email, password);
            if (user.isPresent()) {
                showDashboard(frame, user.get());
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.setContentPane(login);
        frame.revalidate();
    }

    private static void showDashboard(JFrame frame, User user) {
        DashboardPanel dashboard = new DashboardPanel(user, () -> showLogin(frame));
        frame.setContentPane(dashboard);
        frame.revalidate();
    }
}



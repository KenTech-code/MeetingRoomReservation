import javax.swing.*;
import java.awt.*;

public class MainMenuFrame extends JFrame {
    public MainMenuFrame(String username, String role) {
        setTitle("Main Menu - " + role.toUpperCase());
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.setBackground(new Color(245, 245, 245));

        JLabel welcome = new JLabel("Welcome, " + username + " (" + role + ")", JLabel.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(welcome);

        if (role.equals("employee")) {
            addStyledButton(panel, "Check Room Availability", () -> {
                dispose();
                new AvailabilityFrame(username);
            });

            addStyledButton(panel, "Book a Room", () -> {
                dispose();
                new BookingFrame(username);
            });

            addStyledButton(panel, "Edit / Cancel Booking", () -> {
                dispose();
                new EditCancelFrame(username);
            });
        } else if (role.equals("admin")) {
            addStyledButton(panel, "View All Bookings", () -> {
                dispose();
                new AdminFrame();
            });

            addStyledButton(panel, "Manage Rooms", () -> {
                dispose();
                new RoomManagementFrame();
            });
            
            addStyledButton(panel, "Register User", () -> {
                dispose();
                new RegisterUserFrame();
            });
            
            addStyledButton(panel, "Manage Users", () -> {
                dispose();
                new ManageUsersFrame();
            });
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(178, 34, 34)); // firebrick red
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame(); // Return to login
        });

        panel.add(logoutBtn);
        add(panel);
        getContentPane().setBackground(new Color(245, 245, 245));
        setVisible(true);
    }

    private void addStyledButton(JPanel panel, String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(70, 130, 180)); // steel blue
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.addActionListener(e -> action.run());
        panel.add(btn);
    }
}

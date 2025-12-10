import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    JTextField userField;
    JPasswordField passField;

    public LoginFrame() {
        setTitle("Login - Meeting Room System");
        setSize(350, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(userLabel);

        userField = new JTextField();
        panel.add(userField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(passLabel);

        passField = new JPasswordField();
        panel.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(70, 130, 180)); // steel blue
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBackground(new Color(178, 34, 34)); // firebrick
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn);

        add(panel);
        getContentPane().setBackground(new Color(245, 245, 245)); // soft gray
        setVisible(true);
    }

    private void handleLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login successful as " + role + "!");
                dispose();
                new MainMenuFrame(username, role);  // Open the correct menu
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

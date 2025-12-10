import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterUserFrame extends JFrame {
    JTextField usernameField;
    JPasswordField passwordField;
    JComboBox<String> roleBox;

    public RegisterUserFrame() {
        setTitle("Register New User");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"employee", "admin"});
        add(roleBox);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(46, 139, 87));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.addActionListener(e -> registerUser());
        add(registerBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(70, 130, 180));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame("admin", "admin");
        });
        add(backBtn);

        setVisible(true);
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User registered successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
            } else {
                e.printStackTrace();
            }
        }
    }
}

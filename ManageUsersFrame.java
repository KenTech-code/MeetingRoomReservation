import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageUsersFrame extends JFrame {
    JComboBox<String> userDropdown;
    JPasswordField passwordField;
    JComboBox<String> roleDropdown;
    JTextField searchField;
    Vector<String> allUsers = new Vector<>();

    public ManageUsersFrame() {
        setTitle("Manage Users (Admin)");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        add(new JLabel("Search Username:"));
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterUsers();
            }
        });
        add(searchField);

        add(new JLabel("Select User:"));
        userDropdown = new JComboBox<>();
        loadUsers();
        add(userDropdown);

        add(new JLabel("New Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("New Role:"));
        roleDropdown = new JComboBox<>(new String[]{"employee", "admin"});
        add(roleDropdown);

        JButton updateBtn = new JButton("Update User");
        updateBtn.setBackground(new Color(70, 130, 180));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        updateBtn.addActionListener(e -> updateUser());
        add(updateBtn);

        JButton deleteBtn = new JButton("Delete User");
        deleteBtn.setBackground(new Color(178, 34, 34));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
        deleteBtn.addActionListener(e -> deleteUser());
        add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(60, 179, 113));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadUsers();
        });
        add(refreshBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(105, 105, 105));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame("admin", "admin");
        });
        add(backBtn);

        setVisible(true);
    }

    private void loadUsers() {
        allUsers.clear();
        userDropdown.removeAllItems();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username FROM users")) {
            while (rs.next()) {
                String username = rs.getString("username");
                allUsers.add(username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (String user : allUsers) {
            userDropdown.addItem(user);
        }
    }

    private void filterUsers() {
        String query = searchField.getText().toLowerCase();
        userDropdown.removeAllItems();
        for (String user : allUsers) {
            if (user.toLowerCase().contains(query)) {
                userDropdown.addItem(user);
            }
        }
    }

    private void updateUser() {
        String username = (String) userDropdown.getSelectedItem();
        if (username == null) return;

        String newPassword = new String(passwordField.getPassword());
        String newRole = (String) roleDropdown.getSelectedItem();

        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password=?, role=? WHERE username=?")) {
            stmt.setString(1, newPassword);
            stmt.setString(2, newRole);
            stmt.setString(3, username);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteUser() {
        String username = (String) userDropdown.getSelectedItem();
        if (username == null || username.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, "Cannot delete default admin.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username=?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User deleted.");
            allUsers.remove(username);
            loadUsers();  // Reload to reflect deletion
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

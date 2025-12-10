import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize SQLite database tables and default admin user
        DatabaseManager.initializeDatabase();

        // Start with the login screen
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}

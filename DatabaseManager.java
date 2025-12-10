import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:meetingrooms.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT, role TEXT)");

            // Rooms table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (room_name TEXT PRIMARY KEY, capacity INTEGER)");

            // Bookings table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT, " +
                    "room_name TEXT, " +
                    "date TEXT, " +
                    "from_time TEXT, " +
                    "to_time TEXT, " +
                    "people INTEGER, " +
                    "description TEXT)");

            // Add missing columns if bookings table was created earlier with only 'time'
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "bookings", "from_time");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE bookings ADD COLUMN from_time TEXT");
            }

            rs = meta.getColumns(null, null, "bookings", "to_time");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE bookings ADD COLUMN to_time TEXT");
            }

            rs = meta.getColumns(null, null, "bookings", "people");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE bookings ADD COLUMN people INTEGER");
            }

            // Remove legacy column 'time' if needed (you can manually drop it in DB tool, as SQLite doesn’t support DROP COLUMN)

            // Insert default admin if not present
            rs = stmt.executeQuery("SELECT * FROM users WHERE username='admin'");
            if (!rs.next()) {
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin')");
                stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('employee1', 'emp123', 'employee')");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

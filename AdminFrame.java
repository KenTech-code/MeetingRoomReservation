import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminFrame extends JFrame {
    JTextArea bookingsArea;
    JTextField deleteField;

    public AdminFrame() {
        setTitle("All Bookings (Admin View)");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        bookingsArea = new JTextArea();
        bookingsArea.setEditable(false);
        bookingsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(bookingsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Current Bookings"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));

        bottomPanel.add(new JLabel("Delete Booking ID:"));
        deleteField = new JTextField(5);
        bottomPanel.add(deleteField);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(178, 34, 34));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
        deleteBtn.addActionListener(e -> deleteBooking());
        bottomPanel.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(46, 139, 87));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> loadBookings());
        bottomPanel.add(refreshBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(70, 130, 180));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame("admin", "admin");
        });
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);
        loadBookings();
        setVisible(true);
    }

    private void loadBookings() {
        bookingsArea.setText("");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bookings")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String user = rs.getString("username");
                String room = rs.getString("room_name");
                String date = rs.getString("date");
                String from = rs.getString("from_time");
                String to = rs.getString("to_time");
                String desc = rs.getString("description");

                bookingsArea.append(String.format("ID: %-3d  User: %-10s  Room: %-25s  Date: %-10s  Time: %s to %s  Desc: %s\n",
                        id, user, room, date, from, to, desc));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteBooking() {
        try {
            int id = Integer.parseInt(deleteField.getText().trim());
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE id=?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Booking deleted.");
                loadBookings();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid ID.");
        }
    }
}

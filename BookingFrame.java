import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.jdatepicker.impl.*;

public class BookingFrame extends JFrame {
    JComboBox<String> timeBox, toTimeBox, roomBox;
    JTextField descriptionField, capacityField;
    JDatePickerImpl datePicker;

    public BookingFrame(String username) {
        setTitle("Book a Room");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("From Time:"));
        timeBox = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"});
        add(timeBox);

        add(new JLabel("To Time:"));
        toTimeBox = new JComboBox<>(new String[]{"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"});
        add(toTimeBox);

        add(new JLabel("Date:"));
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        add(datePicker);

        add(new JLabel("Room:"));
        roomBox = new JComboBox<>();
        add(roomBox);

        add(new JLabel("Number of People:"));
        capacityField = new JTextField();
        add(capacityField);

        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        // Room reloading
        timeBox.addActionListener(e -> loadRooms());
        toTimeBox.addActionListener(e -> loadRooms());
        datePicker.addActionListener(e -> loadRooms());

        JButton bookBtn = new JButton("Book Room");
        bookBtn.setBackground(new Color(46, 139, 87));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Arial", Font.BOLD, 14));
        bookBtn.addActionListener(e -> bookRoom(username));
        add(bookBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(70, 130, 180));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame(username, "employee");
        });
        add(backBtn);

        setVisible(true);
    }

    private void loadRooms() {
        roomBox.removeAllItems();
        String from = (String) timeBox.getSelectedItem();
        String to = (String) toTimeBox.getSelectedItem();
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();

        if (from == null || to == null || selectedDate == null) return;

        String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM rooms WHERE room_name NOT IN (" +
                             "SELECT room_name FROM bookings WHERE date=? AND " +
                             "((from_time <= ? AND to_time > ?) OR (from_time < ? AND to_time >= ?)))")) {
            stmt.setString(1, date);
            stmt.setString(2, from);
            stmt.setString(3, from);
            stmt.setString(4, to);
            stmt.setString(5, to);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomBox.addItem(rs.getString("room_name") + " (Capacity: " + rs.getInt("capacity") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bookRoom(String username) {
        String roomEntry = (String) roomBox.getSelectedItem();
        String from = (String) timeBox.getSelectedItem();
        String to = (String) toTimeBox.getSelectedItem();
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
        String desc = descriptionField.getText().trim();
        String capText = capacityField.getText().trim();

        if (roomEntry == null || roomEntry.isEmpty() || from == null || to == null || selectedDate == null || capText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields except description are required.");
            return;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        int requestedCap;
        try {
            requestedCap = Integer.parseInt(capText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number of people.");
            return;
        }

        if (requestedCap <= 0) {
            JOptionPane.showMessageDialog(this, "Number of people must be more than 0.");
            return;
        }

        if (to.compareTo(from) <= 0) {
            JOptionPane.showMessageDialog(this, "To Time must be after From Time.");
            return;
        }

        if (from.compareTo("09:00") < 0 || to.compareTo("16:00") > 0) {
            JOptionPane.showMessageDialog(this, "Bookings allowed only between 09:00 and 16:00.");
            return;
        }

        try {
            String roomName = roomEntry.split(" \\(")[0];
            int maxCap = Integer.parseInt(roomEntry.split("Capacity: ")[1].replace(")", ""));

            if (requestedCap > maxCap) {
                JOptionPane.showMessageDialog(this, "Capacity exceeded! Max allowed: " + maxCap);
                return;
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(
                         "SELECT * FROM bookings WHERE room_name=? AND date=? AND " +
                                 "((from_time <= ? AND to_time > ?) OR (from_time < ? AND to_time >= ?))")) {
                checkStmt.setString(1, roomName);
                checkStmt.setString(2, date);
                checkStmt.setString(3, from);
                checkStmt.setString(4, from);
                checkStmt.setString(5, to);
                checkStmt.setString(6, to);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "This room is already booked for selected time.");
                    return;
                }

                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO bookings (username, room_name, date, from_time, to_time, people, description) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, roomName);
                insertStmt.setString(3, date);
                insertStmt.setString(4, from);
                insertStmt.setString(5, to);
                insertStmt.setInt(6, requestedCap);
                insertStmt.setString(7, desc);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Booking successful!", "Booked", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new MainMenuFrame(username, "employee");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during booking.");
        }
    }

    // Date formatter for date picker
    public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parse(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value instanceof java.util.Calendar) {
                return dateFormatter.format(((java.util.Calendar) value).getTime());
            }
            return "";
        }
    }
}

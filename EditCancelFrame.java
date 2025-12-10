import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Date;
import org.jdatepicker.impl.*;

public class EditCancelFrame extends JFrame {
    JComboBox<Integer> bookingIdBox;
    JComboBox<String> fromTimeBox, toTimeBox;
    JDatePickerImpl datePicker;
    JTextArea bookingListArea;

    public EditCancelFrame(String username) {
        setTitle("Edit or Cancel Booking");
        setSize(750, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left panel
        JPanel leftPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setBackground(new Color(245, 245, 245));

        leftPanel.add(new JLabel("Select Booking ID:"));
        bookingIdBox = new JComboBox<>();
        leftPanel.add(bookingIdBox);

        leftPanel.add(new JLabel("New Date:"));
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        leftPanel.add(datePicker);

        leftPanel.add(new JLabel("From Time:"));
        fromTimeBox = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"});
        leftPanel.add(fromTimeBox);

        leftPanel.add(new JLabel("To Time:"));
        toTimeBox = new JComboBox<>(new String[]{"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"});
        leftPanel.add(toTimeBox);

        JButton editBtn = new JButton("Edit Booking");
        editBtn.setBackground(new Color(70, 130, 180));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFont(new Font("Arial", Font.BOLD, 14));
        editBtn.addActionListener(e -> editBooking(username));
        leftPanel.add(editBtn);

        JButton cancelBtn = new JButton("Cancel Booking");
        cancelBtn.setBackground(new Color(178, 34, 34));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
        cancelBtn.addActionListener(e -> cancelBooking(username));
        leftPanel.add(cancelBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(105, 105, 105));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame(username, "employee");
        });
        leftPanel.add(backBtn);

        add(leftPanel, BorderLayout.WEST);

        // Right panel with booking list
        bookingListArea = new JTextArea();
        bookingListArea.setEditable(false);
        bookingListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bookingListArea.setLineWrap(true);
        bookingListArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(bookingListArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Bookings"));
        scrollPane.setPreferredSize(new Dimension(420, 300));
        add(scrollPane, BorderLayout.CENTER);

        loadUserBookings(username);
        setVisible(true);
    }

    private void loadUserBookings(String username) {
        bookingIdBox.removeAllItems();
        bookingListArea.setText("");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, room_name, date, from_time, to_time, people FROM bookings WHERE username=?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String room = rs.getString("room_name");
                String date = rs.getString("date");
                String from = rs.getString("from_time");
                String to = rs.getString("to_time");
                int people = rs.getInt("people");

                bookingIdBox.addItem(id);
                bookingListArea.append(String.format(
                        "ID: %d | Room: %s | Date: %s | Time: %s to %s | People: %d\n",
                        id, room, date, from, to, people));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editBooking(String username) {
        Integer bookingId = (Integer) bookingIdBox.getSelectedItem();
        if (bookingId == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking ID.");
            return;
        }

        Date selectedDate = (Date) datePicker.getModel().getValue();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a new date.");
            return;
        }

        String newDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
        String newFrom = (String) fromTimeBox.getSelectedItem();
        String newTo = (String) toTimeBox.getSelectedItem();

        if (newTo.compareTo(newFrom) <= 0) {
            JOptionPane.showMessageDialog(this, "To Time must be after From Time.");
            return;
        }

        if (newFrom.compareTo("09:00") < 0 || newTo.compareTo("16:00") > 0) {
            JOptionPane.showMessageDialog(this, "Bookings allowed only between 09:00 and 16:00.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE bookings SET date=?, from_time=?, to_time=? WHERE id=? AND username=?")) {
            stmt.setString(1, newDate);
            stmt.setString(2, newFrom);
            stmt.setString(3, newTo);
            stmt.setInt(4, bookingId);
            stmt.setString(5, username);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Booking updated successfully.");
                loadUserBookings(username);
            } else {
                JOptionPane.showMessageDialog(this, "Booking update failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelBooking(String username) {
        Integer bookingId = (Integer) bookingIdBox.getSelectedItem();
        if (bookingId == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking ID.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE id=? AND username=?")) {
            stmt.setInt(1, bookingId);
            stmt.setString(2, username);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Booking cancelled.");
                loadUserBookings(username);
            } else {
                JOptionPane.showMessageDialog(this, "Cancellation failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Custom formatter for date picker
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

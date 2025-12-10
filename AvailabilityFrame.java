import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.jdatepicker.impl.*;

public class AvailabilityFrame extends JFrame {
    JDatePickerImpl datePicker;
    JComboBox<String> timeBox;
    JTextArea resultArea;

    public AvailabilityFrame(String username) {
        setTitle("Check Room Availability");
        setSize(500, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        inputPanel.setBackground(new Color(245, 245, 245));

        inputPanel.add(new JLabel("Select Date:"));
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        inputPanel.add(datePicker);

        inputPanel.add(new JLabel("Select Time (Start):"));
        timeBox = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"});
        inputPanel.add(timeBox);

        JButton checkBtn = new JButton("Check Availability");
        checkBtn.setBackground(new Color(34, 139, 34));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFont(new Font("Arial", Font.BOLD, 14));
        checkBtn.addActionListener(e -> checkAvailability());
        inputPanel.add(checkBtn);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setBackground(new Color(70, 130, 180));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame(username, "employee");
        });
        inputPanel.add(backBtn);

        add(inputPanel, BorderLayout.NORTH);

        // Result Area
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Arial", Font.PLAIN, 13));
        resultArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        add(scroll, BorderLayout.CENTER);

        getContentPane().setBackground(new Color(245, 245, 245));
        setVisible(true);
    }

    private void checkAvailability() {
        Date selectedDate = (Date) datePicker.getModel().getValue();
        String time = (String) timeBox.getSelectedItem();

        if (selectedDate == null || time == null) {
            JOptionPane.showMessageDialog(this, "Please select both date and time.");
            return;
        }

        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        try (Connection conn = DatabaseManager.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet allRooms = stmt.executeQuery("SELECT room_name FROM rooms");

            StringBuilder available = new StringBuilder();
            while (allRooms.next()) {
                String room = allRooms.getString("room_name");

                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT * FROM bookings WHERE room_name=? AND date=? AND " +
                                "((from_time <= ? AND to_time > ?) OR (from_time < ? AND to_time >= ?))");
                checkStmt.setString(1, room);
                checkStmt.setString(2, dateStr);
                checkStmt.setString(3, time);
                checkStmt.setString(4, time);
                checkStmt.setString(5, time);
                checkStmt.setString(6, time);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    available.append("• ").append(room).append("\n");
                }
            }

            resultArea.setText(available.length() > 0 ? available.toString() : "No rooms available.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Date formatter for JDatePicker
    public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) {
            try {
                return dateFormatter.parse(text);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String valueToString(Object value) {
            if (value instanceof java.util.Calendar) {
                return dateFormatter.format(((java.util.Calendar) value).getTime());
            }
            return "";
        }
    }
}

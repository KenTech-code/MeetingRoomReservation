import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RoomManagementFrame extends JFrame {
    JTextField roomField, capField;
    JTextArea roomList;
    JComboBox<String> roomDropdown;

    public RoomManagementFrame() {
        setTitle("Manage Rooms (Admin)");
        setSize(550, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel roomLabel = new JLabel("Room Name:");
        roomField = new JTextField(15);
        JLabel capLabel = new JLabel("Capacity:");
        capField = new JTextField(15);
        JLabel selectLabel = new JLabel("Select Room:");
        roomDropdown = new JComboBox<>();
        loadRoomsToDropdown();

        // Row 0 - Room Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(roomLabel, gbc);
        gbc.gridx = 1;
        panel.add(roomField, gbc);

        // Row 1 - Capacity
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(capLabel, gbc);
        gbc.gridx = 1;
        panel.add(capField, gbc);

        // Row 2 - Dropdown
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(selectLabel, gbc);
        gbc.gridx = 1;
        panel.add(roomDropdown, gbc);
        roomDropdown.addActionListener(e -> {
            String selected = (String) roomDropdown.getSelectedItem();
            if (selected != null) roomField.setText(selected);
        });

        // Row 3 - Add & Update Buttons
        JButton addBtn = new JButton("Add Room");
        styleButton(addBtn, new Color(46, 139, 87));
        addBtn.addActionListener(e -> addRoom());
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(addBtn, gbc);

        JButton updateBtn = new JButton("Update Room");
        styleButton(updateBtn, new Color(70, 130, 180));
        updateBtn.addActionListener(e -> updateRoom());
        gbc.gridx = 1;
        panel.add(updateBtn, gbc);

        // Row 4 - Delete & Refresh
        JButton deleteBtn = new JButton("Delete Room");
        styleButton(deleteBtn, new Color(178, 34, 34));
        deleteBtn.addActionListener(e -> deleteRoom());
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(deleteBtn, gbc);

        JButton refreshBtn = new JButton("Refresh List");
        styleButton(refreshBtn, new Color(60, 179, 113));
        refreshBtn.addActionListener(e -> {
            loadRoomsToDropdown();
            loadRoomList();
        });
        gbc.gridx = 1;
        panel.add(refreshBtn, gbc);

        // Row 5 - Back Button
        JButton backBtn = new JButton("Back to Menu");
        styleButton(backBtn, new Color(105, 105, 105));
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame("admin", "admin");
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(backBtn, gbc);

        // Room list area
        roomList = new JTextArea();
        roomList.setEditable(false);
        roomList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        roomList.setLineWrap(true);
        roomList.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(roomList);
        scroll.setBorder(BorderFactory.createTitledBorder("All Rooms"));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(500, 150));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(245, 245, 245));
        container.add(panel, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);

        add(container);
        loadRoomList();
        setVisible(true);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void loadRoomList() {
        roomList.setText("");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
            while (rs.next()) {
                roomList.append(String.format("• %s (Capacity: %d)\n",
                        rs.getString("room_name"),
                        rs.getInt("capacity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRoomsToDropdown() {
        roomDropdown.removeAllItems();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_name FROM rooms")) {
            while (rs.next()) {
                roomDropdown.addItem(rs.getString("room_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addRoom() {
        String room = roomField.getText().trim();
        int cap;
        try {
            cap = Integer.parseInt(capField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for capacity.");
            return;
        }

        if (room.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room name cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO rooms VALUES (?, ?)")) {
            stmt.setString(1, room);
            stmt.setInt(2, cap);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Room added.");
            loadRoomsToDropdown();
            loadRoomList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Room name already exists or error occurred.");
        }
    }

    private void updateRoom() {
        String selectedRoom = (String) roomDropdown.getSelectedItem();
        String newRoomName = roomField.getText().trim();
        int cap;

        if (selectedRoom == null || newRoomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room and enter valid data.");
            return;
        }

        try {
            cap = Integer.parseInt(capField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid number for capacity.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE rooms SET room_name=?, capacity=? WHERE room_name=?")) {
            stmt.setString(1, newRoomName);
            stmt.setInt(2, cap);
            stmt.setString(3, selectedRoom);
            int updated = stmt.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Room updated.");
            } else {
                JOptionPane.showMessageDialog(this, "No changes made. Check selection.");
            }

            loadRoomsToDropdown();
            loadRoomList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error occurred while updating room.");
            e.printStackTrace();
        }
    }

    private void deleteRoom() {
        String selected = (String) roomDropdown.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM rooms WHERE room_name=?")) {
            stmt.setString(1, selected);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "Room deleted.");
            } else {
                JOptionPane.showMessageDialog(this, "Room not found or could not be deleted.");
            }

            loadRoomsToDropdown();
            loadRoomList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting room.");
            e.printStackTrace();
        }
    }
}

package Gui;

import db.DatabaseConnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TicketGUI extends JFrame {
    
    private JTextField txtTicketID = new JTextField(10);
    private JTextField txtPrice = new JTextField(10);
    private JTextField txtReservationDate = new JTextField(10);
    private JComboBox<String> cbUsername = new JComboBox<>();
    private JComboBox<String> cbSeatID = new JComboBox<>();
    private JComboBox<String> cbTheaterID = new JComboBox<>();
    
    private DefaultTableModel model;
    private JTable table;
    
    public TicketGUI() {
        setTitle("Tickets");
        setSize(800, 500);
        setLayout(new BorderLayout());
        
        // Form panel for input fields
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        form.add(new JLabel("Ticket ID:")); form.add(txtTicketID);
        form.add(new JLabel("Price:")); form.add(txtPrice);
        form.add(new JLabel("Reservation Date (YYYY-MM-DD HH:MM:SS):")); form.add(txtReservationDate);
        form.add(new JLabel("Username:")); form.add(cbUsername);
        form.add(new JLabel("Seat ID:")); form.add(cbSeatID);
        form.add(new JLabel("Theater ID:")); form.add(cbTheaterID);
        add(form, BorderLayout.NORTH);
        
        // Table setup
        String[] cols = {"ticket_id", "price", "reservation_date", "username", "seat_id", "theater_id"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttons = new JPanel();
        JButton btnInsert = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        
        buttons.add(btnInsert);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnSearch);
        buttons.add(btnRefresh);
        add(buttons, BorderLayout.SOUTH);
        
        // Action listeners
        btnInsert.addActionListener(e -> insertTicket());
        btnUpdate.addActionListener(e -> updateTicket());
        btnDelete.addActionListener(e -> deleteTicket());
        btnSearch.addActionListener(e -> searchTicket());
        btnRefresh.addActionListener(e -> loadData());
        
        // Table row selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                txtTicketID.setText(model.getValueAt(selectedRow, 0).toString());
                txtPrice.setText(model.getValueAt(selectedRow, 1).toString());
                txtReservationDate.setText(model.getValueAt(selectedRow, 2).toString());
                // Set comboboxes based on selected row data
                setComboBoxSelection(cbUsername, model.getValueAt(selectedRow, 3).toString());
                setComboBoxSelection(cbSeatID, model.getValueAt(selectedRow, 4).toString());
                setComboBoxSelection(cbTheaterID, model.getValueAt(selectedRow, 5).toString());
            }
        });
        
        // Load data and show form
        loadForeignKeys();
        loadData();
        setVisible(true);
    }
    
    private void setComboBoxSelection(JComboBox<String> comboBox, String value) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).startsWith(value)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }
    
    private void loadForeignKeys() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            // Load usernames
            cbUsername.removeAllItems();
            PreparedStatement ps = conn.prepareStatement("SELECT username FROM Viewer");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbUsername.addItem(rs.getString("username"));
            }
            
            // Load theater IDs
            cbTheaterID.removeAllItems();
            ps = conn.prepareStatement("SELECT theater_id, theater_name FROM Theater");
            rs = ps.executeQuery();
            while (rs.next()) {
                cbTheaterID.addItem(rs.getString("theater_id") + " - " + rs.getString("theater_name"));
            }
            
            // Seat IDs will be loaded based on selected theater
            cbTheaterID.addActionListener(e -> loadSeatsByTheater());
            
            // Initial load of seats
            loadSeatsByTheater();
            
        } catch (SQLException ex) {
            showError("Loading foreign keys", ex);
        }
    }
    
    private void loadSeatsByTheater() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            cbSeatID.removeAllItems();
            
            if (cbTheaterID.getSelectedItem() != null) {
                String selectedTheater = cbTheaterID.getSelectedItem().toString();
                String theaterId = selectedTheater.split(" - ")[0];
                
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT seat_id, seat_type, seat_status FROM Seat WHERE theater_id = ?");
                ps.setInt(1, Integer.parseInt(theaterId));
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    String seatStatus = rs.getString("seat_status");
                    String displayText = rs.getString("seat_id") + " - " + rs.getString("seat_type");
                    
                    // Optionally show seat status in the dropdown
                    if (seatStatus != null) {
                        displayText += " (" + seatStatus + ")";
                    }
                    
                    cbSeatID.addItem(displayText);
                }
            }
        } catch (SQLException ex) {
            showError("Loading seats", ex);
        }
    }
    
    private void loadData() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT t.ticket_id, t.price, t.reservation_date, t.username, " +
                    "t.seat_id, t.theater_id FROM Ticket t");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ticket_id"),
                    rs.getDouble("price"), // Changed to getDouble to match DECIMAL type
                    rs.getString("reservation_date"),
                    rs.getString("username"),
                    rs.getInt("seat_id"),
                    rs.getInt("theater_id")
                });
            }
        } catch (SQLException ex) {
            showError("Loading data", ex);
        }
    }
    
    /**
     * Checks if a seat is empty (available)
     * @param seatId The seat ID to check
     * @param theaterId The theater ID
     * @return true if seat is empty, false otherwise
     */
    private boolean isSeatEmpty(int seatId, int theaterId) {
        try (Connection conn = DatabaseConnect.getConnection()) {
            String sql = "SELECT seat_status FROM Seat WHERE seat_id = ? AND theater_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, seatId);
            ps.setInt(2, theaterId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("seat_status");
                return "EMPTY".equals(status);
            }
            return false; // Seat not found
        } catch (SQLException ex) {
            showError("Checking seat status", ex);
            return false;
        }
    }
    
    /**
     * Updates a seat's status (EMPTY/OCCUPIED)
     * @param seatId The seat ID to update
     * @param theaterId The theater ID
     * @param status The new status
     */
    private void updateSeatStatus(int seatId, int theaterId, String status) {
        try (Connection conn = DatabaseConnect.getConnection()) {
            String sql = "UPDATE Seat SET seat_status = ? WHERE seat_id = ? AND theater_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, seatId);
            ps.setInt(3, theaterId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            showError("Updating seat status", ex);
        }
    }
    
    private void insertTicket() {
        try {
            // Parse seat_id and theater_id from comboboxes
            String seatIdText = cbSeatID.getSelectedItem().toString().split(" - ")[0];
            String theaterIdText = cbTheaterID.getSelectedItem().toString().split(" - ")[0];
            int seatId = Integer.parseInt(seatIdText);
            int theaterId = Integer.parseInt(theaterIdText);
            double price = Double.parseDouble(txtPrice.getText());
            
            // Check if price is non-negative
            if (price < 0) {
                JOptionPane.showMessageDialog(this,
                    "Price cannot be negative",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if seat is empty before inserting
            if (!isSeatEmpty(seatId, theaterId)) {
                JOptionPane.showMessageDialog(this,
                    "THIS SEAT IS RESERVED",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert the ticket
            try (Connection conn = DatabaseConnect.getConnection()) {
                String sql = "INSERT INTO Ticket (ticket_id, price, reservation_date, username, seat_id, theater_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                
                ps.setInt(1, Integer.parseInt(txtTicketID.getText()));
                ps.setDouble(2, price);
                ps.setString(3, txtReservationDate.getText());
                ps.setString(4, cbUsername.getSelectedItem().toString());
                ps.setInt(5, seatId);
                ps.setInt(6, theaterId);
                
                ps.executeUpdate();
                
                // Update seat status to OCCUPIED
                updateSeatStatus(seatId, theaterId, "OCCUPIED");
                
                loadData();
                JOptionPane.showMessageDialog(this, "Ticket inserted successfully!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid numbers for Ticket ID, Price, Seat ID, and Theater ID",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showError("Insert failed", ex);
        }
    }
    
    private void updateTicket() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row to update");
            return;
        }
        
        try {
            // Get the original seat and theater ID before update
            int originalTicketId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int originalSeatId = Integer.parseInt(model.getValueAt(selectedRow, 4).toString());
            int originalTheaterId = Integer.parseInt(model.getValueAt(selectedRow, 5).toString());
            
            // Parse new seat_id and theater_id from comboboxes
            String seatIdText = cbSeatID.getSelectedItem().toString().split(" - ")[0];
            String theaterIdText = cbTheaterID.getSelectedItem().toString().split(" - ")[0];
            int newSeatId = Integer.parseInt(seatIdText);
            int newTheaterId = Integer.parseInt(theaterIdText);
            double price = Double.parseDouble(txtPrice.getText());
            
            // Check if price is non-negative
            if (price < 0) {
                JOptionPane.showMessageDialog(this,
                    "Price cannot be negative",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // If changing seats, check if the new seat is empty
            if (originalSeatId != newSeatId || originalTheaterId != newTheaterId) {
                if (!isSeatEmpty(newSeatId, newTheaterId)) {
                    JOptionPane.showMessageDialog(this,
                        "The selected new seat is already occupied",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            Connection conn = DatabaseConnect.getConnection();
            try {
                // Begin transaction
                conn.setAutoCommit(false);
                
                // Update the ticket
                String sql = "UPDATE Ticket SET price=?, reservation_date=?, username=?, seat_id=?, theater_id=? WHERE ticket_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                
                ps.setDouble(1, price);
                ps.setString(2, txtReservationDate.getText());
                ps.setString(3, cbUsername.getSelectedItem().toString());
                ps.setInt(4, newSeatId);
                ps.setInt(5, newTheaterId);
                ps.setInt(6, originalTicketId);
                
                ps.executeUpdate();
                
                // If changing seats, update seat statuses
                if (originalSeatId != newSeatId || originalTheaterId != newTheaterId) {
                    // Free the original seat
                    updateSeatStatus(originalSeatId, originalTheaterId, "EMPTY");
                    
                    // Mark the new seat as occupied
                    updateSeatStatus(newSeatId, newTheaterId, "OCCUPIED");
                }
                
                // Commit transaction
                conn.commit();
                
                loadData();
                JOptionPane.showMessageDialog(this, "Ticket updated successfully!");
            } catch (SQLException ex) {
                // Rollback on error
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        showError("Rollback failed", rollbackEx);
                    }
                }
                throw ex;
            } finally {
                // Restore auto-commit mode
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        showError("Connection close failed", closeEx);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid numbers for Ticket ID, Price, Seat ID, and Theater ID",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showError("Update failed", ex);
        }
    }
    
    private void deleteTicket() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete");
            return;
        }
        
        try {
            int ticketId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int seatId = Integer.parseInt(model.getValueAt(selectedRow, 4).toString());
            int theaterId = Integer.parseInt(model.getValueAt(selectedRow, 5).toString());
            
            Connection conn = DatabaseConnect.getConnection();
            try {
                // Begin transaction
                conn.setAutoCommit(false);
                
                // Delete the ticket
                String sql = "DELETE FROM Ticket WHERE ticket_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, ticketId);
                ps.executeUpdate();
                
                // Mark the seat as empty
                updateSeatStatus(seatId, theaterId, "EMPTY");
                
                // Commit transaction
                conn.commit();
                
                loadData();
                JOptionPane.showMessageDialog(this, "Ticket deleted successfully!");
            } catch (SQLException ex) {
                // Rollback on error
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        showError("Rollback failed", rollbackEx);
                    }
                }
                throw ex;
            } finally {
                // Restore auto-commit mode
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        showError("Connection close failed", closeEx);
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Delete failed", ex);
        }
    }
    
    private void searchTicket() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter ticket ID to search:");
        if (searchTerm == null || searchTerm.isEmpty()) return;
        
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.ticket_id, t.price, t.reservation_date, t.username, " +
                    "t.seat_id, t.theater_id FROM Ticket t WHERE t.ticket_id = ?");
            ps.setInt(1, Integer.parseInt(searchTerm));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ticket_id"),
                    rs.getDouble("price"), // Changed to getDouble to match DECIMAL type
                    rs.getString("reservation_date"),
                    rs.getString("username"),
                    rs.getInt("seat_id"),
                    rs.getInt("theater_id")
                });
            }
        } catch (SQLException ex) {
            showError("Search failed", ex);
        }
    }
    
    private void showError(String operation, SQLException ex) {
        JOptionPane.showMessageDialog(this,
                operation + " error: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
package Gui;

import db.DatabaseConnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SeatGUI extends JFrame {
    private JTextField seatID = new JTextField(10);
    private JComboBox<String> theaterID = new JComboBox<>();
    private JComboBox<String> seat_type = new JComboBox<>(new String[]{"STANDARD", "VIP"});
    private JComboBox<String> seat_status = new JComboBox<>(new String[]{"EMPTY", "OCCUPIED"});

    private JTable table = new JTable();
    private DefaultTableModel model = new DefaultTableModel();

    public SeatGUI() {
        setTitle("Seats");
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        form.add(new JLabel("Seat ID:")); form.add(seatID);
        form.add(new JLabel("Theater:")); form.add(theaterID);
        form.add(new JLabel("Seat Type:")); form.add(seat_type);
        form.add(new JLabel("Seat Status:")); form.add(seat_status);
        add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        JButton btnInsert = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");

        buttons.add(btnInsert); buttons.add(btnUpdate);
        buttons.add(btnDelete); buttons.add(btnSearch); buttons.add(btnRefresh);
        add(buttons, BorderLayout.SOUTH);

        model.setColumnIdentifiers(new String[]{"Seat ID", "Theater ID", "Theater Name", "Seat Type", "Seat Status"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnInsert.addActionListener(e -> insertSeat());
        btnUpdate.addActionListener(e -> updateSeat());
        btnDelete.addActionListener(e -> deleteSeat());
        btnSearch.addActionListener(e -> searchSeat());
        btnRefresh.addActionListener(e -> loadData());

        loadTheaters();
        loadData();
        setVisible(true);
    }

    private void loadTheaters() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            theaterID.removeAllItems();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT theater_id, theater_name FROM Theater");

            while (rs.next()) {
                theaterID.addItem(rs.getInt("theater_id") + " - " + rs.getString("theater_name"));
            }
        } catch (Exception e) {
            showError("Load Theaters", e);
        }
    }

    private void loadData() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.seat_id, s.theater_id, t.theater_name, s.seat_type, s.seat_status " +
                "FROM Seat s JOIN Theater t ON s.theater_id = t.theater_id");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("seat_id"),
                    rs.getInt("theater_id"),
                    rs.getString("theater_name"),
                    rs.getString("seat_type"),
                    rs.getString("seat_status")
                });
            }
        } catch (Exception e) {
            showError("Load Data", e);
        }
    }

    private void insertSeat() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Seat VALUES (?, ?, ?, ?)");
            ps.setInt(1, Integer.parseInt(seatID.getText()));
            ps.setInt(4, Integer.parseInt(theaterID.getSelectedItem().toString().split(" - ")[0]));
            ps.setString(2, seat_type.getSelectedItem().toString());
            ps.setString(3, seat_status.getSelectedItem().toString());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Seat inserted.");
        } catch (Exception e) {
            showError("Insert", e);
        }
    }

    private void updateSeat() {
        int i = table.getSelectedRow();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Seat SET seat_type=?, seat_status=? WHERE seat_id=? AND theater_id=?");
            ps.setString(1, seat_type.getSelectedItem().toString());
            ps.setString(2, seat_status.getSelectedItem().toString());
            ps.setInt(3, Integer.parseInt(seatID.getText()));
            ps.setInt(4, Integer.parseInt(theaterID.getSelectedItem().toString().split(" - ")[0]));
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Seat updated.");
        } catch (Exception e) {
            showError("Update", e);
        }
    }

    private void deleteSeat() {
        int i = table.getSelectedRow();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Seat WHERE seat_id=? AND theater_id=?");
            ps.setInt(1, Integer.parseInt(seatID.getText()));
            ps.setInt(2, Integer.parseInt(theaterID.getSelectedItem().toString().split(" - ")[0]));
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Seat deleted.");
        } catch (Exception e) {
            showError("Delete", e);
        }
    }

    private void searchSeat() {
        String term = JOptionPane.showInputDialog(this, "Enter seat ID to search:");
        if (term == null) return;

        model.setRowCount(0);
        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.seat_id, s.theater_id, t.theater_name, s.seat_type, s.seat_status " +
                "FROM Seat s JOIN Theater t ON s.theater_id = t.theater_id " +
                "WHERE s.seat_id=?");
            ps.setInt(1, Integer.parseInt(term));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("seat_id"),
                    rs.getInt("theater_id"),
                    rs.getString("theater_name"),
                    rs.getString("seat_type"),
                    rs.getString("seat_status")
                });

                seatID.setText(String.valueOf(rs.getInt("seat_id")));
                seat_type.setSelectedItem(rs.getString("seat_type"));
                seat_status.setSelectedItem(rs.getString("seat_status"));

                String theaterItem = rs.getInt("theater_id") + " - " + rs.getString("theater_name");
                for (int i2 = 0; i2 < theaterID.getItemCount(); i2++) {
                    if (theaterID.getItemAt(i2).equals(theaterItem)) {
                        theaterID.setSelectedIndex(i2);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            showError("Search", e);
        }
    }

    private void showError(String op, Exception ex) {
        JOptionPane.showMessageDialog(this,
            op + " error: " + ex.getMessage(),
            "DB Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SeatGUI());
    }
}

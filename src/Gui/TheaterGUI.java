package Gui;

import db.DatabaseConnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TheaterGUI extends JFrame {

    private JTextField txtTheater_id = new JTextField(20);
    private JTextField txtTheater_name = new JTextField(20);
    private JTextField txtNum_seat = new JTextField(20);

    private JTable table = new JTable();
    private DefaultTableModel model = new DefaultTableModel();

    public TheaterGUI() {
        setTitle("Theaters");
        setSize(600, 400);
        setLayout(new BorderLayout());
        
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        form.add(new JLabel("Theater ID:")); form.add(txtTheater_id);
        form.add(new JLabel("Theater Name:")); form.add(txtTheater_name);
        form.add(new JLabel("Number of Seats:")); form.add(txtNum_seat);
        add(form, BorderLayout.NORTH);

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
        
        model.setColumnIdentifiers(new String[]{"Theater ID", "Theater Name", "Number of Seats"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnInsert.addActionListener(e -> insertTheater());
        btnUpdate.addActionListener(e -> updateTheater());
        btnDelete.addActionListener(e -> deleteTheater());
        btnSearch.addActionListener(e -> searchTheater());
        btnRefresh.addActionListener(e -> loadData());

        loadData();
        setVisible(true);
    }
    
    private void loadData() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Theater");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("theater_id"),
                    rs.getString("theater_name"),
                    rs.getInt("number_of_seats")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading theaters: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void insertTheater() {
        String idText = txtTheater_id.getText().trim();
        String name = txtTheater_name.getText().trim();
        String seatsText = txtNum_seat.getText().trim();

        // Validate input
        if (idText.isEmpty() ) {
            JOptionPane.showMessageDialog(this, "theater id is required.");
            return;
        }
        
        if (!idText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Theater ID must be a valid integer.");
            return;
        }
        if (name.isEmpty() || seatsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, " theater name / number of seats is empty.");
            return;
        }
        if (!seatsText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Number of Seats must be a valid integer.");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Theater VALUES (?, ?, ?)");
            ps.setInt(1, Integer.parseInt(idText));
            ps.setString(2, name);
            ps.setInt(3, Integer.parseInt(seatsText));
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            showError("Insert", e);
        }
    }


    private void updateTheater() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        String idText = txtTheater_id.getText().trim();
        String name = txtTheater_name.getText().trim();
        String seatsText = txtNum_seat.getText().trim();

        // Validation
        if (idText.isEmpty() || name.isEmpty() || seatsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.");
            return;
        }

        if (!idText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Theater ID must be a valid integer.");
            return;
        }

        if (!seatsText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Number of Seats must be a valid integer.");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Theater SET theater_name=?, number_of_seats=? WHERE theater_id=?");
            ps.setString(1, name);
            ps.setInt(2, Integer.parseInt(seatsText));
            ps.setInt(3, Integer.parseInt(idText));  // Now uses txtTheater_id correctly
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Theater updated successfully.");
        } catch (Exception e) {
            showError("Update", e);
        }
    }


    private void deleteTheater() {
    	
        int i = table.getSelectedRow();
        if (i < 0) { 
            JOptionPane.showMessageDialog(this, "Select a row first"); 
            return; 
        }
        int theaterId = Integer.parseInt(model.getValueAt(i, 0).toString());
        
        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Theater WHERE theater_id=?");
            ps.setInt(1, theaterId);
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            showError("Delete", e);
        }
    }

    private void searchTheater() {
        String term = JOptionPane.showInputDialog(this, "Enter theater ID to search:");
        if (term == null) return;
        
        model.setRowCount(0);
        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Theater WHERE theater_id=?");
            ps.setInt(1, Integer.parseInt(term));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("theater_id"),
                    rs.getString("theater_name"),
                    rs.getInt("number_of_seats")
                });
                
                // Also fill the form fields
                txtTheater_id.setText(String.valueOf(rs.getInt("theater_id")));
                txtTheater_name.setText(rs.getString("theater_name"));
                txtNum_seat.setText(String.valueOf(rs.getInt("number_of_seats")));
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
}
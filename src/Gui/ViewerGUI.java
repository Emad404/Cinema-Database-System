package Gui;

import db.DatabaseConnect;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewerGUI extends JFrame {
	
    private JTextField txtUsername = new JTextField(20);
    private JTextField txtFname = new JTextField(20);
    private JTextField txtLname = new JTextField(20);
    private JTextField txtEmail = new JTextField(20);
    private JTextField txtPhone = new JTextField(20);
    private JTextField txtDob = new JTextField(20);
    private DefaultTableModel model;
    private JTable table;

    public ViewerGUI() {
        setTitle("Viewers");
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        form.add(new JLabel("Username:")); form.add(txtUsername);
        form.add(new JLabel("First Name:")); form.add(txtFname);
        form.add(new JLabel("Last Name:")); form.add(txtLname);
        form.add(new JLabel("Email:")); form.add(txtEmail);
        form.add(new JLabel("Phone (10 digits):")); form.add(txtPhone);
        form.add(new JLabel("DOB (YYYY-MM-DD):")); form.add(txtDob);
        add(form, BorderLayout.NORTH);

        String[] cols = {"Username", "First Name", "Last Name", "Email", "Phone", "Date of Birth"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnInsert = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");

        buttons.add(btnInsert); buttons.add(btnUpdate); buttons.add(btnDelete);
        buttons.add(btnSearch); buttons.add(btnRefresh);
        add(buttons, BorderLayout.SOUTH);

        btnInsert.addActionListener(e -> insertViewer());
        btnUpdate.addActionListener(e -> updateViewer());
        btnDelete.addActionListener(e -> deleteViewer());
        btnSearch.addActionListener(e -> searchViewer());
        btnRefresh.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            int i = table.getSelectedRow();
            if (i >= 0) {
                txtUsername.setText(model.getValueAt(i, 0).toString());
                txtFname.setText(model.getValueAt(i, 1).toString());
                txtLname.setText(model.getValueAt(i, 2).toString());
                txtEmail.setText(model.getValueAt(i, 3).toString());
                txtPhone.setText(model.getValueAt(i, 4).toString());
                txtDob.setText(model.getValueAt(i, 5).toString());
            }
        });

        loadData();
        setVisible(true);
    }

    private void insertViewer() {
    	String username = txtUsername.getText().trim();
    	String fName = txtFname.getText().trim();
    	String lName = txtLname.getText().trim();
    	String email = txtEmail.getText().trim();
    	String phone = txtPhone.getText().trim();
    	String dob = txtDob.getText().trim();

    	// First: check if required fields are empty
    	if (username.isEmpty()) {
    	    JOptionPane.showMessageDialog(this, "Username is required.");
    	    return;
    	}

    	// (Optional) check others too if needed
    	if (email.isEmpty() || dob.isEmpty()) {
    	    JOptionPane.showMessageDialog(this, "Email and Date of Birth are  empty.");
    	    return;
    	}
    	
    	if (!txtDob.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "DOB must be in YYYY-MM-DD format.");
            return;
            
        }
    	
    	 phone = txtPhone.getText().trim();
    	 
    	 if (phone.isEmpty()) {
     	    JOptionPane.showMessageDialog(this, "Phone number must is  empty.");
     	    return;
     	}

    	if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
    	    JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.");
    	    return;
    	}
        
    	
        
       /* if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "DOB must be in YYYY-MM-DD format.");
            return;
        }*/
      
        String sql = "INSERT INTO Viewer VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnect.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtUsername.getText());
            p.setString(2, txtFname.getText());
            p.setString(3, txtLname.getText());
            p.setString(4, txtEmail.getText());
            p.setInt(5, Integer.parseInt(txtPhone.getText()));
            p.setDate(6, Date.valueOf(txtDob.getText()));
            p.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Viewer inserted.");
        } catch (SQLException ex) {
            showError("Insert", ex);
        }
    }

    private void updateViewer() {
        if (!txtPhone.getText().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.");
            return;
        }
        if (!txtDob.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "DOB must be in YYYY-MM-DD format.");
            return;
        }

        int i = table.getSelectedRow();
        if (i < 0) return;

        String sql = "UPDATE Viewer SET Fname=?, Lname=?, email=?, phone_number=?, date_of_birth=? WHERE username=?";
        try (Connection c = DatabaseConnect.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtFname.getText());
            p.setString(2, txtLname.getText());
            p.setString(3, txtEmail.getText());
            p.setInt(4, Integer.parseInt(txtPhone.getText()));
            p.setDate(5, Date.valueOf(txtDob.getText()));
            p.setString(6, txtUsername.getText());
            p.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Viewer updated.");
        } catch (SQLException ex) {
            showError("Update", ex);
        }
    }

    private void deleteViewer() {
        int i = table.getSelectedRow();
        if (i < 0) return;

        String sql = "DELETE FROM Viewer WHERE username=?";
        try (Connection c = DatabaseConnect.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, model.getValueAt(i, 0).toString());
            p.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Viewer deleted.");
        } catch (SQLException ex) {
            showError("Delete", ex);
        }
    }

    private void searchViewer() {
        String term = JOptionPane.showInputDialog(this, "Enter username to search:");
        if (term == null) return;
        model.setRowCount(0);
        String sql = "SELECT * FROM Viewer WHERE username LIKE ?";
        try (Connection c = DatabaseConnect.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + term + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"), rs.getString("Fname"),
                    rs.getString("Lname"), rs.getString("email"),
                    rs.getString("phone_number"), rs.getString("date_of_birth")
                });
            }
        } catch (SQLException ex) {
            showError("Search", ex);
        }
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection c = DatabaseConnect.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM Viewer")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"), rs.getString("Fname"),
                    rs.getString("Lname"), rs.getString("email"),
                    rs.getString("phone_number"), rs.getString("date_of_birth")
                });
            }
        } catch (SQLException e) {
            showError("Load", e);
        }
    }

    private void showError(String op, SQLException ex) {
        JOptionPane.showMessageDialog(this,
            op + " error: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

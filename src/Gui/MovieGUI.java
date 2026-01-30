package Gui;

import db.DatabaseConnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MovieGUI extends JFrame {
    private JTextField txtID = new JTextField(20);
    private JTextField txtName = new JTextField(20);
    private JTextField txtAge_rate = new JTextField(20);
    private JComboBox<String> langBox = new JComboBox<>(new String[]{"ENGLISH", "ARABIC"});

    private JTable table = new JTable();
    private DefaultTableModel model = new DefaultTableModel();

    public MovieGUI() {
        setTitle("Movies");
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        form.add(new JLabel("Movie ID:")); form.add(txtID);
        form.add(new JLabel("Movie Name:")); form.add(txtName);
        form.add(new JLabel("Age Rate (number):")); form.add(txtAge_rate);
        form.add(new JLabel("Language:")); form.add(langBox);
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

        model.setColumnIdentifiers(new String[]{"Movie ID", "Movie Name", "Age Rate", "Language"});
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnInsert.addActionListener(e -> insertMovie());
        btnUpdate.addActionListener(e -> updateMovie());
        btnDelete.addActionListener(e -> deleteMovie());
        btnSearch.addActionListener(e -> searchMovie());
        btnRefresh.addActionListener(e -> loadData());

        loadData();
        setVisible(true);
    }

    private void loadData() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Movie");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("movie_id"),
                    rs.getString("movie_name"),
                    rs.getInt("age_rate"),
                    rs.getString("language")
                });
            }
        } catch (Exception e) {
            showError("Load", e);
        }
    }
   
    private void insertMovie() {
    	
        String idText = txtID.getText().trim();
        String name = txtName.getText().trim();
        String ageRateText = txtAge_rate.getText().trim();
        String language = langBox.getSelectedItem().toString();

        //  Validate Movie ID (Primary Key)
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Movie ID is required.");
            return;
        }

        if (!idText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Movie ID must be a valid integer.");
            return;
        }

        //  Validate Age Rate
        if (!ageRateText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Age rate must be a valid integer.");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Movie VALUES (?, ?, ?, ?)");

            ps.setInt(1, Integer.parseInt(idText));     // movie_id
            ps.setString(2, name);                      // movie_name
            ps.setInt(3, Integer.parseInt(ageRateText));// age_rate
            ps.setString(4, language);                  // language

            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Movie inserted.");
        } catch (Exception e) {
            showError("Insert", e);
        }
    }

    /*private void insertMovie() {
       
    
    	
    	if (!txtAge_rate.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Age rate must be a valid integer.");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Movie VALUES (?, ?, ?, ?)");
          
            ps.setInt(1, Integer.parseInt(txtID.getText()));             // movie_id
            ps.setString(2, txtName.getText());                          // movie_name
            ps.setInt(3, Integer.parseInt(txtAge_rate.getText()));       // age_rate
            ps.setString(4, langBox.getSelectedItem().toString());       // language

            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Movie inserted.");
        } catch (Exception e) {
            showError("Insert", e);
        }
    }*/

    private void updateMovie() {
        if (!txtAge_rate.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Age rate must be a valid integer.");
            return;
        }

        int i = table.getSelectedRow();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first");
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Movie SET movie_name=?, age_rate=?, language=? WHERE movie_id=?");
            ps.setString(1, txtName.getText());
            ps.setInt(2, Integer.parseInt(txtAge_rate.getText()));
            ps.setString(3, langBox.getSelectedItem().toString());
            ps.setInt(4, Integer.parseInt(txtID.getText()));
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Movie updated.");
        } catch (Exception e) {
            showError("Update", e);
        }
    }

    private void deleteMovie() {
    	
        int i = table.getSelectedRow();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first");
            return;
        }
        
        int movieId = (int) model.getValueAt(i, 0); // column 0 = Movie ID

        try (Connection conn = DatabaseConnect.getConnection()) {
            
        	PreparedStatement ps = conn.prepareStatement("DELETE FROM Movie WHERE movie_id=?");
           
        	ps.setInt(1, movieId);
            
        	ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Movie deleted.");
        } catch (Exception e) {
            showError("Delete", e);
        }
    }

    private void searchMovie() {
        String term = JOptionPane.showInputDialog(this, "Enter movie ID to search:");
        if (term == null) return;

        model.setRowCount(0);
        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Movie WHERE movie_id=?");
            ps.setInt(1, Integer.parseInt(term));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("movie_id"),
                    rs.getString("movie_name"),
                    rs.getInt("age_rate"),
                    rs.getString("language")
                });

                txtID.setText(String.valueOf(rs.getInt("movie_id")));
                txtName.setText(rs.getString("movie_name"));
                txtAge_rate.setText(String.valueOf(rs.getInt("age_rate")));
                langBox.setSelectedItem(rs.getString("language"));
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

package Gui;

import db.DatabaseConnect;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ShowsGUI extends JFrame {

    private JComboBox<String> movieComboBox = new JComboBox<>();
    private JComboBox<String> theaterComboBox = new JComboBox<>();
    private JTextField startDateTimeField = new JTextField(15);
    private JTextField endDateTimeField = new JTextField(15);

    private JTable table = new JTable();
    private DefaultTableModel model = new DefaultTableModel();

    private Map<String, Integer> movieMap = new HashMap<>();
    private Map<String, Integer> theaterMap = new HashMap<>();

    public ShowsGUI() {
        setTitle("Shows Management");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Show Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Movie ID:"), gbc); //***** was movie
        gbc.gridx = 1;
        formPanel.add(movieComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Theater ID:"), gbc); //***** was theater
        gbc.gridx = 1;
        formPanel.add(theaterComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Start Date/Time (YYYY-MM-DD HH:MM:SS):"), gbc);
        gbc.gridx = 1;
        formPanel.add(startDateTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("End Date/Time (YYYY-MM-DD HH:MM:SS):"), gbc);
        gbc.gridx = 1;
        formPanel.add(endDateTimeField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnInsert = new JButton("Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");

        buttonPanel.add(btnInsert);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnRefresh);

        model.setColumnIdentifiers(new String[]{"Movie ID", "Movie Name", "Theater ID", "Theater Name", "Start Date/Time", "End Date/Time"});
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        btnInsert.addActionListener(e -> insertShow());
        btnUpdate.addActionListener(e -> updateShow());
        btnDelete.addActionListener(e -> deleteShow());
        btnSearch.addActionListener(e -> searchShow());
        btnRefresh.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });

        loadMoviesAndTheaters();
        loadData();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMoviesAndTheaters() {
    	
        try (Connection conn = DatabaseConnect.getConnection()) {
            movieComboBox.removeAllItems();
            Statement stmtMovies = conn.createStatement();
            ResultSet rsMovies = stmtMovies.executeQuery("SELECT movie_id FROM Movie");

            while (rsMovies.next()) {
                movieComboBox.addItem(String.valueOf(rsMovies.getInt("movie_id")));
            }

            theaterComboBox.removeAllItems();
            Statement stmtTheaters = conn.createStatement();
            ResultSet rsTheaters = stmtTheaters.executeQuery("SELECT theater_id FROM Theater");

            while (rsTheaters.next()) {
                theaterComboBox.addItem(String.valueOf(rsTheaters.getInt("theater_id")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading movie and theater IDs: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadData() {
        try (Connection conn = DatabaseConnect.getConnection()) {
            model.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.movie_id, m.movie_name, s.theater_id, t.theater_name, s.start_datetime, s.end_datetime " +
                "FROM Shows s " +
                "JOIN Movie m ON s.movie_id = m.movie_id " +
                "JOIN Theater t ON s.theater_id = t.theater_id " +
                "ORDER BY s.start_datetime DESC");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("movie_id"),
                    rs.getString("movie_name"),
                    rs.getInt("theater_id"),
                    rs.getString("theater_name"),
                    rs.getString("start_datetime"),
                    rs.getString("end_datetime")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading shows: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromTable(int selectedRow) {
        movieComboBox.setSelectedItem(model.getValueAt(selectedRow, 1).toString());
        theaterComboBox.setSelectedItem(model.getValueAt(selectedRow, 3).toString());
        startDateTimeField.setText(model.getValueAt(selectedRow, 4).toString());
        endDateTimeField.setText(model.getValueAt(selectedRow, 5).toString());
    }

    private void insertShow() {
        try {
            // Get user inputs
            String movieIdStr = movieComboBox.getSelectedItem().toString();
            String theaterIdStr = theaterComboBox.getSelectedItem().toString();
            String startDate = startDateTimeField.getText().trim();
            String endDate = endDateTimeField.getText().trim();

            // ❗ Check if startDate is missing (since it's part of the PK)
            if (startDate.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Start Date/Time is required. ",
                    "Missing Start Time", JOptionPane.WARNING_MESSAGE);
                return; // Stop insertion
            }

            int movieId = Integer.parseInt(movieIdStr);
            int theaterId = Integer.parseInt(theaterIdStr);

            // Proceed with DB insert...
            try (Connection conn = DatabaseConnect.getConnection()) {
                String sql = "INSERT INTO Shows (movie_id, theater_id, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, movieId);
                ps.setInt(2, theaterId);
                ps.setString(3, startDate);
                ps.setString(4, endDate);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Show inserted successfully.");
                loadData();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inserting show: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void updateShow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        if (!validateForm()) return;

        if (startDateTimeField.getText().trim().equals(endDateTimeField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Start and End time cannot be the same.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            int movieId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int theaterId = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
            String oldStartTime = model.getValueAt(selectedRow, 4).toString();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Shows SET start_datetime=?, end_datetime=? " +
                    "WHERE movie_id=? AND theater_id=? AND start_datetime=?");
            ps.setString(1, startDateTimeField.getText().trim());
            ps.setString(2, endDateTimeField.getText().trim());
            ps.setInt(3, movieId);
            ps.setInt(4, theaterId);
            ps.setString(5, oldStartTime);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Show updated successfully.");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating show: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteShow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnect.getConnection()) {
            int movieId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int theaterId = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());
            String startTime = model.getValueAt(selectedRow, 4).toString();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM Shows WHERE movie_id=? AND theater_id=? AND start_datetime=?");
            ps.setInt(1, movieId);
            ps.setInt(2, theaterId);
            ps.setString(3, startTime);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Show deleted successfully.");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting show: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchShow() {
        String[] options = {"Movie Name", "Theater Name", "Date"};
        String selected = (String) JOptionPane.showInputDialog(this, "Search by:", "Search Shows",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (selected == null) return;

        String input = JOptionPane.showInputDialog(this, "Enter search term:");
        if (input == null || input.trim().isEmpty()) return;

        String query = "SELECT s.movie_id, m.movie_name, s.theater_id, t.theater_name, s.start_datetime, s.end_datetime " +
                       "FROM Shows s JOIN Movie m ON s.movie_id = m.movie_id JOIN Theater t ON s.theater_id = t.theater_id ";
        String condition = "";
        switch (selected) {
            case "Movie Name": condition = "WHERE m.movie_name LIKE ?"; break;
            case "Theater Name": condition = "WHERE t.theater_name LIKE ?"; break;
            case "Date": condition = "WHERE s.start_datetime LIKE ?"; break;
        }

        try (Connection conn = DatabaseConnect.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query + condition);
            ps.setString(1, "%" + input.trim() + "%");
            ResultSet rs = ps.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("movie_id"),
                        rs.getString("movie_name"),
                        rs.getInt("theater_id"),
                        rs.getString("theater_name"),
                        rs.getString("start_datetime"),
                        rs.getString("end_datetime")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateForm() {
        if (movieComboBox.getSelectedIndex() < 0 || theaterComboBox.getSelectedIndex() < 0) return false;
        if (startDateTimeField.getText().trim().isEmpty() || endDateTimeField.getText().trim().isEmpty()) return false;
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShowsGUI::new);
    }
}

package Gui;
import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DatabaseConnect;

public class OLDViewerGUI extends JFrame {
	
	// --------------database connections
	private static final String URL = "jdbc:mysql://localhost:3306/Cienma";
	private static final String USER = "root";
	private static final String PASS = "Emad$mysql99";
	
	//----------GUI components
	
	private JTextField txtUsername = new JTextField(10);
	private JTextField txtFname = new JTextField(10);
	private JTextField txtLname = new JTextField(10);
	private JTextField txtEmail = new JTextField(10);
	private JTextField txtPhone = new JTextField(10);
	private JTextField txtDob = new JTextField(10);
	private DefaultTableModel model;
	private JTable table;
	
	
	public OLDViewerGUI() {
	super("Viewer Manager");
	initUI();
	loadData();
	
	}


	private void initUI() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(5,5));
		
		// ----------------entering model
		
		JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		form.add(new JLabel("Username:")); form.add(txtUsername);
		form.add(new JLabel("Fname:")); form.add(txtFname);
		form.add(new JLabel("Lname:")); form.add(txtLname);
		form.add(new JLabel("Email:")); form.add(txtEmail);
		form.add(new JLabel("Phone:")); form.add(txtPhone);
		form.add(new JLabel("DOB:")); form.add(txtDob);
		add(form, BorderLayout.NORTH);
		//------------
		String[] cols = { "username", "Fname", "Lname", "email", "phone_number",
		"date_of_birth" };
		model = new DefaultTableModel(cols, 0);
		table = new JTable(model);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		//----------buttons operations
		
		JPanel buttons = new JPanel();
		JButton btnInsert = new JButton("Insert");
		JButton btnUpdate = new JButton("Update");
		JButton btnDelete = new JButton("Delete");
		JButton btnSearch = new JButton("Search");
		buttons.add(btnInsert);
		buttons.add(btnUpdate);
		buttons.add(btnDelete);
		buttons.add(btnSearch);
		add(buttons, BorderLayout.SOUTH);
		
		// connect each button with it's operation
		
		btnInsert.addActionListener(e -> insertViewer());
		btnUpdate.addActionListener(e -> updateViewer());
		btnDelete.addActionListener(e -> deleteViewer());
		btnSearch.addActionListener(e -> searchViewer());
		}
	
	// to show all data
	private void loadData() {	

		model.setRowCount(0);
		String sql = "SELECT * FROM Viewer";
		try (Connection c = DriverManager.getConnection(URL, USER, PASS);
		Statement s = c.createStatement();
		ResultSet rs= s.executeQuery(sql)) {
		
			while (rs.next()) {
		model.addRow(new Object[]{
		rs.getString("username"),
		rs.getString("Fname"),
		rs.getString("Lname"),
		rs.getString("email"),
		rs.getString("phone_number"),
		rs.getString("date_of_birth")
		});
		}
		} catch (SQLException ex) {
			showError("Load", ex);
			}
			}



// insert operation

	private void insertViewer() {
		
	String sql = "INSERT INTO Viewer VALUES (?,?,?,?,?,?)";
	try (Connection c = DriverManager.getConnection(URL, USER, PASS);
	
	PreparedStatement p = c.prepareStatement(sql)) {
	p.setString(1, txtUsername.getText());
	p.setString(2, txtFname.getText());
	p.setString(3, txtLname.getText());
	p.setString(4, txtEmail.getText());
	p.setString(5, txtPhone.getText());
	p.setString(6, txtDob.getText());
	p.executeUpdate();
	
	loadData();
	}
	catch (SQLException ex) {
		showError("Insert", ex);
		}
	}

	//update operation
	private void updateViewer() {
		int i = table.getSelectedRow();
		if (i < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
		String user = model.getValueAt(i, 0).toString();
		
		String sql = "UPDATE Viewer SET Fname=?,Lname=?,email=?,phone_number=?,date_of_birth=? WHERE username=? ";
		
		try (Connection c = DriverManager.getConnection(URL, USER, PASS);
				PreparedStatement p = c.prepareStatement(sql)) {
				p.setString(1, txtFname.getText());
				p.setString(2, txtLname.getText());
				p.setString(3, txtEmail.getText());
				p.setString(4, txtPhone.getText());
				p.setString(5, txtDob.getText());
				p.setString(6, user);
				p.executeUpdate();
				loadData();
				} catch (SQLException ex) {
				showError("Update", ex);
				}
				}
	
	//--------delete operation
	private void deleteViewer() {
		
		int i = table.getSelectedRow();
		if (i < 0) { JOptionPane.showMessageDialog(this, "Select a row first"); return; }
		
		String user = model.getValueAt(i, 0).toString();
		String sql = "DELETE FROM Viewer WHERE username=?";
		
		try (Connection c = DriverManager.getConnection(URL, USER, PASS);
		PreparedStatement p = c.prepareStatement(sql)) {
		p.setString(1, user);
		p.executeUpdate();
		loadData();
		
		} catch (SQLException ex) {
		showError("Delete", ex);
		}
		}

	//--------search operation

	private void searchViewer() {
		String term = JOptionPane.showInputDialog(this, "Enter username to search:");
		if (term == null) return;
		
		model.setRowCount(0);
		String sql = "SELECT * FROM Viewer WHERE username LIKE ?";
		
		try (Connection c = DriverManager.getConnection(URL, USER, PASS);
		PreparedStatement p = c.prepareStatement(sql)) {
		p.setString(1, "%" + term + "%");
		
		try (ResultSet rs = p.executeQuery()) {
		
			while (rs.next()) {
		model.addRow(new Object[]{
		rs.getString("username"),
		rs.getString("Fname"),
		rs.getString("Lname"),
		rs.getString("email"),
		rs.getString("phone_number"),
		rs.getString("date_of_birth")
		});
		}
		}
		} catch (SQLException ex) {
		showError("Search", ex);	
			}
	}
	
	//------ method to show error message
	private void showError(String op, SQLException ex) {
		JOptionPane.showMessageDialog(this,
		op + " error: " + ex.getMessage(),
		"DB Error", JOptionPane.ERROR_MESSAGE);
		}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new OLDViewerGUI().setVisible(true));
	}
	
	}

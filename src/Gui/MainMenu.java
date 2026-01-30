package Gui;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Cinema - Main Menu");
        setSize(600, 500);  // 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1, 10, 10));  // 

        Font buttonFont = new Font("Arial", Font.BOLD, 22); //

        // Create and style buttons
        JButton viewerBtn = createStyledButton("Viewer", buttonFont);
        JButton movieBtn = createStyledButton("Movie", buttonFont);
        JButton theaterBtn = createStyledButton("Theater", buttonFont);
        JButton showsBtn = createStyledButton("Shows", buttonFont);
        JButton ticketBtn = createStyledButton("Ticket", buttonFont);
        JButton seatBtn = createStyledButton("Seat", buttonFont);

        // Add button listeners
        viewerBtn.addActionListener(e -> new ViewerGUI());
        movieBtn.addActionListener(e -> new MovieGUI());
        theaterBtn.addActionListener(e -> new TheaterGUI());
        showsBtn.addActionListener(e -> new ShowsGUI());
        ticketBtn.addActionListener(e -> new TicketGUI());
        seatBtn.addActionListener(e -> new SeatGUI());

        // Add buttons to the frame
        add(viewerBtn);
        add(movieBtn);
        add(theaterBtn);
        add(showsBtn);
        add(ticketBtn);
        add(seatBtn);

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private JButton createStyledButton(String text, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setFocusPainted(false); // remove focus border
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}

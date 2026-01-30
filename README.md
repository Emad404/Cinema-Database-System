Cinema Database Management System
Project Overview
This project is a comprehensive Cinema Management System developed as part of the CSC380 (Fundamentals of Database Systems) course at King Saud University. The system allows administrators to manage viewers, movie schedules, theater seat availability, and ticket bookings through a Java-based Graphical User Interface (GUI).

Features
User Management: Add, update, and search for cinema viewers.

Movie & Theater Tracking: Manage movie details (language, age rating) and theater capacities.

Show Scheduling: Coordinate movie start and end times across different theaters.

Ticket Booking System: Reserve specific seats for viewers, ensuring seats are not double-booked.

Automated Seat Status: The system automatically marks seats as 'OCCUPIED' upon ticket purchase and 'EMPTY' upon deletion.

Technical Stack
Language: Java 17+.

Database: MySQL.

Interface: Java Swing.

Connectivity: JDBC (Java Database Connectivity).

Project Structure
The repository is organized to follow standard development practices:

src/Gui/: Contains the Java Swing classes for the user interface.

src/db/: Contains DatabaseConnect.java, which manages the JDBC lifecycle.

config/: Holds database.properties for easy configuration of DB credentials.

SQL/: Contains the updated1_db.sql script to recreate the entire database schema and sample data.

Getting Started
1. Database Setup
Open MySQL Workbench.

Go to File > Open SQL Script and select SQL/updated1_db.sql.

Run the script to create the cinema database and all required tables.

2. Configuration
Update the config/database.properties file with your local MySQL credentials:

Properties
db.url=jdbc:mysql://127.0.0.1:3306/cinema
db.user=your_username
db.password=your_password
3. Run the Application
Run the MainMenu.java file from your IDE (Eclipse/IntelliJ) to launch the application.

Why this README is effective:
Professional Tone: It uses academic and technical language that reflects your status as a KSU student.

Clear Navigation: It explains exactly what the config/ and SQL/ folders are for, showing that you understand project architecture.

Functional Guidance: It provides a clear "Getting Started" guide so your professor can test your code immediately.

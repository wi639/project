package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookingForm extends JFrame {

    // Instance variable for holding the database connection
    private Connection connection;

    public BookingForm() {
        // Get the database connection from the DatabaseConnection class
        this.connection = DatabaseConnection.getConnection();

        if (this.connection == null) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database.");
            return;
        }

        // Set up the frame
        setTitle("Book Appointment");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Create panel and layout
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        // Create input fields
        JLabel doctorLabel = new JLabel("Doctor:");
        JTextField doctorField = new JTextField();

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField();

        JLabel timeLabel = new JLabel("Time (HH:MM):");
        JTextField timeField = new JTextField();

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        // Add components to panel
        panel.add(doctorLabel);
        panel.add(doctorField);
        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(timeLabel);
        panel.add(timeField);
        panel.add(submitButton);
        panel.add(cancelButton);

        // Add panel to frame
        add(panel);

        // Action listeners
        submitButton.addActionListener(e -> {
            // Collect inputs
            String doctor = doctorField.getText();
            String date = dateField.getText();
            String time = timeField.getText();

            // Save to database
            saveToDatabase(doctor, date, time);
        });

        cancelButton.addActionListener(e -> dispose()); // Close the form

        // Set visibility
        setVisible(true);
    }

    private void saveToDatabase(String doctor, String date, String time) {
        // SQL insert query
        String sql = "INSERT INTO appointments (doctor, date, time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctor);
            stmt.setString(2, date);
            stmt.setString(3, time);

            // Execute the query
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Appointment booked with Dr. " + doctor + " on " + date + " at " + time);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book appointment.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving appointment to the database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Create and show the booking form
        new BookingForm();
    }
}

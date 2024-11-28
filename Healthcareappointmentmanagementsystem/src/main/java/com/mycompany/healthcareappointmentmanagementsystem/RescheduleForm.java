package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RescheduleForm extends JFrame {

    // Instance variable for holding the database connection
    private Connection connection;

    public RescheduleForm() {
        // Get the database connection from the DatabaseConnection class
        this.connection = DatabaseConnection.getConnection();

        if (this.connection == null) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database.");
            return;
        }

        // Set up the frame
        setTitle("Reschedule Appointment");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Create panel and layout
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        // Create input fields
        JLabel appointmentIdLabel = new JLabel("Appointment ID:");
        JTextField appointmentIdField = new JTextField();

        JLabel newDateLabel = new JLabel("New Date (YYYY-MM-DD):");
        JTextField newDateField = new JTextField();

        JLabel newTimeLabel = new JLabel("New Time (HH:MM):");
        JTextField newTimeField = new JTextField();

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        // Add components to panel
        panel.add(appointmentIdLabel);
        panel.add(appointmentIdField);
        panel.add(newDateLabel);
        panel.add(newDateField);
        panel.add(newTimeLabel);
        panel.add(newTimeField);
        panel.add(submitButton);
        panel.add(cancelButton);

        // Add panel to frame
        add(panel);

        // Action listeners
        submitButton.addActionListener(e -> {
            // Collect inputs
            String appointmentId = appointmentIdField.getText();
            String newDate = newDateField.getText();
            String newTime = newTimeField.getText();

            // Validate inputs (you can add more validation as needed)
            if (appointmentId.isEmpty() || newDate.isEmpty() || newTime.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            // Update in database
            rescheduleAppointment(appointmentId, newDate, newTime);
        });

        cancelButton.addActionListener(e -> dispose()); // Close the form

        // Set visibility
        setVisible(true);
    }

    private void rescheduleAppointment(String appointmentId, String newDate, String newTime) {
        // SQL update query to reschedule the appointment
        String sql = "UPDATE appointments SET date = ?, time = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newDate);
            stmt.setString(2, newTime);
            stmt.setString(3, appointmentId);

            // Execute the update query
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Appointment " + appointmentId + " rescheduled to " + newDate + " at " + newTime);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reschedule appointment. Please check the appointment ID.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Create and show the reschedule form
        new RescheduleForm();
    }
}

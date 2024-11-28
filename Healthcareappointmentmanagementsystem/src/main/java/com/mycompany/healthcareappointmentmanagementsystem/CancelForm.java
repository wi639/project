package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CancelForm extends JFrame {

    // Instance variable for holding the database connection
    private Connection connection;

    public CancelForm() {
        // Get the database connection from the DatabaseConnection class
        this.connection = DatabaseConnection.getConnection();

        if (this.connection == null) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database.");
            return;
        }

        // Set up the frame
        setTitle("Cancel Appointment");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Create panel and layout
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        // Create input fields
        JLabel appointmentIdLabel = new JLabel("Appointment ID:");
        JTextField appointmentIdField = new JTextField();

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        // Add components to panel
        panel.add(appointmentIdLabel);
        panel.add(appointmentIdField);
        panel.add(submitButton);
        panel.add(cancelButton);

        // Add panel to frame
        add(panel);

        // Action listeners
        submitButton.addActionListener(e -> {
            // Collect inputs
            String appointmentId = appointmentIdField.getText();

            // Validate the appointmentId
            if (appointmentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the Appointment ID.");
                return;
            }

            // Call the method to cancel the appointment
            cancelAppointment(appointmentId);
        });

        cancelButton.addActionListener(e -> dispose()); // Close the form

        // Set visibility
        setVisible(true);
    }

    private void cancelAppointment(String appointmentId) {
        // SQL delete query to cancel the appointment
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, appointmentId);

            // Execute the delete query
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Appointment " + appointmentId + " canceled.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel appointment. Please check the Appointment ID.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error canceling appointment: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Create and show the cancel form
        new CancelForm();
    }
}

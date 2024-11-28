package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
import java.util.Arrays;

public class DoctorDashboard extends JFrame {

    private String doctorName;

    public DoctorDashboard(String doctorName) {
        this.doctorName = doctorName;
        setTitle("Doctor Dashboard");
        setSize(1200, 700);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Create panel and layout
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Label with doctor’s name
        JLabel label = new JLabel("Welcome, Dr. " + doctorName + "!", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.NORTH);

        // Fetch and display doctor's profile details
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new GridLayout(3, 1));  // Updated to display 3 items instead of 4

        // Fetch doctor details from the database
        Doctor doctor = getDoctorDetails(doctorName);
        if (doctor != null) {
            profilePanel.add(new JLabel("Specialty: " + doctor.getSpecialty()));
            profilePanel.add(new JLabel("Contact: " + doctor.getContactNumber()));
            profilePanel.add(new JLabel("Email: " + doctor.getEmail()));
        } else {
            profilePanel.add(new JLabel("Doctor details not found."));
        }

        // Add profile panel to the main panel
        panel.add(profilePanel, BorderLayout.WEST);

        // Table to display appointments
        JTable table = new JTable(getAppointmentsForDoctor(doctorName), 
                                  new Vector<>(Arrays.asList("Appointment ID", "Patient ID", "Date", "Time", "Status")));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel for action buttons (mark as completed and logout)
        JPanel actionPanel = new JPanel();
        JCheckBox completedCheckBox = new JCheckBox("Mark as Completed");
        JButton updateStatusButton = new JButton("Update Status");
        actionPanel.add(completedCheckBox);
        actionPanel.add(updateStatusButton);

        // Add action panel to the bottom of the frame
        panel.add(actionPanel, BorderLayout.SOUTH);

        // Add panel to frame
        add(panel);

        // Set visibility
        setVisible(true);

        // Action Listener for the "Update Status" button
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (completedCheckBox.isSelected()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int appointmentId = (int) table.getValueAt(selectedRow, 0);  // Get the appointment ID
                        updateAppointmentStatusToCompleted(appointmentId);  // Update status in the database
                        fetchAndDisplayAppointments();  // Refresh the table to show updated status
                    } else {
                        JOptionPane.showMessageDialog(DoctorDashboard.this, "Please select an appointment to update.");
                    }
                } else {
                    JOptionPane.showMessageDialog(DoctorDashboard.this, "Please check the 'Mark as Completed' checkbox.");
                }
            }
        });
    }

    // Fetch doctor's profile details from the database
    private Doctor getDoctorDetails(String doctorName) {
        String query = "SELECT first_name, last_name, specialty, contact_number, email FROM doctors WHERE CONCAT(first_name, ' ', last_name) = ?";
        Doctor doctor = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, doctorName);  
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                String specialty = rs.getString("specialty");
                String contactNumber = rs.getString("contact_number");
                String email = rs.getString("email");

                doctor = new Doctor(fullName, specialty, contactNumber, email);
            } else {
                JOptionPane.showMessageDialog(this, "Doctor not found in the database.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching doctor details: " + e.getMessage());
        }

        return doctor;
    }

    // Fetch appointments for the doctor from the database
    private Vector<Vector<Object>> getAppointmentsForDoctor(String doctorName) {
        Vector<Vector<Object>> data = new Vector<>();
        String query = "SELECT appointment_id, patient_id, appointment_date, appointment_time, status FROM appointments WHERE doctor_id = (SELECT doctor_id FROM doctors WHERE CONCAT(first_name, ' ', last_name) = ?)"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, doctorName);  
            ResultSet rs = stmt.executeQuery();

            // Loop through result set and populate the data vector
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("appointment_id"));
                row.add(rs.getInt("patient_id")); 
                row.add(rs.getDate("appointment_date")); 
                row.add(rs.getString("appointment_time")); 
                row.add(rs.getString("status")); 
                data.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data from database: " + e.getMessage());
        }
        return data;
    }

    // Update appointment status to "Completed"
    private void updateAppointmentStatusToCompleted(int appointmentId) {
        String query = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "Completed");
            stmt.setInt(2, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Appointment marked as completed.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating appointment status: " + e.getMessage());
        }
    }

    // Fetch and refresh appointments after updating the status
    private void fetchAndDisplayAppointments() {
        // Clear the table and re-fetch the data
        JTable table = new JTable(getAppointmentsForDoctor(doctorName),
                                  new Vector<>(Arrays.asList("Appointment ID", "Patient ID", "Date", "Time", "Status")));
        // Update the table with new data
        ((JScrollPane) getContentPane().getComponent(1)).setViewportView(new JScrollPane(table));
    }

    public static void main(String[] args) {
        // You can pass the doctor's name as a parameter when creating the dashboard
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Ensure you pass the correct full name (e.g., "Jane Smith")
                new DoctorDashboard("Jane Smith").setVisible(true);  
            }
        });
    }
}

// Doctor class to hold doctor details
class Doctor {
    private String name;
    private String specialty;
    private String contactNumber;
    private String email;

    public Doctor(String name, String specialty, String contactNumber, String email) {
        this.name = name;
        this.specialty = specialty;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }
}



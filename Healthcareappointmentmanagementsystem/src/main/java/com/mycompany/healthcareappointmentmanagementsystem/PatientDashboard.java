package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class PatientDashboard extends JFrame {

    private JComboBox<String> doctorComboBox;
    private JTextField dateTextField;  // Allow patient to manually input date
    private JTextField timeTextField;  // Allow patient to manually input time
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public PatientDashboard() {
        // Set up the frame
        setTitle("Patient Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Create panel and layout for combo boxes and buttons
        JPanel selectionPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Initialize combo boxes and text fields
        doctorComboBox = new JComboBox<>();
        dateTextField = new JTextField();  // Allow free date input
        timeTextField = new JTextField();  // Allow free time input

        // Populate doctors
        populateDoctors();

        // Add components to selection panel
        selectionPanel.add(new JLabel("Select Doctor:"));
        selectionPanel.add(doctorComboBox);
        selectionPanel.add(new JLabel("Enter Date (YYYY-MM-DD):"));
        selectionPanel.add(dateTextField);
        selectionPanel.add(new JLabel("Enter Time (HH:MM):"));
        selectionPanel.add(timeTextField);

        // Create buttons
        JButton bookButton = new JButton("Book Appointment");
        JButton viewBookingsButton = new JButton("View Booking Details");
        JButton logoutButton = new JButton("Logout");
        JButton cancelButton = new JButton("Cancel Appointment");
        JButton rescheduleButton = new JButton("Reschedule Appointment");

        // Add buttons to button panel
        buttonPanel.add(bookButton);
        buttonPanel.add(viewBookingsButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(rescheduleButton);

        // Add panels to frame
        add(selectionPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Table for displaying bookings
        String[] columnNames = {"Appointment ID", "Doctor", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bookingTable = new JTable(tableModel);
        add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // Action Listeners
        bookButton.addActionListener(e -> bookAppointment());
        viewBookingsButton.addActionListener(e -> fetchAndDisplayBookings());
        logoutButton.addActionListener(e -> logout());
        cancelButton.addActionListener(e -> cancelAppointment());
        rescheduleButton.addActionListener(e -> rescheduleAppointment());

        // Set visibility
        setVisible(true);
    }

    // Populate doctors combo box
    private void populateDoctors() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT first_name, last_name FROM doctors";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String doctorName = rs.getString("first_name") + " " + rs.getString("last_name");
                doctorComboBox.addItem(doctorName);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching doctor list: " + e.getMessage());
        }
    }

    // Book appointment functionality
    private void bookAppointment() {
        String doctor = (String) doctorComboBox.getSelectedItem();
        String date = dateTextField.getText();  // Get user input for date
        String time = timeTextField.getText();  // Get user input for time

        if (doctor != null && !date.isEmpty() && !time.isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO appointments (doctor_id, patient_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?)")) {

                // Replace with appropriate IDs
                int doctorId = getDoctorIdByName(doctor);
                int patientId = getPatientId();  // Method to get the current logged-in patient ID

                stmt.setInt(1, doctorId);
                stmt.setInt(2, patientId);
                stmt.setDate(3, java.sql.Date.valueOf(date));  // Convert string to Date
                stmt.setString(4, time);
                stmt.setString(5, "Pending"); // Set status to Pending for admin confirmation

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Appointment booked successfully with Dr. " + doctor + " on " + date + " at " + time + ". Waiting for admin confirmation.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to book appointment. Try again.");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error booking appointment: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select doctor, date, and time.");
        }
    }

    // Utility method to get doctor_id by name
    private int getDoctorIdByName(String doctorName) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT doctor_id FROM doctors WHERE CONCAT(first_name, ' ', last_name) = ?")) {

            stmt.setString(1, doctorName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching doctor ID: " + e.getMessage());
        }
        return -1; // Return an invalid ID if not found
    }

    // Utility method to get patient_id (Assuming patient is logged in)
    private int getPatientId() {
        // Logic to get the patient ID, assuming the logged-in patient info is stored somewhere.
        return 1;  // Replace with actual patient ID from session or context
    }

    // Fetch and display bookings in the table
    private void fetchAndDisplayBookings() {
        tableModel.setRowCount(0); // Clear existing rows
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT a.appointment_id, CONCAT(d.first_name, ' ', d.last_name) AS doctor, a.appointment_date, a.appointment_time, a.status "
                             + "FROM appointments a "
                             + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                             + "WHERE a.patient_id = ?")) {

            stmt.setInt(1, getPatientId());  // Fetching appointments for the logged-in patient
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("appointment_id"),
                        rs.getString("doctor"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching booking details: " + e.getMessage());
        }
    }

    // Cancel Appointment functionality
    private void cancelAppointment() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            int appointmentId = (int) tableModel.getValueAt(selectedRow, 0); // Get appointment ID
            int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?", "Cancel Appointment", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ?")) {

                    stmt.setInt(1, appointmentId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                        fetchAndDisplayBookings();  // Refresh the table
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to cancel appointment.");
                    }

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
        }
    }

    // Reschedule Appointment functionality
    private void rescheduleAppointment() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            int appointmentId = (int) tableModel.getValueAt(selectedRow, 0); // Get appointment ID
            String newDate = dateTextField.getText();
            String newTime = timeTextField.getText();
            if (!newDate.isEmpty() && !newTime.isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE appointments SET appointment_date = ?, appointment_time = ? WHERE appointment_id = ?")) {

                    stmt.setDate(1, java.sql.Date.valueOf(newDate));  // Convert string to Date
                    stmt.setString(2, newTime);
                    stmt.setInt(3, appointmentId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully.");
                        fetchAndDisplayBookings();  // Refresh the table
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to reschedule appointment.");
                    }

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a new date and time.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.");
        }
    }

    // Logout functionality
    private void logout() {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?");
        if (option == JOptionPane.YES_OPTION) {
            dispose(); 
            new LoginForm(); 
        }
    }

    public static void main(String[] args) {
        new PatientDashboard();
    }
}

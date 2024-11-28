package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.util.Arrays;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        // Set up JFrame properties
        setTitle("Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  
        
        // Create panel and layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 20, 20)); 
        
        // Create UI components
        JLabel welcomeLabel = new JLabel("Welcome, Admin!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(welcomeLabel);

        // Buttons for admin functionalities
        JButton manageBookingsButton = new JButton("Manage Bookings");
        JButton bookPatientButton = new JButton("Book Patient");
        JButton logoutButton = new JButton("Logout");

        // Set font and button dimensions
        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        Dimension buttonSize = new Dimension(300, 50);
        
        manageBookingsButton.setFont(buttonFont);
        bookPatientButton.setFont(buttonFont);
        logoutButton.setFont(buttonFont);
        
        manageBookingsButton.setPreferredSize(buttonSize);
        bookPatientButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
        
        // Add buttons to panel
        panel.add(manageBookingsButton);
        panel.add(bookPatientButton);
        panel.add(logoutButton);

        // Center the panel's contents
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add panel to frame with padding
        add(panel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 

        // Action listeners for the buttons
        manageBookingsButton.addActionListener(e -> manageBookings());
        bookPatientButton.addActionListener(e -> bookPatient());
        logoutButton.addActionListener(e -> logout());

        // Set visibility
        setVisible(true);
    }

    // Manage bookings functionality
    private void manageBookings() {
        JFrame manageBookingsFrame = new JFrame("Manage Bookings");
        manageBookingsFrame.setSize(600, 400);
        manageBookingsFrame.setLocationRelativeTo(null);
        
        // Create a table to show appointments
        String[] columnNames = {"ID", "Doctor", "Patient ID", "Date", "Time", "Status"};
        Vector<Vector<Object>> data = getBookingsFromDatabase(); 
        
        JTable table = new JTable(data, new Vector<>(Arrays.asList(columnNames)));
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Create buttons for confirming/canceling bookings
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout());
        
        JButton confirmButton = new JButton("Confirm Booking");
        JButton cancelButton = new JButton("Cancel Booking");

        actionPanel.add(confirmButton);
        actionPanel.add(cancelButton);

        // Action listeners for the buttons
        confirmButton.addActionListener(e -> handleBookingAction(table, "Confirm"));
        cancelButton.addActionListener(e -> handleBookingAction(table, "Cancel"));

        // Add the table and buttons to the frame
        manageBookingsFrame.setLayout(new BorderLayout());
        manageBookingsFrame.add(scrollPane, BorderLayout.CENTER);
        manageBookingsFrame.add(actionPanel, BorderLayout.SOUTH);
        
        manageBookingsFrame.setVisible(true);
    }

    // Handle Confirm or Cancel booking action
    // Handle Confirm or Cancel booking action
private void handleBookingAction(JTable table, String action) {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a booking first.");
        return;
    }

    int appointmentId = (Integer) table.getValueAt(selectedRow, 0);
    String status = (String) table.getValueAt(selectedRow, 5);

    if ("Confirm".equals(action)) {
        if ("Pending".equals(status)) {
            // If the booking is in "Pending" state, confirm it
            updateBookingStatus(appointmentId, "Scheduled");
            JOptionPane.showMessageDialog(this, "Booking confirmed and scheduled.");
        } else if ("Scheduled".equals(status)) {
            // If the booking is already scheduled, show message
            JOptionPane.showMessageDialog(this, "Booking is already scheduled.");
        } else if ("Cancelled".equals(status)) {
            // If the booking is canceled, it cannot be confirmed
            JOptionPane.showMessageDialog(this, "Booking is canceled. It cannot be confirmed.");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid booking status for confirmation.");
        }
    } else if ("Cancel".equals(action)) {
        if ("Pending".equals(status)) {
            // If the booking is in "Pending" state, cancel it
            updateBookingStatus(appointmentId, "Cancelled");
            JOptionPane.showMessageDialog(this, "Booking canceled.");
        } else if ("Cancelled".equals(status)) {
            // If the booking is already canceled, show message
            JOptionPane.showMessageDialog(this, "Booking is already canceled.");
        } else if ("Scheduled".equals(status)) {
            // If the booking is scheduled, cancel it
            updateBookingStatus(appointmentId, "Cancelled");
            JOptionPane.showMessageDialog(this, "Booking canceled.");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid booking status for cancellation.");
        }
    }
}


    // Update booking status in the database
    private void updateBookingStatus(int appointmentId, String newStatus) {
        String query = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Booking status updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update booking status.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
        }
    }

    // Method to fetch booking data from the database
    private Vector<Vector<Object>> getBookingsFromDatabase() {
        Vector<Vector<Object>> data = new Vector<>();
        String query = "SELECT a.appointment_id, " +
                       "CONCAT(d.first_name, ' ', d.last_name) AS doctor_name, " +
                       "a.patient_id, a.appointment_date AS date, " +
                       "a.appointment_time AS time, a.status " +
                       "FROM appointments a " +
                       "JOIN doctors d ON a.doctor_id = d.doctor_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("appointment_id"));
                row.add(rs.getString("doctor_name"));
                row.add(rs.getInt("patient_id"));
                row.add(rs.getDate("date"));
                row.add(rs.getString("time"));
                row.add(rs.getString("status"));
                data.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data from database: " + e.getMessage());
        }
        return data;
    }

    // Book patient functionality
    private void bookPatient() {
        String[] doctors = getAvailableDoctors();

        if (doctors == null || doctors.length == 0) {
            JOptionPane.showMessageDialog(this, "No doctors available for booking.");
            return;
        }

        String doctor = (String) JOptionPane.showInputDialog(this,
                "Select a doctor:",
                "Doctor Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                doctors,
                doctors[0]);

        if (doctor == null) return; 

        String patientId = JOptionPane.showInputDialog(this, "Enter Patient ID:");
        if (patientId == null || patientId.isEmpty()) return;

        String date = JOptionPane.showInputDialog(this, "Enter the date (YYYY-MM-DD):");
        if (date == null || date.isEmpty()) return;

        String time = JOptionPane.showInputDialog(this, "Enter the time (HH:MM):");
        if (time == null || time.isEmpty()) return;

        if (!isDoctorAvailable(doctor, date, time)) {
            JOptionPane.showMessageDialog(this, "The doctor is not available at this time.");
            return;
        }

        scheduleAppointment(doctor, patientId, date, time);
    }

    // Fetch available doctors from the database
    private String[] getAvailableDoctors() {
        String query = "SELECT CONCAT(first_name, ' ', last_name) AS doctor_name FROM doctors"; 

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> doctors = new Vector<>();
            while (rs.next()) {
                doctors.add(rs.getString("doctor_name"));
            }

            return doctors.toArray(new String[0]);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching doctors: " + e.getMessage());
            return null;
        }
    }

    private boolean isDoctorAvailable(String doctor, String date, String time) {
        String query = "SELECT COUNT(*) FROM appointments a " +
                       "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                       "WHERE CONCAT(d.first_name, ' ', d.last_name) = ? AND a.appointment_date = ? AND a.appointment_time = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, doctor);
            stmt.setString(2, date);
            stmt.setString(3, time);

            ResultSet rs = stmt.executeQuery();
            return !(rs.next() && rs.getInt(1) > 0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error checking availability: " + e.getMessage());
            return false;
        }
    }

    // Schedule a new appointment
    private void scheduleAppointment(String doctor, String patientId, String date, String time) {
        String query = "INSERT INTO appointments (doctor_id, patient_id, appointment_date, appointment_time, status) " +
                       "VALUES ((SELECT doctor_id FROM doctors WHERE CONCAT(first_name, ' ', last_name) = ?), ?, ?, ?, 'Pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, doctor);
            stmt.setInt(2, Integer.parseInt(patientId));
            stmt.setString(3, date);
            stmt.setString(4, time);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to schedule appointment.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error scheduling appointment: " + e.getMessage());
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
        new AdminDashboard();
    }
}

package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReceptionistDashboard extends JFrame {
    public ReceptionistDashboard() {
        setTitle("Receptionist Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 20, 20));

        JButton viewAppointments = new JButton("View Appointments");
        JButton managePatients = new JButton("Manage Patients");
        JButton logout = new JButton("Logout");

        panel.add(viewAppointments);
        panel.add(managePatients);
        panel.add(logout);

        add(panel);

        // Example button action using DatabaseConnection.getConnection()
        viewAppointments.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    JOptionPane.showMessageDialog(this, "Database connected successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to connect to database.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });

        logout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logging out...");
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new ReceptionistDashboard();
    }
}

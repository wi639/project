
package com.mycompany.healthcareappointmentmanagementsystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    // Declare components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRole;
    private JButton btnLogin;

    public LoginForm() {
        // Set up JFrame properties
        setTitle("Hospital Patient Records System - Login");
        setSize(1200, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        // Create panel and layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());  
        panel.setBackground(new Color(240, 248, 255)); 
        // Create GridBagConstraints to control component positioning
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 

        // Create and add components
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsername.setForeground(new Color(0, 51, 102)); 
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setForeground(new Color(0, 51, 102));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtPassword, gbc);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(new Font("Arial", Font.BOLD, 14));
        lblRole.setForeground(new Color(0, 51, 102));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblRole, gbc);

        String[] roles = {"Admin", "Doctor","Receptionist","Patient"};
        cbRole = new JComboBox<>(roles);
        cbRole.setFont(new Font("Arial", Font.PLAIN, 14));
        cbRole.setBackground(new Color(255, 255, 255)); 
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(cbRole, gbc);

        // Login Button with hospital-themed color and styling
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(0, 204, 102)); 
        btnLogin.setForeground(Color.WHITE);  
        btnLogin.setFocusPainted(false); 
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(btnLogin, gbc);

        // Add panel to frame
        add(panel);

        // Set login button action
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginAction();
            }
        });
    }

    private void loginAction() {
    String username = txtUsername.getText();
    String password = new String(txtPassword.getPassword());
    String role = cbRole.getSelectedItem().toString();

    try (Connection conn = DatabaseConnection.getConnection()) {
        // Check for Admin login
        if (role.equals("Admin")) {
            handleAdminLogin(password);
            return;
        }

        // Check for Patient login
        if (role.equals("Patient")) {
            handlePatientLogin(username, password);
            return;
        }

        // For Doctor or Receptionist login, fetch credentials from the database
        if (role.equals("Doctor") || role.equals("Receptionist")) {
            handleDoctorOrReceptionistLogin(username, password, role, conn);
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage());
    }
}

private void handleAdminLogin(String password) {
    String adminPassword = "admin123"; 
    if (password.equals(adminPassword)) {
        JOptionPane.showMessageDialog(this, "Admin login successful!");
        new AdminDashboard().setVisible(true);
        dispose(); // Close the login window
    } else {
        JOptionPane.showMessageDialog(this, "Invalid admin password. Please try again.");
    }
}

private void handlePatientLogin(String username, String password) {
    String patientUsername = "patient1"; 
    String patientPassword = "patient123"; 

    if (username.equals(patientUsername) && password.equals(patientPassword)) {
        JOptionPane.showMessageDialog(this, "Patient login successful!");
        new PatientDashboard().setVisible(true);
        dispose(); // Close the login window
    } else {
        JOptionPane.showMessageDialog(this, "Invalid patient credentials. Please try again.");
    }
}

private void handleDoctorOrReceptionistLogin(String username, String password, String role, Connection conn) throws SQLException {
    String sql = "";

    if (role.equals("Doctor")) {
        // Query the doctors table for doctor credentials
        sql = "SELECT * FROM doctors WHERE username = ? AND password = ? AND role = ?";
    } else if (role.equals("Receptionist")) {
        // For Receptionist, use the users table (or adjust if you have a separate table for receptionists)
        sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
    }

    try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, username);
        pst.setString(2, password);
        pst.setString(3, role);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + role);

                if (role.equals("Doctor")) {
                    String doctorName = rs.getString("first_name") + " " + rs.getString("last_name");
                    new DoctorDashboard(doctorName).setVisible(true);
                } else {
                    new ReceptionistDashboard().setVisible(true);
                }

                dispose(); // Close the login window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
            }
        }
    }
}



    public static void main(String[] args) {
        // Run the login form
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}


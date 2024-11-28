/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.healthcareappointmentmanagementsystem;

import javax.swing.SwingUtilities;

/**
 *
 * @author pc
 */
public class Healthcareappointmentmanagementsystem {
   public static void main(String[] args) {
        // Start the login form on application startup
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Rhynne Gracelle
 */
package gui;

import model.Employee;
import repository.DbEmployeeRepository;
import repository.EmployeeRepository;

import javax.swing.*;

public class MainDashboardLauncher {

    public static void launch(Employee loggedInEmployee) {
        EmployeeRepository repo = new DbEmployeeRepository();

        SwingUtilities.invokeLater(() ->
                new MainDashboardFrame(repo, loggedInEmployee).setVisible(true)
        );
    }
}
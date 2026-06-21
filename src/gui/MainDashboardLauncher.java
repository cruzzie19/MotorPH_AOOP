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
import repository.CredentialRepository;
import repository.DbCredentialRepository;
import service.auth.AccountService;

public class MainDashboardLauncher {

    public static void launch(Employee loggedInEmployee) {
        EmployeeRepository empRepo = new DbEmployeeRepository();
        AccountService accServ = AccountService.createDefault();
        CredentialRepository credRepo = new DbCredentialRepository();

        SwingUtilities.invokeLater(() ->
                new MainDashboardFrame(empRepo, loggedInEmployee, accServ, credRepo).setVisible(true)
        );
    }
}
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
import repository.CsvEmployeeRepository;
import repository.EmployeeRepository;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainDashboardLauncher {

    public static void launch(Employee loggedInEmployee) {
        Path csvPath = resolveEmployeeCsvPath();
        EmployeeRepository repo = new CsvEmployeeRepository(csvPath.toString());

        SwingUtilities.invokeLater(() ->
                new MainDashboardFrame(repo, csvPath, loggedInEmployee).setVisible(true)
        );
    }

    private static Path resolveEmployeeCsvPath() {
        String fileName = "MotorPH Employee Record.csv";
        Path[] candidates = new Path[]{
                Paths.get("data", fileName),
                Paths.get("src", "data", fileName),
                Paths.get(System.getProperty("user.dir"), "data", fileName),
                Paths.get(System.getProperty("user.dir"), "src", "data", fileName)
        };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        return candidates[1].toAbsolutePath().normalize();
    }
}
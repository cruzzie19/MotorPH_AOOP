/**
 *
 * @author Leianna Cruz
 */

package gui;

import model.Employee;
import repository.BulkAccountGenerator;
import repository.DbEmployeeRepository;
import repository.EmployeeRepository;

import javax.swing.*;

public class EmployeeManagementLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {


            JFrame dummy = new JFrame();
            dummy.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            LoginDialog loginDialog = new LoginDialog(dummy);
            loginDialog.setVisible(true);

            if (!loginDialog.isSucceeded()) {
                dummy.dispose();
                System.exit(0);
                return;
            }

            Employee loggedInEmployee = loginDialog.getLoggedInEmployee();
            dummy.dispose();

            // Launch the new CardLayout dashboard
            MainDashboardLauncher.launch(loggedInEmployee);
        });
    }
}
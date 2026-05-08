/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Employee;
import service.auth.AccountService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BulkAccountGenerator {

    public static void generateAccounts(CsvEmployeeRepository repo) throws IOException {

        AccountService accountService = new AccountService(repo);
        List<Employee> employees = repo.loadAll();

        for (Employee e : employees) {

            String empId = e.getId();
            String firstName = e.getFirstName();

            if (empId == null || firstName == null) {
                continue;
            }

            empId = empId.trim();
            firstName = firstName.trim();

            if (empId.isEmpty() || firstName.isEmpty()) {
                continue;
            }

            String password = empId + Character.toUpperCase(firstName.charAt(0));

            accountService.registerOrUpdate(empId, password.toCharArray());

            System.out.println("Created account -> Username: "
                    + empId + " | Password: " + password);
        }

        System.out.println("All employee accounts created.");
    }
}
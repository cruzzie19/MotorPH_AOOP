/**
 *
 * @author Leianna Cruz
 */

package service.auth;

import model.Employee;
import repository.EmployeeRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class AccountService {

    private static final DateTimeFormatter BIRTHDAY_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yyyy");

    private final PasswordManager passwordManager;
    private final EmployeeRepository employeeRepository;

    public AccountService(EmployeeRepository employeeRepository) {
        this.passwordManager = new PasswordManager();
        this.employeeRepository = employeeRepository;
    }

    public boolean hasAccounts() {
        try {
            return passwordManager.hasAccounts();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean registerOrUpdate(String username, char[] password) {

        try {
            passwordManager.upsertAccount(username, password);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    public boolean validate(String username, char[] password) {

        try {
            return passwordManager.validate(username, password);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean changePassword(String username,
                                  char[] oldPassword,
                                  char[] newPassword) {

        try {
            return passwordManager.changePassword(username, oldPassword, newPassword);
        } catch (IOException e) {
            return false;
        } finally {

            if (oldPassword != null) Arrays.fill(oldPassword, '\0');
            if (newPassword != null) Arrays.fill(newPassword, '\0');
        }
    }

    public boolean registerEmployeeAccount(String employeeId,
                                           String birthDateText,
                                           char[] password) {

        try {

            if (employeeId == null || employeeId.trim().isEmpty()
                    || birthDateText == null || birthDateText.trim().isEmpty()
                    || password == null || password.length == 0) {
                return false;
            }

            LocalDate birthDate;

            try {
                birthDate = LocalDate.parse(birthDateText.trim(), BIRTHDAY_FORMAT);
            } catch (DateTimeParseException ex) {
                return false;
            }

            List<Employee> employees = employeeRepository.loadAll();

            for (Employee employee : employees) {

                if (employeeId.trim().equals(employee.getId())
                        && birthDate.equals(employee.getBirthDate())) {

                    passwordManager.upsertAccount(employeeId.trim(), password);
                    return true;
                }
            }

            return false;

        } catch (IOException | IllegalArgumentException e) {
            return false;
        } finally {

            if (password != null) Arrays.fill(password, '\0');
        }
    }

    public Employee findEmployeeByUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {

            List<Employee> employees = employeeRepository.loadAll();

            for (Employee employee : employees) {

                if (username.trim().equalsIgnoreCase(employee.getId())) {
                    return employee;
                }
            }

        } catch (IOException e) {
            return null;
        }

        return null;
    }
    
    public static AccountService createDefault() {

        repository.CsvEmployeeRepository repo =
                new repository.CsvEmployeeRepository("data/MotorPH Employee Record.csv");

        return new AccountService(repo);
    }
}
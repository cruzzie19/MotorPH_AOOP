/**
 *
 * @author Leianna Cruz
 * @author Khaesey Angel Tablante
 * @desc Khaesey
 */

package service;

import RBAC.Permission;
import model.Employee;
import model.Payslip;
import repository.PayrollRepository;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Data-handling service for {@link Payslip} records.
 *
 * <p>This class deliberately does <b>not</b> perform any payroll math -
 * that responsibility belongs to {@link PayrollComputationService}. This
 * service only saves, retrieves, lists, and deletes already-computed
 * payslips through a {@link PayrollRepository}, translating checked
 * {@link IOException}s into unchecked {@link RuntimeException}s so callers
 * in the GUI layer don't need to handle them directly (consistent with how
 * {@code EmployeeService} and {@code LeaveService} are used elsewhere in
 * this project).</p>
 */
public class PayslipService {

    private final PayrollRepository repository;

    public PayslipService(PayrollRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves (inserts or updates) a payslip.
     *
     * @param payslip payslip to persist
     */
    public void savePayslip(Payslip payslip) {
        if (payslip == null) {
            throw new IllegalArgumentException("Payslip is required.");
        }
        if (payslip.getEmployeeId() == null || payslip.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Payslip is missing an employee ID.");
        }
        if (payslip.getPayrollMonth() == null) {
            throw new IllegalArgumentException("Payslip is missing a payroll month.");
        }

        try {
            repository.save(payslip);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payslip.", e);
        }
    }

    /**
     * Retrieves the previously generated payslip for an employee and month,
     * if one exists.
     *
     * @param employeeId employee ID
     * @param month payroll month
     * @return matching payslip, or {@code null} if none has been generated yet
     */
    public Payslip getPayslip(String employeeId, YearMonth month) {
        if (employeeId == null || employeeId.trim().isEmpty() || month == null) {
            return null;
        }

        try {
            return repository.findByEmployeeAndMonth(employeeId.trim(), month);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payslip.", e);
        }
    }

    /**
     * Retrieves every payslip generated for a specific employee.
     *
     * @param employeeId employee ID
     * @return matching payslips
     */
    public List<Payslip> getPayslipsForEmployee(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return repository.findByEmployeeId(employeeId.trim());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payslips for employee.", e);
        }
    }

    /**
     * Retrieves every payslip generated for a specific payroll month
     * (across all employees).
     *
     * @param month payroll month
     * @return matching payslips
     */
    public List<Payslip> getPayslipsForMonth(YearMonth month) {
        if (month == null) {
            return Collections.emptyList();
        }

        try {
            return repository.findByMonth(month);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payslips for month.", e);
        }
    }

    /**
     * Retrieves every payslip on record.
     *
     * @return all payslips
     */
    public List<Payslip> getAllPayslips() {
        try {
            return repository.findAll();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load payslips.", e);
        }
    }

    /**
     * 
     * Retrieves the payslips a given user is permitted to see for reporting
     * purposes: employees with broader payroll-viewing permissions see every
     * payslip on record (most recent month first), while everyone else only
     * sees their own.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee requesting the report
     * @return visible payslips, most recent payroll month first
     */
    public List<Payslip> getVisiblePayslips(Employee currentUser) {
        validateEmployee(currentUser);

        List<Payslip> payslips = canViewBroaderPayroll(currentUser)
                ? getAllPayslips()
                : getPayslipsForEmployee(currentUser.getId());

        payslips.sort(Comparator.comparing(Payslip::getPayrollMonth,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return payslips;
    }

    /**
     * 
     * Retrieves the payslips for a specific target employee, enforcing that
     * a user without broader payroll-viewing permissions may only request
     * their own records.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee requesting the report
     * @param targetEmployeeId the employee whose payslips are being requested
     * @return visible payslips, most recent payroll month first
     */
    public List<Payslip> getVisiblePayslipsForEmployee(Employee currentUser, String targetEmployeeId) {
        validateEmployee(currentUser);

        if (!canViewBroaderPayroll(currentUser) && !isOwnRecord(currentUser, targetEmployeeId)) {
            throw new IllegalArgumentException("You can only view your own payroll records.");
        }

        List<Payslip> payslips = getPayslipsForEmployee(targetEmployeeId);
        payslips.sort(Comparator.comparing(Payslip::getPayrollMonth,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return payslips;
    }

    /**
     * 
     * Whether the given user now can view payroll/payslip records belonging to
     * other employees (e.g. HR, Payroll, Accounting, Executive roles), as
     * opposed to only their own.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee
     * @return {@code true} if the user has broader payroll-viewing permission
     */
    public boolean canViewBroaderPayroll(Employee currentUser) {
        return AuthorizationService.hasPermission(currentUser, Permission.VIEW_PAYROLL)
                || AuthorizationService.hasPermission(currentUser, Permission.VIEW_PAYSLIP);
    }

    private boolean isOwnRecord(Employee currentUser, String employeeId) {
        if (currentUser == null || employeeId == null) {
            return false;
        }
        return employeeId.trim().equalsIgnoreCase(currentUser.getId().trim());
    }

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }
        if (employee.getId() == null || employee.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
    }

    /**
     * Deletes the payslip for a specific employee and payroll month.
     *
     * @param employeeId employee ID
     * @param month payroll month
     */
    public void deletePayslip(String employeeId, YearMonth month) {
        if (employeeId == null || employeeId.trim().isEmpty() || month == null) {
            return;
        }

        try {
            repository.delete(employeeId.trim(), month);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete payslip.", e);
        }
    }
}

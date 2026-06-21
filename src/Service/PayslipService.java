/**
 *
 * @author Leianna Cruz
 */

package service;

import model.Payslip;
import repository.PayrollRepository;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Collections;
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

/**
 *
 * @author Leianna Cruz
 * @author Khaesey Angel Tablante
 */

package service;

import RBAC.Permission;
import model.Employee;
import model.Payslip;
import model.PayrollSummary;
import repository.PayrollRepository;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private EmployeeService employeeService;

    public PayslipService(PayrollRepository repository) {
        this.repository = repository;
    }

    public PayslipService(PayrollRepository repository, EmployeeService employeeService) {
        this.repository = repository;
        this.employeeService = employeeService;
    }

    /**
     * Allows the department lookup dependency to be supplied after
     * construction, for call sites that build {@code PayslipService} via
     * the single-argument constructor.
     *
     * @param employeeService used to resolve each employee's role/department
     */
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
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
     * Builds a payroll summary grouped by department (an employee's
     * {@link RBAC.Role}), covering every payslip on record. Only available
     * to users with broader payroll-viewing permission; throws otherwise.
     * Requires an {@link EmployeeService} to have been supplied (via the
     * two-argument constructor or {@link #setEmployeeService}) so each
     * payslip's employee can be resolved to a department.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee requesting the summary
     * @return one {@link PayrollSummary} per department, sorted alphabetically
     */
    public List<PayrollSummary> getPayrollSummaryByDepartment(Employee currentUser) {
        validateEmployee(currentUser);

        if (!canViewBroaderPayroll(currentUser)) {
            throw new IllegalArgumentException("You do not have permission to view the department payroll summary.");
        }
        if (employeeService == null) {
            throw new IllegalStateException("PayslipService is missing its EmployeeService dependency "
                    + "needed to resolve departments. Call setEmployeeService(...) first.");
        }

        List<Payslip> payslips = getAllPayslips();

        Map<String, PayrollSummary> summaries = new LinkedHashMap<>();
        for (Payslip payslip : payslips) {
            String department = resolveDepartment(payslip.getEmployeeId());

            PayrollSummary summary = summaries.computeIfAbsent(department, PayrollSummary::new);
            summary.setPayslipCount(summary.getPayslipCount() + 1);
            summary.setTotalGrossPay(nz(summary.getTotalGrossPay()).add(nz(payslip.getGrossPay())));
            summary.setTotalDeductions(nz(summary.getTotalDeductions()).add(nz(payslip.getTotalDeductions())));
            summary.setTotalNetPay(nz(summary.getTotalNetPay()).add(nz(payslip.getNetPay())));
        }

        for (Map.Entry<String, PayrollSummary> entry : summaries.entrySet()) {
            long uniqueEmployees = payslips.stream()
                    .filter(p -> entry.getKey().equals(resolveDepartment(p.getEmployeeId())))
                    .map(Payslip::getEmployeeId)
                    .distinct()
                    .count();
            entry.getValue().setEmployeeCount((int) uniqueEmployees);
        }

        List<PayrollSummary> result = new ArrayList<>(summaries.values());
        result.sort(Comparator.comparing(PayrollSummary::getDepartment));
        return result;
    }

    private String resolveDepartment(String employeeId) {
        Employee employee = employeeService.findById(employeeId);
        if (employee == null || employee.getRole() == null) {
            return "Unassigned";
        }
        String roleName = employee.getRole().getName();
        return (roleName == null || roleName.trim().isEmpty()) ? "Unassigned" : roleName.trim();
    }

    private java.math.BigDecimal nz(java.math.BigDecimal value) {
        return value == null ? java.math.BigDecimal.ZERO : value;
    }


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

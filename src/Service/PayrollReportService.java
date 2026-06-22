/**
 *
 * @author Khaesey Angel Tablante
 */

package service;

import model.Employee;
import model.Payslip;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates payroll/payslip reports using JasperReports, respecting the
 * same RBAC rules enforced by {@link PayslipService}: employees without
 * broader payroll-viewing permission can only generate a report of their
 * own payslips, while employees with that permission (HR, Payroll,
 * Accounting, Executive) can generate a summarized report covering every
 * employee's payslips.
 */
public class PayrollReportService {

    private static final String TEMPLATE_PATH = "/reports/payroll_report.jrxml";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private final PayslipService payslipService;

    public PayrollReportService(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    /**
     * Generates and displays the payroll report visible to the given user.
     * Users with broader payroll-viewing permission get every employee's
     * payslips, grouped and summarized by employee; everyone else gets a
     * report of only their own payslips.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateAndShowReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        boolean broaderView = payslipService.canViewBroaderPayroll(currentUser);

        List<Payslip> payslips = payslipService.getVisiblePayslips(currentUser);

        String title = broaderView
                ? "MotorPH Payroll Report - All Employees"
                : "MotorPH Payroll Report - " + currentUser.getFullName();

        showReport(payslips, title, currentUser);
    }

    /**
     * Generates and displays a payroll report scoped to one specific
     * employee, honoring the requesting user's RBAC permissions (delegated
     * to {@link PayslipService#getVisiblePayslipsForEmployee}).
     *
     * @param currentUser the logged-in employee requesting the report
     * @param targetEmployeeId the employee whose payslips are being reported on
     */
    public void generateAndShowReportForEmployee(Employee currentUser, String targetEmployeeId) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<Payslip> payslips =
                payslipService.getVisiblePayslipsForEmployee(currentUser, targetEmployeeId);

        String title = "MotorPH Payroll Report - " + targetEmployeeId;

        showReport(payslips, title, currentUser);
    }

    private void showReport(List<Payslip> payslips, String title, Employee currentUser) {
        try {
            List<PayrollReportRow> rows = toReportRows(payslips);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

            java.util.Map<String, Object> parameters = new java.util.HashMap<>();
            parameters.put("reportTitle", title);
            parameters.put("generatedBy", safe(currentUser.getFullName()));
            parameters.put("generatedOn", LocalDateTime.now().format(TIMESTAMP_FORMAT));

            net.sf.jasperreports.engine.design.JasperDesign design = loadDesign();
            net.sf.jasperreports.engine.JasperReport report = JasperCompileManager.compileReport(design);

            JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle(title);
            viewer.setVisible(true);

        } catch (JRException e) {
            throw new RuntimeException("Failed to generate payroll report.", e);
        }
    }

    private net.sf.jasperreports.engine.design.JasperDesign loadDesign() throws JRException {
        try (InputStream in = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (in == null) {
                throw new JRException("Payroll report template not found at " + TEMPLATE_PATH
                        + ". Make sure reports/payroll_report.jrxml is on the classpath.");
            }
            return net.sf.jasperreports.engine.xml.JRXmlLoader.load(in);
        } catch (java.io.IOException e) {
            throw new JRException("Failed to read payroll report template.", e);
        }
    }

    private List<PayrollReportRow> toReportRows(List<Payslip> payslips) {
        List<PayrollReportRow> rows = new ArrayList<>();
        for (Payslip payslip : payslips) {
            rows.add(new PayrollReportRow(
                    payslip.getEmployeeId(),
                    safe(payslip.getEmployeeName()),
                    payslip.getPayrollMonth() != null ? payslip.getPayrollMonth().toString() : "",
                    nz(payslip.getGrossPay()),
                    nz(payslip.getTotalDeductions()),
                    nz(payslip.getNetPay())
            ));
        }
        return rows;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Flat bean shape expected by {@code payroll_report.jrxml}'s fields.
     */
    public static class PayrollReportRow {
        private final String employeeId;
        private final String employeeName;
        private final String payrollMonth;
        private final BigDecimal grossPay;
        private final BigDecimal totalDeductions;
        private final BigDecimal netPay;

        public PayrollReportRow(String employeeId, String employeeName, String payrollMonth,
                                 BigDecimal grossPay, BigDecimal totalDeductions, BigDecimal netPay) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.payrollMonth = payrollMonth;
            this.grossPay = grossPay;
            this.totalDeductions = totalDeductions;
            this.netPay = netPay;
        }

        public String getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getPayrollMonth() { return payrollMonth; }
        public BigDecimal getGrossPay() { return grossPay; }
        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public BigDecimal getNetPay() { return netPay; }
    }
}

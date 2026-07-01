/**
 *
 * @author Khaesey Angel Tablante
 */

package service;

import model.Employee;
import model.Payslip;
import model.PayrollSummary;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates payroll/payslip reports using JasperReports, respecting the
 * same RBAC rules enforced by {@link PayslipService}.
 *
 * <p>Three report shapes are supported:</p>
 * <ul>
 *     <li><b>Own payslip(s)</b> - the MotorPH-branded payslip layout
 *     (Earnings / Benefits / Deductions / Summary, one page per payslip),
 *     for the requesting employee's own records.</li>
 *     <li><b>Aggregated report</b> - a flat table of every employee's
 *     payslips, for users with broader payroll-viewing permission.</li>
 *     <li><b>Department summary</b> - totals grouped by department (role),
 *     for users with broader payroll-viewing permission.</li>
 * </ul>
 */
public class PayrollReportService {

    private static final String OWN_TEMPLATE_PATH = "/reports/payroll_report.jrxml";
    private static final String PAYSLIP_TEMPLATE_PATH = "/reports/employee_payslip.jrxml";
    private static final String SUMMARY_TEMPLATE_PATH = "/reports/payroll_summary_report.jrxml";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MM/yyyy");

    private final PayslipService payslipService;

    public PayrollReportService(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    /**
     * Whether the given user has the option to generate reports covering
     * other employees (aggregated report, department summary), in addition
     * to their own payslip(s).
     *
     * @param currentUser the logged-in employee
     * @return {@code true} if they have broader payroll-viewing permission
     */
    public boolean canGenerateBroaderReports(Employee currentUser) {
        return payslipService.canViewBroaderPayroll(currentUser);
    }

    /**
     * Generates and displays the requesting user's own payslip(s) using the
     * MotorPH-branded payslip layout, most recent month first. Available to
     * everyone, including HR, when they want their personal payslip rather
     * than a company-wide report.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateOwnPayslipReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<Payslip> payslips = payslipService.getPayslipsForEmployee(currentUser.getId());
        if (payslips.isEmpty()) {
            throw new IllegalArgumentException("No payslips have been generated for you yet.");
        }

        try {
            net.sf.jasperreports.engine.design.JasperDesign design = loadDesign(PAYSLIP_TEMPLATE_PATH);
            net.sf.jasperreports.engine.JasperReport report = JasperCompileManager.compileReport(design);

            // Most recent payslip first.
            payslips.sort((a, b) -> {
                YearMonth ma = a.getPayrollMonth();
                YearMonth mb = b.getPayrollMonth();
                if (ma == null) return 1;
                if (mb == null) return -1;
                return mb.compareTo(ma);
            });

            Payslip firstPayslip = payslips.get(0);
            Map<String, Object> firstParams = buildPayslipParameters(firstPayslip);
            JasperPrint masterPrint = JasperFillManager.fillReport(report, firstParams, new JREmptyDataSource());

            for (int i = 1; i < payslips.size(); i++) {
                Payslip currentPayslip = payslips.get(i);
                Map<String, Object> currentParams = buildPayslipParameters(currentPayslip);
                
                JasperPrint subPrint = JasperFillManager.fillReport(report, currentParams, new JREmptyDataSource());
                
                for (net.sf.jasperreports.engine.JRPrintPage page : subPrint.getPages()) {
                    masterPrint.addPage(page);
                }
            }
            
            String title = "MotorPH Payslip History - " + safe(currentUser.getFullName());
            JasperViewer viewer = new JasperViewer(masterPrint, false);
            viewer.setTitle(title);
            viewer.setVisible(true);
            
        } catch (JRException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate payslip.", e);
        }
    }

    /**
     * Generates and displays the company-wide aggregated payroll report
     * (flat table, every employee's payslips). Only available to users
     * with broader payroll-viewing permission; throws otherwise.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateAggregatedReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }
        if (!canGenerateBroaderReports(currentUser)) {
            throw new IllegalArgumentException("You do not have permission to view the aggregated payroll report.");
        }

        List<Payslip> payslips = payslipService.getVisiblePayslips(currentUser);
        String title = "MotorPH Payroll Report - All Employees";
        showAggregatedReport(payslips, title, currentUser);
    }

    /**
     * Generates and displays the department-grouped payroll summary. Only
     * available to users with broader payroll-viewing permission; throws
     * otherwise.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateDepartmentSummaryReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<PayrollSummary> summary = payslipService.getPayrollSummaryByDepartment(currentUser);
        String title = "MotorPH Payroll Summary - By Department";

        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(summary);
            render(SUMMARY_TEMPLATE_PATH, dataSource, title, currentUser);
        } catch (JRException e) {
            throw new RuntimeException("Failed to generate department payroll summary.", e);
        }
    }

    /**
     * Generates and displays a payroll report scoped to one specific
     * employee (flat table), honoring the requesting user's RBAC
     * permissions (delegated to {@link PayslipService#getVisiblePayslipsForEmployee}).
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
        showAggregatedReport(payslips, title, currentUser);
    }

    private void showAggregatedReport(List<Payslip> payslips, String title, Employee currentUser) {
        try {
            List<PayrollReportRow> rows = toReportRows(payslips);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            render(OWN_TEMPLATE_PATH, dataSource, title, currentUser);
        } catch (JRException e) {
            throw new RuntimeException("Failed to generate payroll report.", e);
        }
    }

    private void render(String templatePath, JRBeanCollectionDataSource dataSource, String title, Employee currentUser) throws JRException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", title);
        parameters.put("generatedBy", safe(currentUser.getFullName()));
        parameters.put("generatedOn", LocalDateTime.now().format(TIMESTAMP_FORMAT));

        net.sf.jasperreports.engine.design.JasperDesign design = loadDesign(templatePath);
        net.sf.jasperreports.engine.JasperReport report = JasperCompileManager.compileReport(design);
        JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setTitle(title);
        viewer.setVisible(true);
    }

    private Map<String, Object> buildPayslipParameters(Payslip payslip) {
        Map<String, Object> parameters = new HashMap<>();

        YearMonth month = payslip.getPayrollMonth();
        java.time.LocalDate periodStart = month != null ? month.atDay(1) : null;
        java.time.LocalDate periodEnd = month != null ? month.atEndOfMonth() : null;
        String monthLabel = month != null ? month.format(MONTH_FORMAT) : "";

        parameters.put("payslipNo", (payslip.getPayslipId() != null ? payslip.getPayslipId() : "-")
                + "-" + monthLabel);
        parameters.put("periodStartDate", periodStart != null ? periodStart.toString() : "");
        parameters.put("periodEndDate", periodEnd != null ? periodEnd.toString() : "");
        parameters.put("employeeId", safe(payslip.getEmployeeId()));
        parameters.put("employeeName", safe(payslip.getEmployeeName()));
        parameters.put("positionDepartment", safe(payslip.getPosition()));

        parameters.put("monthlyRate", nz(payslip.getBasicPay()));
        parameters.put("dailyRate", nz(payslip.getHourlyRate()).multiply(BigDecimal.valueOf(8)));
        parameters.put("daysWorked", nz(payslip.getHoursWorked()).divide(BigDecimal.valueOf(8), 2, java.math.RoundingMode.HALF_UP));
        parameters.put("overtime", BigDecimal.ZERO);
        parameters.put("grossIncome", nz(payslip.getGrossPay()));

        parameters.put("riceSubsidy", nz(payslip.getRiceSubsidy()));
        parameters.put("phoneAllowance", nz(payslip.getPhoneAllowance()));
        parameters.put("clothingAllowance", nz(payslip.getClothingAllowance()));
        parameters.put("totalBenefits", nz(payslip.getTotalAllowance()));

        parameters.put("sss", nz(payslip.getSss()));
        parameters.put("philHealth", nz(payslip.getPhilHealth()));
        parameters.put("pagIbig", nz(payslip.getPagIbig()));
        parameters.put("withholdingTax", nz(payslip.getTax()));
        parameters.put("totalDeductions", nz(payslip.getTotalDeductions()));

        parameters.put("netPay", nz(payslip.getNetPay()));

        return parameters;
    }

    private net.sf.jasperreports.engine.design.JasperDesign loadDesign(String templatePath) throws JRException {
        try (InputStream in = getClass().getResourceAsStream(templatePath)) {
            if (in == null) {
                throw new JRException("Report template not found at " + templatePath
                        + ". Make sure it is on the classpath under src/reports/.");
            }
            return net.sf.jasperreports.engine.xml.JRXmlLoader.load(in);
        } catch (java.io.IOException e) {
            throw new JRException("Failed to read report template: " + templatePath, e);
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

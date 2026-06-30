/**
 *
 * @author Khaesey Angel Tablante
 */

package service;

import model.AttendanceSummary;
import model.Employee;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates attendance reports using JasperReports, respecting the same
 * RBAC rules enforced by {@link AttendanceService}.
 *
 * <p>Two report shapes are supported:</p>
 * <ul>
 *     <li><b>Own report</b> (raw attendance rows for one employee) - used by
 *     regular employees viewing their own history, and optionally by HR
 *     when they want to see their personal attendance instead of the
 *     company-wide summary.</li>
 *     <li><b>Aggregated summary report</b> (one KPI row per employee: days
 *     present, late count, total hours, average time-in) - used by HR/users
 *     with broader attendance-viewing permission instead of dumping every
 *     raw row.</li>
 * </ul>
 * Note: Bali the late count or 'late' KPI, 
 * I did presume that an official standard time (eg. 8:00 AM)
 * since i did read files and rubrics but not seen any standard for late treshold, 
 * flag anything after that as late.
 * inform me Khaesey Tablante for modification or you may modify it in 
 * LATE_TRESHOLD = LocalTime.of(8, 0); at the {@Link Service/AttendanceService.java}
 */
public class AttendanceReportService {

    private static final String OWN_TEMPLATE_PATH = "/reports/attendance_report.jrxml";
    private static final String SUMMARY_TEMPLATE_PATH = "/reports/attendance_summary_report.jrxml";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private final AttendanceService attendanceService;

    public AttendanceReportService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Whether the given user has the option to generate the aggregated,
     * company-wide summary report (in addition to their own personal one).
     *
     * @param currentUser the logged-in employee
     * @return {@code true} if they can view the broader attendance summary
     */
    public boolean canGenerateAggregatedReport(Employee currentUser) {
        return attendanceService.canViewBroaderAttendance(currentUser);
    }

    /**
     * Generates and displays the requesting user's own attendance report
     * (raw log-in/log-out rows, not summarized). Available to everyone.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateOwnReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<model.AttendanceRecord> records = attendanceService.getAttendanceByEmployee(currentUser.getId());
        String title = "MotorPH Attendance Report - " + currentUser.getFullName();
        showRawReport(records, title, currentUser);
    }

    /**
     * Generates and displays the company-wide attendance KPI summary
     * (one row per employee). Only available to users with broader
     * attendance-viewing permission; throws otherwise.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateAggregatedReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }
        if (!canGenerateAggregatedReport(currentUser)) {
            throw new IllegalArgumentException("You do not have permission to view the aggregated attendance report.");
        }

        List<AttendanceSummary> summary = attendanceService.getAttendanceSummary(currentUser);
        String title = "MotorPH Attendance Summary - All Employees";
        showSummaryReport(summary, title, currentUser);
    }

    /**
     * Convenience method that picks the most appropriate report for the
     * user automatically: aggregated summary if they have broader viewing
     * permission, otherwise their own report. Kept for callers that don't
     * need to offer an explicit choice.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateAndShowReport(Employee currentUser) {
        if (canGenerateAggregatedReport(currentUser)) {
            generateAggregatedReport(currentUser);
        } else {
            generateOwnReport(currentUser);
        }
    }

    /**
     * Generates and displays an attendance report scoped to one specific
     * employee (raw rows), honoring the requesting user's RBAC permissions.
     *
     * @param currentUser the logged-in employee iS requesting the report
     * @param targetEmployeeId the employee whose attendance is being reported to
     */
    public void generateAndShowReportForEmployee(Employee currentUser, String targetEmployeeId) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<model.AttendanceRecord> records =
                attendanceService.getVisibleAttendanceByEmployee(currentUser, targetEmployeeId);

        String title = "MotorPH Attendance Report - " + targetEmployeeId;
        showRawReport(records, title, currentUser);
    }

    private void showRawReport(List<model.AttendanceRecord> records, String title, Employee currentUser) {
        try {
            List<AttendanceReportRow> rows = toReportRows(records);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            render(OWN_TEMPLATE_PATH, dataSource, title, currentUser);
        } catch (JRException e) {
            throw new RuntimeException("Failed to generate attendance report.", e);
        }
    }

    private void showSummaryReport(List<AttendanceSummary> summary, String title, Employee currentUser) {
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(summary);
            render(SUMMARY_TEMPLATE_PATH, dataSource, title, currentUser);
        } catch (JRException e) {
            throw new RuntimeException("Failed to generate attendance summary report.", e);
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

    private List<AttendanceReportRow> toReportRows(List<model.AttendanceRecord> records) {
        List<AttendanceReportRow> rows = new java.util.ArrayList<>();
        for (model.AttendanceRecord record : records) {
            String employeeName = (safe(record.getFirstName()) + " " + safe(record.getLastName())).trim();
            rows.add(new AttendanceReportRow(
                    record.getEmployeeId(),
                    employeeName.isEmpty() ? record.getEmployeeId() : employeeName,
                    record.getDate(),
                    record.getLogIn(),
                    record.getLogOut()
            ));
        }
        return rows;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Flat bean shape expected by {@code attendance_report.jrxml}'s fields.
     */
    public static class AttendanceReportRow {
        private final String employeeId;
        private final String employeeName;
        private final String date;
        private final String logIn;
        private final String logOut;

        public AttendanceReportRow(String employeeId, String employeeName, String date, String logIn, String logOut) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.date = date;
            this.logIn = logIn;
            this.logOut = logOut;
        }

        public String getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getDate() { return date; }
        public String getLogIn() { return logIn; }
        public String getLogOut() { return logOut; }
    }
}

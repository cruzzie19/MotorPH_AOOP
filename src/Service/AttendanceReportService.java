/**
 *
 * @author Khaesey Angel Tablante
 */

package service;

import RBAC.Permission;
import model.AttendanceRecord;
import model.Employee;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates attendance reports using JasperReports, respecting the same
 * RBAC rules already enforced by {@link AttendanceService}: employees
 * without {@link Permission#VIEW_ATTENDANCE} can only generate a report of
 * their own attendance history, while employees with that permission can
 * generate a summarized report covering every employee.
 */
public class AttendanceReportService {

    private static final String TEMPLATE_PATH = "/reports/attendance_report.jrxml";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private final AttendanceService attendanceService;

    public AttendanceReportService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Generates and displays the attendance report visible to the given
     * user. Users with broader viewing permission get every employee's
     * records, grouped and summarized by employee; everyone else gets a
     * report of only their own records.
     *
     * @param currentUser the logged-in employee requesting the report
     */
    public void generateAndShowReport(Employee currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        boolean broaderView = attendanceService.canViewBroaderAttendance(currentUser);

        List<AttendanceRecord> records = broaderView
                ? attendanceService.getVisibleAttendance(currentUser)
                : attendanceService.getAttendanceByEmployee(currentUser.getId());

        String title = broaderView
                ? "MotorPH Attendance Report - All Employees"
                : "MotorPH Attendance Report - " + currentUser.getFullName();

        showReport(records, title, currentUser);
    }

    /**
     * Generates and displays an attendance report scoped to one specific
     * employee, honoring the requesting user's RBAC permissions (delegated
     * to {@link AttendanceService#getVisibleAttendanceByEmployee}).
     *
     * @param currentUser the logged-in employee requesting the report
     * @param targetEmployeeId the employee whose attendance is being reported on
     */
    public void generateAndShowReportForEmployee(Employee currentUser, String targetEmployeeId) {
        if (currentUser == null) {
            throw new IllegalArgumentException("No logged-in employee found.");
        }

        List<AttendanceRecord> records =
                attendanceService.getVisibleAttendanceByEmployee(currentUser, targetEmployeeId);

        String title = "MotorPH Attendance Report - " + targetEmployeeId;

        showReport(records, title, currentUser);
    }

    private void showReport(List<AttendanceRecord> records, String title, Employee currentUser) {
        try {
            List<AttendanceReportRow> rows = toReportRows(records);

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
            throw new RuntimeException("Failed to generate attendance report.", e);
        }
    }

    private net.sf.jasperreports.engine.design.JasperDesign loadDesign() throws JRException {
        try (InputStream in = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (in == null) {
                throw new JRException("Attendance report template not found at " + TEMPLATE_PATH
                        + ". Make sure reports/attendance_report.jrxml is on the classpath.");
            }
            return net.sf.jasperreports.engine.xml.JRXmlLoader.load(in);
        } catch (java.io.IOException e) {
            throw new JRException("Failed to read attendance report template.", e);
        }
    }

    private List<AttendanceReportRow> toReportRows(List<AttendanceRecord> records) {
        List<AttendanceReportRow> rows = new ArrayList<>();
        for (AttendanceRecord record : records) {
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
     * Kept private/static since it exists only to feed JasperReports.
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

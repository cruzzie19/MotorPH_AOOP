/**
 *
 * @author Leianna Cruz
 * @author Khaesey Angel Tablante
 */

package service;

import RBAC.Permission;
import model.AttendanceRecord;
import model.AttendanceSummary;
import model.Employee;
import repository.AttendanceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttendanceService {

    private final AttendanceRepository repository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 0);

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    public List<AttendanceRecord> getVisibleAttendance(Employee currentUser) {
        validateEmployee(currentUser);

        if (canViewBroaderAttendance(currentUser)) {
            List<AttendanceRecord> records = repository.findAll();
            records.sort(Comparator.comparing(this::safeDateString).reversed());
            return records;
        }

        return getAttendanceByEmployee(currentUser.getId());
    }

    public List<AttendanceRecord> getVisibleAttendanceByEmployee(Employee currentUser, String targetEmployeeId) {
        validateEmployee(currentUser);

        if (canViewBroaderAttendance(currentUser)) {
            List<AttendanceRecord> records = repository.findByEmployeeId(targetEmployeeId);
            records.sort(Comparator.comparing(this::safeDateString).reversed());
            return records;
        }

        if (!isOwnRecord(currentUser, targetEmployeeId)) {
            throw new IllegalArgumentException("You can only view your own attendance records.");
        }

        return getAttendanceByEmployee(targetEmployeeId);
    }

    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) {
        List<AttendanceRecord> records = repository.findByEmployeeId(employeeId);
        records.sort(Comparator.comparing(this::safeDateString).reversed());
        return records;
    }

    /**
     * Builds a per-employee KPI summary (days present, late count, total
     * hours, average time-in) from the attendance records visible to the
     * given user. Employees with broader viewing permission (e.g. HR) get
     * one summary row per employee across the whole company; everyone else
     * gets a single summary row for just themselves.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee requesting the summary
     * @return one {@link AttendanceSummary} per employee, sorted by employee ID
     */
    public List<AttendanceSummary> getAttendanceSummary(Employee currentUser) {
        List<AttendanceRecord> records = getVisibleAttendance(currentUser);
        return summarize(records);
    }

    /**
     * Builds a per-employee KPI summary scoped to a single target employee,
     * honoring the same RBAC rules as {@link #getVisibleAttendanceByEmployee}.
     *
     * @author Khaesey Angel Tablante
     * @param currentUser the logged-in employee requesting the summary
     * @param targetEmployeeId the employee whose attendance is being summarized
     * @return a single-element list containing that employee's summary, or
     *         empty if they have no attendance records
     */
    public List<AttendanceSummary> getAttendanceSummaryForEmployee(Employee currentUser, String targetEmployeeId) {
        List<AttendanceRecord> records = getVisibleAttendanceByEmployee(currentUser, targetEmployeeId);
        return summarize(records);
    }

    private List<AttendanceSummary> summarize(List<AttendanceRecord> records) {
        Map<String, AttendanceSummary> summaries = new LinkedHashMap<>();
        Map<String, List<LocalTime>> timeInsByEmployee = new LinkedHashMap<>();

        for (AttendanceRecord record : records) {
            String employeeId = record.getEmployeeId();
            AttendanceSummary summary = summaries.computeIfAbsent(employeeId, id -> {
                String name = (safe(record.getFirstName()) + " " + safe(record.getLastName())).trim();
                return new AttendanceSummary(id, name.isEmpty() ? id : name);
            });

            if (isBlank(record.getLogIn())) {
                continue;
            }

            summary.setDaysPresent(summary.getDaysPresent() + 1);

            LocalTime timeIn = parseTime(record.getLogIn());
            if (timeIn != null) {
                if (timeIn.isAfter(LATE_THRESHOLD)) {
                    summary.setLateCount(summary.getLateCount() + 1);
                }
                timeInsByEmployee.computeIfAbsent(employeeId, id -> new ArrayList<>()).add(timeIn);
            }

            BigDecimal hoursForDay = hoursWorked(record.getLogIn(), record.getLogOut());
            if (hoursForDay != null) {
                summary.setTotalHours(summary.getTotalHours().add(hoursForDay));
            }
        }

        for (AttendanceSummary summary : summaries.values()) {
            List<LocalTime> timeIns = timeInsByEmployee.get(summary.getEmployeeId());
            summary.setAverageTimeIn(averageTime(timeIns));
        }

        List<AttendanceSummary> result = new ArrayList<>(summaries.values());
        result.sort(Comparator.comparing(AttendanceSummary::getEmployeeId));
        return result;
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal hoursWorked(String logIn, String logOut) {
        LocalTime in = parseTime(logIn);
        LocalTime out = parseTime(logOut);
        if (in == null || out == null || !out.isAfter(in)) {
            return null;
        }
        long minutes = Duration.between(in, out).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String averageTime(List<LocalTime> times) {
        if (times == null || times.isEmpty()) {
            return "";
        }
        long totalMinutes = 0;
        for (LocalTime t : times) {
            totalMinutes += t.getHour() * 60L + t.getMinute();
        }
        long avgMinutes = totalMinutes / times.size();
        LocalTime avg = LocalTime.of((int) (avgMinutes / 60), (int) (avgMinutes % 60));
        return avg.format(TIME_FORMAT);
    }

    public void timeIn(Employee employee) {
        validateEmployee(employee);

        String employeeId = employee.getId();
        String today = LocalDate.now().format(DATE_FORMAT);

        AttendanceRecord existing = repository.findByEmployeeIdAndDate(employeeId, today);
        if (existing != null && !isBlank(existing.getLogIn())) {
            throw new IllegalArgumentException("You have already timed in today.");
        }

        String now = LocalTime.now().format(TIME_FORMAT);

        if (existing == null) {
            AttendanceRecord record = new AttendanceRecord(
                    employeeId,
                    safe(employee.getLastName()),
                    safe(employee.getFirstName()),
                    today,
                    now,
                    ""
            );
            repository.add(record);
        } else {
            existing.setLogIn(now);
            repository.update(existing);
        }
    }

    public void timeOut(Employee employee) {
        validateEmployee(employee);

        String employeeId = employee.getId();
        String today = LocalDate.now().format(DATE_FORMAT);

        AttendanceRecord existing = repository.findByEmployeeIdAndDate(employeeId, today);
        if (existing == null || isBlank(existing.getLogIn())) {
            throw new IllegalArgumentException("You must time in first before timing out.");
        }

        if (!isBlank(existing.getLogOut())) {
            throw new IllegalArgumentException("You have already timed out today.");
        }

        String now = LocalTime.now().format(TIME_FORMAT);
        existing.setLogOut(now);
        repository.update(existing);
    }

    public void updateAttendance(Employee currentUser, AttendanceRecord updatedRecord) {
        validateEmployee(currentUser);

        if (updatedRecord == null) {
            throw new IllegalArgumentException("Attendance record cannot be null.");
        }
        if (isBlank(updatedRecord.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (isBlank(updatedRecord.getDate())) {
            throw new IllegalArgumentException("Date is required.");
        }

        if (!canUpdateAnyAttendance(currentUser) && !isOwnRecord(currentUser, updatedRecord.getEmployeeId())) {
            throw new IllegalArgumentException("You can only update your own attendance records.");
        }

        repository.update(updatedRecord);
    }

    public void deleteAttendance(Employee currentUser, String employeeId, String date) {
        validateEmployee(currentUser);

        if (isBlank(employeeId) || isBlank(date)) {
            throw new IllegalArgumentException("Employee ID and date are required.");
        }

        if (!canDeleteAnyAttendance(currentUser)) {
            throw new IllegalArgumentException("Only HR can delete attendance records.");
        }

        repository.delete(employeeId, date);
    }

    public boolean canViewBroaderAttendance(Employee currentUser) {
        return AuthorizationService.hasPermission(currentUser, Permission.VIEW_ATTENDANCE);
    }

    public boolean canUpdateAnyAttendance(Employee currentUser) {
        return AuthorizationService.hasPermission(currentUser, Permission.EDIT_ATTENDANCE);
    }

    public boolean canDeleteAnyAttendance(Employee currentUser) {
        return AuthorizationService.hasPermission(currentUser, Permission.DELETE_ATTENDANCE)
                || AuthorizationService.hasPermission(currentUser, Permission.EDIT_ATTENDANCE);
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
        if (isBlank(employee.getId())) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
    }

    private String safeDateString(AttendanceRecord record) {
        try {
            return LocalDate.parse(record.getDate(), DATE_FORMAT).toString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
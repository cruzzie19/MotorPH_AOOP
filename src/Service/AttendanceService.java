/**
 *
 * @author Leianna Cruz
 */

package service;

import RBAC.Permission;
import model.AttendanceRecord;
import model.Employee;
import repository.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class AttendanceService {

    private final AttendanceRepository repository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

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
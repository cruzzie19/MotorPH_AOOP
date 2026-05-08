/**
 *
 * @author Leianna Cruz
 */

package service;

import model.Leave;
import repository.LeaveRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LeaveService {

    private static final int MAX_LEAVE_DAYS_PER_YEAR = 15;

    private final LeaveRepository repo;

    public LeaveService(LeaveRepository repo) {
        this.repo = repo;
    }

    public List<Leave> getAll() {
        return repo.findAll();
    }

    public List<Leave> getByEmployeeId(String employeeId) {
        List<Leave> all = repo.findAll();
        List<Leave> filtered = new ArrayList<>();
        for (Leave l : all) {
            if (l.getEmployeeId().equalsIgnoreCase(employeeId)) {
                filtered.add(l);
            }
        }
        return filtered;
    }

    public void requestLeave(Leave leave) {
        validateLeave(leave);
        validateAnnualLeaveLimit(leave, false);
        repo.add(leave);
    }

    public void updateLeave(Leave leave) {
        validateLeave(leave);
        validateAnnualLeaveLimit(leave, true);
        repo.update(leave);
    }

    public void updateOwnPendingLeave(Leave leave, String employeeId) {
        if (!leave.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("You can only update your own leave.");
        }

        validateLeave(leave);
        validateAnnualLeaveLimit(leave, true);

        repo.update(leave);
    }

    public void deleteOwnPendingLeave(int leaveId, String employeeId) {
        repo.delete(leaveId);
    }

    private void validateLeave(Leave leave) {
        LeaveRequestValidator.validate(leave);

        if (leave.getStatus() == null || leave.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
    }

    private void validateAnnualLeaveLimit(Leave targetLeave, boolean excludeCurrentRecord) {
        if (targetLeave == null) {
            return;
        }

        LocalDate requestStart = LocalDate.parse(targetLeave.getStartDate());
        LocalDate requestEnd = LocalDate.parse(targetLeave.getEndDate());

        int requestYear = requestStart.getYear();
        if (requestEnd.getYear() != requestYear) {
            throw new IllegalArgumentException("Leave request must stay within one calendar year.");
        }

        long requestedDays = countInclusiveDays(requestStart, requestEnd);
        long usedDays = 0;

        List<Leave> existingLeaves = repo.findByEmployeeId(targetLeave.getEmployeeId());

        for (Leave existing : existingLeaves) {
            if (existing == null) {
                continue;
            }

            if (excludeCurrentRecord && existing.getLeaveId() == targetLeave.getLeaveId()) {
                continue;
            }

            String status = existing.getStatus() == null ? "" : existing.getStatus().trim();
            if (!status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Approved")) {
                continue;
            }

            if (existing.getStartDate() == null || existing.getEndDate() == null) {
                continue;
            }

            LocalDate existingStart;
            LocalDate existingEnd;

            try {
                existingStart = LocalDate.parse(existing.getStartDate().trim());
                existingEnd = LocalDate.parse(existing.getEndDate().trim());
            } catch (Exception ex) {
                continue;
            }

            if (existingStart.getYear() != requestYear || existingEnd.getYear() != requestYear) {
                continue;
            }

            usedDays += countInclusiveDays(existingStart, existingEnd);
        }

        long totalDays = usedDays + requestedDays;

        if (totalDays > MAX_LEAVE_DAYS_PER_YEAR) {
            long remainingDays = MAX_LEAVE_DAYS_PER_YEAR - usedDays;

            if (remainingDays < 0) {
                remainingDays = 0;
            }

            throw new IllegalArgumentException(
                    "Leave request exceeds the 15-day annual limit. " +
                    "Requested: " + requestedDays + " day(s). " +
                    "Remaining balance: " + remainingDays + " day(s)."
            );
        }
    }

    private long countInclusiveDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
/**
 *
 * @author Leianna Cruz
 */

package model;

public class Leave implements LeaveRequest {

    private int leaveId;
    private String employeeId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String notes;
    private String status;

    public Leave() {
    }

    public Leave(int leaveId, String employeeId, String leaveType,
                 String startDate, String endDate, String notes, String status) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.status = status;
    }

    @Override
    public int getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(int leaveId) {
        this.leaveId = leaveId;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId == null ? "" : employeeId.trim();
    }

    @Override
    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType == null ? "" : leaveType.trim();
    }

    @Override
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate == null ? "" : startDate.trim();
    }

    @Override
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate == null ? "" : endDate.trim();
    }

    @Override
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? "" : status.trim();
    }
}
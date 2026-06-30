/**
 *
 * @author Khaesey Angel Tablante
 */

package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This is for aggregated attendance KPIs for a single employee over a reporting period.
 * Used by service.AttendanceReportService to build HR's summarized
 * attendance report 
 */
public class AttendanceSummary {

    private String employeeId;
    private String employeeName;
    private int daysPresent;
    private int lateCount;
    private BigDecimal totalHours = BigDecimal.ZERO;
    private String averageTimeIn = "";

    public AttendanceSummary() {
    }

    public AttendanceSummary(String employeeId, String employeeName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getDaysPresent() {
        return daysPresent;
    }

    public void setDaysPresent(int daysPresent) {
        this.daysPresent = daysPresent;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours == null ? BigDecimal.ZERO : totalHours.setScale(2, RoundingMode.HALF_UP);
    }

    public String getAverageTimeIn() {
        return averageTimeIn;
    }

    public void setAverageTimeIn(String averageTimeIn) {
        this.averageTimeIn = averageTimeIn == null ? "" : averageTimeIn;
    }
}

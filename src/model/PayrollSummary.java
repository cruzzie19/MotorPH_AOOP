/**
 *
 * @author Khaesey Angel Tablante
 */

package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aggregated payroll KPIs for a single department (modeled here as
 * {@link Employee#getRole()}) over a reporting period. Used to build the
 * department-filtered payroll summary report requested for Task 7.2.
 */
public class PayrollSummary {

    private String department;
    private int employeeCount;
    private int payslipCount;
    private BigDecimal totalGrossPay = BigDecimal.ZERO;
    private BigDecimal totalDeductions = BigDecimal.ZERO;
    private BigDecimal totalNetPay = BigDecimal.ZERO;

    public PayrollSummary() {
    }

    public PayrollSummary(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public int getPayslipCount() {
        return payslipCount;
    }

    public void setPayslipCount(int payslipCount) {
        this.payslipCount = payslipCount;
    }

    public BigDecimal getTotalGrossPay() {
        return totalGrossPay;
    }

    public void setTotalGrossPay(BigDecimal totalGrossPay) {
        this.totalGrossPay = scale(totalGrossPay);
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = scale(totalDeductions);
    }

    public BigDecimal getTotalNetPay() {
        return totalNetPay;
    }

    public void setTotalNetPay(BigDecimal totalNetPay) {
        this.totalNetPay = scale(totalNetPay);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}

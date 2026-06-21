/**
 *
 * @author Leianna Cruz
 */

package model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Plain data model representing one generated payslip for an employee for a
 * specific payroll month.
 *
 * <p>This class is intentionally "dumb" - it holds data only. Computation
 * lives in {@code service.PayrollComputationService} and persistence lives
 * in {@code repository.PayrollRepository} / {@code service.PayslipService}.
 * Keeping the model free of both keeps each class easy to test on its
 * own.</p>
 */
public class Payslip {

    /** Database primary key. Null until the payslip has been saved. */
    private Long payslipId;

    private String employeeId;
    private String employeeName;
    private String position;
    private String employeeType;

    private YearMonth payrollMonth;

    private BigDecimal hoursWorked;
    private BigDecimal hourlyRate;
    private BigDecimal basicPay;

    private BigDecimal riceSubsidy;
    private BigDecimal phoneAllowance;
    private BigDecimal clothingAllowance;
    private BigDecimal totalAllowance;

    private BigDecimal grossPay;

    private BigDecimal tax;
    private BigDecimal sss;
    private BigDecimal philHealth;
    private BigDecimal pagIbig;
    private BigDecimal totalDeductions;

    private BigDecimal netPay;

    public Payslip() {
    }

    public Payslip(
            String employeeId,
            String employeeName,
            String position,
            String employeeType,
            YearMonth payrollMonth,
            BigDecimal hoursWorked,
            BigDecimal hourlyRate,
            BigDecimal basicPay,
            BigDecimal riceSubsidy,
            BigDecimal phoneAllowance,
            BigDecimal clothingAllowance,
            BigDecimal totalAllowance,
            BigDecimal grossPay,
            BigDecimal tax,
            BigDecimal sss,
            BigDecimal philHealth,
            BigDecimal pagIbig,
            BigDecimal totalDeductions,
            BigDecimal netPay
    ) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.position = position;
        this.employeeType = employeeType;
        this.payrollMonth = payrollMonth;
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
        this.basicPay = basicPay;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.totalAllowance = totalAllowance;
        this.grossPay = grossPay;
        this.tax = tax;
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
    }

    public Long getPayslipId() {
        return payslipId;
    }

    public void setPayslipId(Long payslipId) {
        this.payslipId = payslipId;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public YearMonth getPayrollMonth() {
        return payrollMonth;
    }

    public void setPayrollMonth(YearMonth payrollMonth) {
        this.payrollMonth = payrollMonth;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getBasicPay() {
        return basicPay;
    }

    public void setBasicPay(BigDecimal basicPay) {
        this.basicPay = basicPay;
    }

    public BigDecimal getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(BigDecimal riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public BigDecimal getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(BigDecimal phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public BigDecimal getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(BigDecimal clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
    }

    public BigDecimal getTotalAllowance() {
        return totalAllowance;
    }

    public void setTotalAllowance(BigDecimal totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getSss() {
        return sss;
    }

    public void setSss(BigDecimal sss) {
        this.sss = sss;
    }

    public BigDecimal getPhilHealth() {
        return philHealth;
    }

    public void setPhilHealth(BigDecimal philHealth) {
        this.philHealth = philHealth;
    }

    public BigDecimal getPagIbig() {
        return pagIbig;
    }

    public void setPagIbig(BigDecimal pagIbig) {
        this.pagIbig = pagIbig;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payslip other)) return false;
        return Objects.equals(employeeId, other.employeeId)
                && Objects.equals(payrollMonth, other.payrollMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, payrollMonth);
    }

    @Override
    public String toString() {
        return "Payslip[employeeId=" + employeeId + ", month=" + payrollMonth + ", netPay=" + netPay + "]";
    }
}

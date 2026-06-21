/**
 *
 * @author Leianna Cruz
 */

package service;

import model.AttendanceRecord;
import model.Employee;
import model.Payslip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pure computation service for payroll.
 *
 * <p>Reworked: this class used to carry its own nested
 * {@code PayslipResult} class. Now that {@code model.Payslip} exists as a
 * persistable model, this service builds and returns {@code Payslip}
 * instances instead. This class still does <b>not</b> talk to any
 * repository - persistence is handled separately by
 * {@link PayslipService} / {@code repository.PayrollRepository}.</p>
 */
public class PayrollComputationService {

    private static final BigDecimal SSS_RATE = new BigDecimal("0.04");
    private static final BigDecimal PHILHEALTH_RATE = new BigDecimal("0.0275");
    private static final BigDecimal PAGIBIG_RATE = new BigDecimal("0.02");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private static final DateTimeFormatter ATTENDANCE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter ATTENDANCE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm");

    public Payslip computePayslipFromAttendance(
            Employee employee,
            List<AttendanceRecord> attendanceRecords,
            YearMonth targetMonth
    ) {
        validateEmployee(employee);

        BigDecimal hoursWorked = computeHoursWorked(attendanceRecords, targetMonth);

        // Business rule: blank payslip if there is no attendance for the selected month
        if (hoursWorked.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return buildPayslip(employee, hoursWorked, targetMonth);
    }

    public Payslip computePayslipFromHoursWorked(
            Employee employee,
            BigDecimal hoursWorked,
            YearMonth targetMonth
    ) {
        validateEmployee(employee);

        BigDecimal safeHoursWorked = nonNegative(hoursWorked).setScale(2, RoundingMode.HALF_UP);
        return buildPayslip(employee, safeHoursWorked, targetMonth);
    }

    private Payslip buildPayslip(
            Employee employee,
            BigDecimal hoursWorked,
            YearMonth targetMonth
    ) {
        BigDecimal hourlyRate = scale(employee.getHourlyRate());
        BigDecimal basicPay = scale(hoursWorked.multiply(hourlyRate));

        BigDecimal riceSubsidy = scale(employee.getRiceSubsidy());
        BigDecimal phoneAllowance = scale(employee.getPhoneAllowance());
        BigDecimal clothingAllowance = scale(employee.getClothingAllowance());

        BigDecimal totalAllowance = scale(
                riceSubsidy.add(phoneAllowance).add(clothingAllowance)
        );

        BigDecimal grossPay = scale(basicPay.add(totalAllowance));

        BigDecimal sss = computeSss(grossPay);
        BigDecimal philHealth = computePhilHealth(grossPay);
        BigDecimal pagIbig = computePagIbig(grossPay);

        BigDecimal taxableIncome = grossPay
                .subtract(sss)
                .subtract(philHealth)
                .subtract(pagIbig);

        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }

        taxableIncome = scale(taxableIncome);

        BigDecimal tax = computeTax(taxableIncome);

        BigDecimal totalDeductions = scale(
                sss.add(philHealth).add(pagIbig).add(tax)
        );

        BigDecimal netPay = scale(grossPay.subtract(totalDeductions));

        return new Payslip(
                employee.getId(),
                employee.getFullName(),
                safe(employee.getPosition()),
                safe(employee.getEmployeeType()),
                targetMonth,
                hoursWorked,
                hourlyRate,
                basicPay,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                totalAllowance,
                grossPay,
                tax,
                sss,
                philHealth,
                pagIbig,
                totalDeductions,
                netPay
        );
    }

    public BigDecimal computeSss(BigDecimal grossPay) {
        return scale(nonNegative(grossPay).multiply(SSS_RATE));
    }

    public BigDecimal computePhilHealth(BigDecimal grossPay) {
        return scale(nonNegative(grossPay).multiply(PHILHEALTH_RATE));
    }

    public BigDecimal computePagIbig(BigDecimal grossPay) {
        return scale(nonNegative(grossPay).multiply(PAGIBIG_RATE));
    }

    public BigDecimal computeTax(BigDecimal taxableIncome) {
        return scale(nonNegative(taxableIncome).multiply(TAX_RATE));
    }

    private BigDecimal computeHoursWorked(List<AttendanceRecord> records, YearMonth targetMonth) {
        if (records == null || targetMonth == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalHours = BigDecimal.ZERO;

        for (AttendanceRecord record : records) {
            if (record == null) {
                continue;
            }

            if (isBlank(record.getDate()) || isBlank(record.getLogIn()) || isBlank(record.getLogOut())) {
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(record.getDate().trim(), ATTENDANCE_DATE_FORMAT);
                if (!YearMonth.from(date).equals(targetMonth)) {
                    continue;
                }

                LocalTime timeIn = LocalTime.parse(record.getLogIn().trim(), ATTENDANCE_TIME_FORMAT);
                LocalTime timeOut = LocalTime.parse(record.getLogOut().trim(), ATTENDANCE_TIME_FORMAT);

                if (!timeOut.isAfter(timeIn)) {
                    continue;
                }

                long minutes = Duration.between(timeIn, timeOut).toMinutes();
                BigDecimal hours = BigDecimal.valueOf(minutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                totalHours = totalHours.add(hours);

            } catch (Exception ignored) {
                // skip malformed attendance rows
            }
        }

        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required.");
        }
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private BigDecimal scale(BigDecimal value) {
        return nonNegative(value).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a probationary employee.
 * Business rule: probationary employees do NOT receive allowances.
 * @author Leianna Cruz
 */
public class ProbationaryEmployee extends Employee {

    public ProbationaryEmployee(
            String id,
            String firstName,
            String lastName,
            LocalDate birthDate,
            BigDecimal basicSalary,
            BigDecimal riceSubsidy,
            BigDecimal phoneAllowance,
            BigDecimal clothingAllowance,
            BigDecimal grossSemiMonthlyRate,
            BigDecimal hourlyRate
    ) {
        super(
                id,
                firstName,
                lastName,
                birthDate,
                basicSalary,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                grossSemiMonthlyRate,
                hourlyRate
        );
        setStatus("Probationary");
    }

    @Override
    public boolean isEligibleForAllowance() {
        return false;
    }

    @Override
    public BigDecimal getRiceSubsidy() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getPhoneAllowance() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getClothingAllowance() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalAllowance() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getWeeklyAllowanceEquivalent() {
        return BigDecimal.ZERO;
    }

    /**
     * Probationary employees receive basic salary only.
     */
    @Override
    public BigDecimal calculateSalary() {
        return getBasicSalary();
    }

    @Override
    public String getEmployeeType() {
        return "Probationary";
    }

    @Override
    public String toString() {
        return "Probationary Employee - "
                + getFirstName() + " "
                + getLastName()
                + " (" + getPosition() + ")";
    }
}
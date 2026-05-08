package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a regular (permanent) employee.
 * @author Leianna Cruz
 */
public class RegularEmployee extends Employee {

    public RegularEmployee(
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
        setStatus("Regular");
    }

    @Override
    public boolean isEligibleForAllowance() {
        return true;
    }

    /**
     * Regular employees receive allowances.
     */
    @Override
    public BigDecimal calculateSalary() {
        return getBasicSalary()
                .add(getRiceSubsidy())
                .add(getPhoneAllowance())
                .add(getClothingAllowance());
    }

    @Override
    public String getEmployeeType() {
        return "Regular";
    }

    @Override
    public String toString() {
        return "Regular Employee - "
                + getFirstName() + " "
                + getLastName()
                + " (" + getPosition() + ")";
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Rhynne Gracelle
 */

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Factory class responsible for creating Employee objects.
 * Centralizes employee type creation logic.
 */
public class EmployeeFactory {

    private EmployeeFactory() {
        // Prevent instantiation
    }

    public static Employee createEmployee(
            String status,
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

        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
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
        }

        // Default employee type
        return new RegularEmployee(
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
    }
}

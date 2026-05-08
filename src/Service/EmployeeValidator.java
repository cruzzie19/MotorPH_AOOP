/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Utility validator for employee creation/update requests.
 * Enforces required fields, numeric boundaries, and business constraints
 * before an employee can be persisted.
 *
 * <p>Validation scope:</p>
 * <ul>
 *   <li>Required identity and profile fields (ID, name, contact, government IDs)</li>
 *   <li>Required employment metadata (status, position, supervisor, role)</li>
 *   <li>Required birth date</li>
 *   <li>Non-negative compensation values</li>
 *   <li>Basic salary upper bound of 500,000.00</li>
 * </ul>
 *
 * @author Rhynne Gracelle
 * @see service.EmployeeRequest
 */

package service;

import java.math.BigDecimal;

public final class EmployeeValidator {

    private static final BigDecimal MAX_BASIC_SALARY = new BigDecimal("500000");

    private EmployeeValidator() {
    }

    /**
     * Validates an employee request.
     *
     * @param request employee input payload to validate
     * @throws IllegalArgumentException if any required field is missing
     *                                  or numeric constraints are violated
     */
    public static void validate(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Employee request cannot be null.");
        }

        requireNotBlank(request.getId(), "Employee ID is required.");
        requireNotBlank(request.getFirstName(), "First name is required.");
        requireNotBlank(request.getLastName(), "Last name is required.");
        requireNotBlank(request.getAddress(), "Address is required.");
        requireNotBlank(request.getPhone(), "Phone number is required.");
        requireNotBlank(request.getSssNumber(), "SSS number is required.");
        requireNotBlank(request.getPhilHealthNumber(), "PhilHealth number is required.");
        requireNotBlank(request.getTinNumber(), "TIN number is required.");
        requireNotBlank(request.getPagIbigNumber(), "Pag-IBIG number is required.");
        requireNotBlank(request.getStatus(), "Status is required.");
        requireNotBlank(request.getPosition(), "Position is required.");
        requireNotBlank(request.getSupervisor(), "Supervisor is required.");
        requireNotBlank(request.getRoleName(), "Role is required.");

        if (request.getBirthDate() == null) {
            throw new IllegalArgumentException("Birth date is required.");
        }

        requireNonNegative(request.getBasicSalary(), "Basic salary");
        requireNonNegative(request.getRiceSubsidy(), "Rice subsidy");
        requireNonNegative(request.getPhoneAllowance(), "Phone allowance");
        requireNonNegative(request.getClothingAllowance(), "Clothing allowance");
        requireNonNegative(request.getGrossSemiMonthlyRate(), "Gross semi-monthly rate");
        requireNonNegative(request.getHourlyRate(), "Hourly rate");

        requireMaximum(request.getBasicSalary(), MAX_BASIC_SALARY,
                "Basic salary cannot exceed 500,000.00.");
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
    }

    private static void requireMaximum(BigDecimal value, BigDecimal maximum, String message) {
        if (value != null && value.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
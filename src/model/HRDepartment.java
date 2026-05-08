package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author trisha
 */
public class HRDepartment extends Employee {
    
    public HRDepartment(String id, String firstName, String lastName, LocalDate birthDate, BigDecimal basicSalary, BigDecimal riceSubsidy, BigDecimal phoneAllowance, BigDecimal clothingAllowance, BigDecimal grossSemiMonthlyRate, BigDecimal hourlyRate) {
        super(id, firstName, lastName, birthDate, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossSemiMonthlyRate, hourlyRate);
    }

    @Override
public String toString() {
    return "HR Department"+getPosition()+ getFirstName() + " " + getLastName();
}

    @Override
    public BigDecimal calculateSalary() {
         return getBasicSalary()
                .add(getRiceSubsidy())
                .add(getPhoneAllowance())
                .add(getClothingAllowance());
    }
 
    
    
    public boolean approveLeave(String employeeId, int days) {

        if (employeeId == null || employeeId.isEmpty()) {
            return false;
        }

        if (days <= 0) {
            return false;
        }

        System.out.println("Leave approved for Employee ID: " + employeeId +
                " for " + days + " days.");

        return true;
    }

 
    public boolean approveLeave(String employeeId, int days, String reason) {

        if (employeeId == null || employeeId.isEmpty()) {
            return false;
        }

        if (days <= 0 || reason == null || reason.isEmpty()) {
            return false;
        }

        System.out.println("Leave approved for Employee ID: " + employeeId +
                " for " + days + " days. Reason: " + reason);

        return true;
    }
}
    
    
    
   

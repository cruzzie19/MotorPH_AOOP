/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author trisha
 */
public class PayrollDepartment extends Employee {
    
    public PayrollDepartment(String id, String firstName, String lastName, LocalDate birthDate, BigDecimal basicSalary, BigDecimal riceSubsidy, BigDecimal phoneAllowance, BigDecimal clothingAllowance, BigDecimal grossSemiMonthlyRate, BigDecimal hourlyRate) {
        super(id, firstName, lastName, birthDate, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossSemiMonthlyRate, hourlyRate);
    }
        @Override
public String toString() {
    return "Sales Department "+getPosition()+ getFirstName() + " " + getLastName();
}

    @Override
    public BigDecimal calculateSalary() {
         return getBasicSalary()
                .add(getRiceSubsidy())
                .add(getPhoneAllowance())
                .add(getClothingAllowance());
    }

    
    
    

  
}



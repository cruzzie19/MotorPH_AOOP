/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Leianna Cruz
 */
package RBAC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RBACSetup {

    public static Map<String, Role> setupRoles() {
        Map<String, Role> roles = new HashMap<>();

        // EXECUTIVE ROLE
        roles.put("EXECUTIVE", new Role("EXECUTIVE", Set.of(
                Permission.VIEW_EMPLOYEE,
                Permission.VIEW_EMPLOYEE_LIST,
                Permission.VIEW_EMPLOYEE_BASIC_DETAILS,
                Permission.VIEW_EMPLOYEE_COMPENSATION,

                Permission.VIEW_PAYROLL,
                Permission.VIEW_PAYSLIP,
                Permission.VIEW_OWN_PAYSLIP,

                Permission.VIEW_ATTENDANCE,
                Permission.VIEW_OWN_ATTENDANCE,

                Permission.VIEW_LEAVE_REQUESTS,
                Permission.VIEW_LEAVE_HISTORY,
                Permission.VIEW_OWN_LEAVE_HISTORY,

                Permission.VIEW_OWN_EMPLOYEE_PROFILE
        )));

        // HR ROLE
        roles.put("HR", new Role("HR", Set.of(
                Permission.VIEW_EMPLOYEE,
                Permission.VIEW_EMPLOYEE_LIST,
                Permission.VIEW_EMPLOYEE_BASIC_DETAILS,
                Permission.VIEW_EMPLOYEE_PERSONAL_DETAILS,
                Permission.VIEW_EMPLOYEE_GOVERNMENT_IDS,
                Permission.VIEW_EMPLOYEE_COMPENSATION,
                Permission.VIEW_OWN_EMPLOYEE_PROFILE,

                Permission.ADD_EMPLOYEE,
                Permission.EDIT_EMPLOYEE,
                Permission.DELETE_EMPLOYEE,

                Permission.SUBMIT_LEAVE,
                Permission.VIEW_LEAVE_REQUESTS,
                Permission.VIEW_LEAVE_HISTORY,
                Permission.VIEW_OWN_LEAVE_HISTORY,
                Permission.APPROVE_LEAVE,
                Permission.REJECT_LEAVE,

                Permission.VIEW_ATTENDANCE,
                Permission.VIEW_OWN_ATTENDANCE,
                Permission.EDIT_ATTENDANCE,

                Permission.VIEW_PAYROLL,
                Permission.VIEW_PAYSLIP,
                Permission.VIEW_OWN_PAYSLIP
        )));

        // PAYROLL ROLE
        roles.put("PAYROLL", new Role("PAYROLL", Set.of(
                Permission.VIEW_EMPLOYEE,
                Permission.VIEW_EMPLOYEE_LIST,
                Permission.VIEW_EMPLOYEE_BASIC_DETAILS,
                Permission.VIEW_EMPLOYEE_GOVERNMENT_IDS,
                Permission.VIEW_EMPLOYEE_COMPENSATION,
                Permission.VIEW_OWN_EMPLOYEE_PROFILE,

                Permission.VIEW_PAYROLL,
                Permission.PROCESS_PAYROLL,
                Permission.VIEW_OWN_PAYROLL,

                Permission.GENERATE_PAYSLIP,
                Permission.VIEW_PAYSLIP,
                Permission.VIEW_OWN_PAYSLIP,

                Permission.VIEW_OWN_ATTENDANCE,

                Permission.SUBMIT_LEAVE,
                Permission.VIEW_OWN_LEAVE_HISTORY
        )));

        // ACCOUNTING ROLE
        roles.put("ACCOUNTING", new Role("ACCOUNTING", Set.of(
                Permission.VIEW_EMPLOYEE,
                Permission.VIEW_EMPLOYEE_LIST,
                Permission.VIEW_EMPLOYEE_BASIC_DETAILS,
                Permission.VIEW_EMPLOYEE_COMPENSATION,
                Permission.VIEW_OWN_EMPLOYEE_PROFILE,

                Permission.VIEW_PAYROLL,
                Permission.VIEW_PAYSLIP,
                Permission.VIEW_OWN_PAYSLIP,

                Permission.VIEW_OWN_ATTENDANCE,

                Permission.SUBMIT_LEAVE,
                Permission.VIEW_OWN_LEAVE_HISTORY
        )));

        // IT ROLE
        roles.put("IT", new Role("IT", Set.of(
                Permission.VIEW_EMPLOYEE,
                Permission.VIEW_EMPLOYEE_LIST,
                Permission.VIEW_EMPLOYEE_BASIC_DETAILS,
                Permission.VIEW_OWN_EMPLOYEE_PROFILE,

                Permission.ACCESS_SYSTEM_TOOLS,
                Permission.RESET_PASSWORD,
                Permission.MANAGE_USERS,

                Permission.VIEW_OWN_ATTENDANCE,
                Permission.SUBMIT_LEAVE,
                Permission.VIEW_OWN_LEAVE_HISTORY,
                Permission.VIEW_OWN_PAYSLIP
        )));

        // SALES ROLE
        roles.put("SALES", new Role("SALES", Set.of(
                Permission.VIEW_OWN_EMPLOYEE_PROFILE,
                Permission.VIEW_OWN_PAYSLIP,
                Permission.VIEW_OWN_ATTENDANCE,
                Permission.SUBMIT_LEAVE,
                Permission.VIEW_OWN_LEAVE_HISTORY,
                Permission.TIME_IN,
                Permission.TIME_OUT
        )));

        return roles;
    }
}
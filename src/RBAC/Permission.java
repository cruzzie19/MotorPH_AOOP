/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
/**
 *
 * @author Leianna Cruz
 */
package RBAC;


public enum Permission {

    // EMPLOYEE ACCESS - GENERAL
    VIEW_EMPLOYEE,                 // Backward-compatible general employee view
    VIEW_EMPLOYEE_LIST,
    VIEW_EMPLOYEE_BASIC_DETAILS,
    VIEW_EMPLOYEE_PERSONAL_DETAILS,
    VIEW_EMPLOYEE_GOVERNMENT_IDS,
    VIEW_EMPLOYEE_COMPENSATION,
    VIEW_OWN_EMPLOYEE_PROFILE,

    // EMPLOYEE MANAGEMENT
    ADD_EMPLOYEE,
    EDIT_EMPLOYEE,
    DELETE_EMPLOYEE,

    // PAYROLL
    VIEW_PAYROLL,
    PROCESS_PAYROLL,
    VIEW_OWN_PAYROLL,

    // PAYSLIP
    GENERATE_PAYSLIP,
    VIEW_PAYSLIP,
    VIEW_OWN_PAYSLIP,

    // LEAVE
    SUBMIT_LEAVE,
    VIEW_LEAVE_REQUESTS,
    VIEW_LEAVE_HISTORY,
    VIEW_OWN_LEAVE_HISTORY,
    APPROVE_LEAVE,
    REJECT_LEAVE,

    // ATTENDANCE
    TIME_IN,
    TIME_OUT,
    VIEW_ATTENDANCE,
    VIEW_OWN_ATTENDANCE,
    EDIT_ATTENDANCE,
    DELETE_ATTENDANCE,

    // SYSTEM / IT / ADMIN
    ACCESS_SYSTEM_TOOLS,
    MANAGE_USERS,
    RESET_PASSWORD
}
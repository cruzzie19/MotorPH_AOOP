/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Rhynne Gracelle
 */

package service;

import RBAC.Permission;
import model.Employee;

public class AuthorizationService {

    public static boolean hasPermission(Employee employee, Permission permission) {

        if (employee == null) {
            return false;
        }

        if (employee.getRole() == null) {
            return false;
        }

        return employee.getRole().hasPermission(permission);
    }
}
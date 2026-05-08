/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * In-memory session holder for the currently authenticated user.
 * Provides simple static access to set, read, and clear the active session user.
 *
 * <p>Note: this implementation is process-local and not thread-safe.
 * It is suitable for this desktop application context.</p>
 *
 * @author Rhynne Gracelle
 * @see model.Employee
 */
package service;

import model.Employee;

public class SessionManager {

    private static Employee currentUser;

    /**
     * Sets the current authenticated user.
     *
     * @param employee authenticated employee, or null to clear
     */
    public static void setCurrentUser(Employee employee) {
        currentUser = employee;
    }

    /**
     * Returns the current authenticated user.
     *
     * @return active user, or null if no user is logged in
     */
    public static Employee getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current session user.
     */
    public static void logout() {
        currentUser = null;
    }
}

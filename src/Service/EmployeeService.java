/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Rhynne Gracelle
 */

package service;

import RBAC.RBACSetup;
import RBAC.Role;
import model.Employee;
import model.ProbationaryEmployee;
import model.RegularEmployee;
import repository.EmployeeRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeService {

    private final EmployeeRepository repository;
    private final Map<String, Role> roles = RBACSetup.setupRoles();

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> getAllEmployees() {
        try {
            return repository.loadAll();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load employee records.", e);
        }
    }

    public String getNextEmployeeId() {
        int max = 10000;

        for (Employee employee : getAllEmployees()) {
            if (employee == null || employee.getId() == null) {
                continue;
            }

            try {
                int id = Integer.parseInt(employee.getId().trim());
                max = Math.max(max, id);
            } catch (NumberFormatException ignored) {
                // Skip non-numeric employee IDs
            }
        }

        return String.valueOf(max + 1);
    }

    public Employee findById(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }

        for (Employee employee : getAllEmployees()) {
            if (employee != null && employee.getId().equalsIgnoreCase(employeeId.trim())) {
                return employee;
            }
        }
        return null;
    }

    public void addEmployee(EmployeeRequest request) {
        EmployeeValidator.validate(request);

        List<Employee> employees = getAllEmployees();

        for (Employee employee : employees) {
            if (employee.getId().equalsIgnoreCase(request.getId())) {
                throw new IllegalArgumentException("Employee ID already exists.");
            }
        }

        Employee employee = buildEmployee(request);
        employees.add(employee);
        saveAll(employees);
    }

    public void updateEmployee(String originalEmployeeId, EmployeeRequest request) {
        EmployeeValidator.validate(request);

        List<Employee> employees = getAllEmployees();
        boolean updated = false;

        for (int i = 0; i < employees.size(); i++) {
            Employee existing = employees.get(i);

            if (!existing.getId().equalsIgnoreCase(originalEmployeeId)
                    && existing.getId().equalsIgnoreCase(request.getId())) {
                throw new IllegalArgumentException("Employee ID already exists.");
            }

            if (existing.getId().equalsIgnoreCase(originalEmployeeId)) {
                employees.set(i, buildEmployee(request));
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("Employee record not found.");
        }

        saveAll(employees);
    }

    public void deleteEmployee(String employeeId) {
        List<Employee> employees = new ArrayList<>(getAllEmployees());
        boolean removed = employees.removeIf(emp -> emp.getId().equalsIgnoreCase(employeeId));

        if (!removed) {
            throw new IllegalArgumentException("Employee record not found.");
        }

        saveAll(employees);
    }

    public List<Employee> searchEmployees(String keyword) {
        String search = keyword == null ? "" : keyword.trim().toLowerCase();

        if (search.isEmpty()) {
            return getAllEmployees();
        }

        List<Employee> result = new ArrayList<>();

        for (Employee emp : getAllEmployees()) {
            String combined = (
                    safe(emp.getId()) + " " +
                    safe(emp.getLastName()) + " " +
                    safe(emp.getFirstName()) + " " +
                    safe(emp.getStatus()) + " " +
                    safe(emp.getPosition()) + " " +
                    safe(emp.getSupervisor()) + " " +
                    (emp.getRole() != null ? safe(emp.getRole().getName()) : "")
            ).toLowerCase();

            if (combined.contains(search)) {
                result.add(emp);
            }
        }

        return result;
    }

    private Employee buildEmployee(EmployeeRequest request) {
        Employee employee;

        if ("Probationary".equalsIgnoreCase(request.getStatus())) {
            employee = new ProbationaryEmployee(
                    request.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getBirthDate(),
                    request.getBasicSalary(),
                    request.getRiceSubsidy(),
                    request.getPhoneAllowance(),
                    request.getClothingAllowance(),
                    request.getGrossSemiMonthlyRate(),
                    request.getHourlyRate()
            );
        } else {
            employee = new RegularEmployee(
                    request.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getBirthDate(),
                    request.getBasicSalary(),
                    request.getRiceSubsidy(),
                    request.getPhoneAllowance(),
                    request.getClothingAllowance(),
                    request.getGrossSemiMonthlyRate(),
                    request.getHourlyRate()
            );
        }

        employee.setDepartment(request.getDepartment());
        employee.setAddress(request.getAddress());
        employee.setPhone(request.getPhone());
        employee.setSssNumber(request.getSssNumber());
        employee.setPhilHealthNumber(request.getPhilHealthNumber());
        employee.setTinNumber(request.getTinNumber());
        employee.setPagIbigNumber(request.getPagIbigNumber());
        employee.setStatus(request.getStatus());
        employee.setPosition(request.getPosition());
        employee.setSupervisor(request.getSupervisor());
        employee.setRole(resolveRole(request.getRoleName()));

        return employee;
    }

    private Role resolveRole(String roleName) {
        Role role = roles.get(roleName == null ? "" : roleName.trim().toUpperCase());
        if (role == null) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
        return role;
    }

    private void saveAll(List<Employee> employees) {
        try {
            repository.saveAll(employees);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save employee records.", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
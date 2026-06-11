package repository;

import RBAC.RBACSetup;
import RBAC.Role;
import model.Employee;
import model.ProbationaryEmployee;
import model.RegularEmployee;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DbEmployeeRepository implements EmployeeRepository {

    private static final Map<String, Role> ROLES = RBACSetup.setupRoles();

    @Override
    public List<Employee> loadAll() throws IOException {
        String sql = """
                SELECT employee_id,
                       last_name,
                       first_name,
                       birthday,
                       address,
                       phone_number,
                       sss_number,
                       philhealth_number,
                       tin_number,
                       pag_ibig_number,
                       status,
                       position,
                       supervisor_id,
                       role,
                       basic_salary,
                       rice_subsidy,
                       phone_allowance,
                       clothing_allowance,
                       gross_semi_monthly_rate,
                       hourly_rate
                  FROM employee
                 ORDER BY employee_id
                """;

        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DbConnectionFactory.open();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                employees.add(mapEmployee(resultSet));
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load employees from the database", ex);
        }

        return employees;
    }

    @Override
    public void saveAll(List<Employee> employees) throws IOException {
        String upsertSql = """
                INSERT INTO employee (
                    employee_id,
                    last_name,
                    first_name,
                    birthday,
                    address,
                    phone_number,
                    sss_number,
                    philhealth_number,
                    tin_number,
                    pag_ibig_number,
                    status,
                    position,
                    supervisor_id,
                    role,
                    basic_salary,
                    rice_subsidy,
                    phone_allowance,
                    clothing_allowance,
                    gross_semi_monthly_rate,
                    hourly_rate
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (employee_id) DO UPDATE SET
                    last_name = EXCLUDED.last_name,
                    first_name = EXCLUDED.first_name,
                    birthday = EXCLUDED.birthday,
                    address = EXCLUDED.address,
                    phone_number = EXCLUDED.phone_number,
                    sss_number = EXCLUDED.sss_number,
                    philhealth_number = EXCLUDED.philhealth_number,
                    tin_number = EXCLUDED.tin_number,
                    pag_ibig_number = EXCLUDED.pag_ibig_number,
                    status = EXCLUDED.status,
                    position = EXCLUDED.position,
                    supervisor_id = EXCLUDED.supervisor_id,
                    role = EXCLUDED.role,
                    basic_salary = EXCLUDED.basic_salary,
                    rice_subsidy = EXCLUDED.rice_subsidy,
                    phone_allowance = EXCLUDED.phone_allowance,
                    clothing_allowance = EXCLUDED.clothing_allowance,
                    gross_semi_monthly_rate = EXCLUDED.gross_semi_monthly_rate,
                    hourly_rate = EXCLUDED.hourly_rate
                """;

        try (Connection connection = DbConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement upsert = connection.prepareStatement(upsertSql)) {
                for (Employee employee : employees) {
                    bindEmployee(upsert, employee);
                    upsert.addBatch();
                }
                upsert.executeBatch();
            }

            deleteMissingEmployees(connection, employees);
            connection.commit();
        } catch (SQLException ex) {
            throw new IOException("Failed to save employees to the database", ex);
        }
    }

    private void deleteMissingEmployees(Connection connection, List<Employee> employees) throws SQLException {
        Set<String> employeeIds = new HashSet<>();
        for (Employee employee : employees) {
            if (employee != null && employee.getId() != null && !employee.getId().trim().isEmpty()) {
                employeeIds.add(employee.getId().trim());
            }
        }

        if (employeeIds.isEmpty()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM employee");
            }
            return;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM employee WHERE employee_id NOT IN (");
        for (int i = 0; i < employeeIds.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (String employeeId : employeeIds) {
                statement.setString(index++, employeeId);
            }
            statement.executeUpdate();
        }
    }

    private Employee mapEmployee(ResultSet resultSet) throws SQLException {
        String employeeId = safeTrim(resultSet.getString("employee_id"));
        String firstName = safeTrim(resultSet.getString("first_name"));
        String lastName = safeTrim(resultSet.getString("last_name"));
        LocalDate birthday = resultSet.getDate("birthday") != null
                ? resultSet.getDate("birthday").toLocalDate()
                : LocalDate.now();

        BigDecimal basicSalary = nonNullDecimal(resultSet.getBigDecimal("basic_salary"));
        BigDecimal riceSubsidy = nonNullDecimal(resultSet.getBigDecimal("rice_subsidy"));
        BigDecimal phoneAllowance = nonNullDecimal(resultSet.getBigDecimal("phone_allowance"));
        BigDecimal clothingAllowance = nonNullDecimal(resultSet.getBigDecimal("clothing_allowance"));
        BigDecimal grossSemiMonthlyRate = nonNullDecimal(resultSet.getBigDecimal("gross_semi_monthly_rate"));
        BigDecimal hourlyRate = nonNullDecimal(resultSet.getBigDecimal("hourly_rate"));

        String status = safeTrim(resultSet.getString("status"));
        Employee employee = createEmployeeByStatus(
                status,
                employeeId,
                firstName,
                lastName,
                birthday,
                basicSalary,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                grossSemiMonthlyRate,
                hourlyRate
        );

        employee.setAddress(safeTrim(resultSet.getString("address")));
        setIfNotBlankPhone(employee, safeTrim(resultSet.getString("phone_number")));
        setIfNotBlankSss(employee, safeTrim(resultSet.getString("sss_number")));
        setIfNotBlankPhilHealth(employee, safeTrim(resultSet.getString("philhealth_number")));
        setIfNotBlankTin(employee, safeTrim(resultSet.getString("tin_number")));
        setIfNotBlankPagIbig(employee, safeTrim(resultSet.getString("pag_ibig_number")));

        String position = safeTrim(resultSet.getString("position"));
        if (!position.isBlank()) {
            employee.setPosition(position);
        }

        String supervisorId = safeTrim(resultSet.getString("supervisor_id"));
        if (!supervisorId.isBlank()) {
            employee.setSupervisor(supervisorId);
        }

        String roleName = safeTrim(resultSet.getString("role"));
        if (!roleName.isBlank()) {
            Role role = ROLES.get(roleName.toUpperCase());
            if (role != null) {
                employee.setRole(role);
            }
        }

        if (!status.isBlank()) {
            employee.setStatus(status);
        }

        return employee;
    }

    private Employee createEmployeeByStatus(
            String status,
            String id,
            String first,
            String last,
            LocalDate birth,
            BigDecimal basic,
            BigDecimal rice,
            BigDecimal phoneA,
            BigDecimal clothA,
            BigDecimal semi,
            BigDecimal hour
    ) {
        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
                    id, first, last, birth, basic, rice, phoneA, clothA, semi, hour
            );
        }

        return new RegularEmployee(
                id, first, last, birth, basic, rice, phoneA, clothA, semi, hour
        );
    }

    private void bindEmployee(PreparedStatement statement, Employee employee) throws SQLException, IOException {
        statement.setString(1, requireText(employee.getId(), "employee_id"));
        statement.setString(2, requireText(employee.getLastName(), "last_name"));
        statement.setString(3, requireText(employee.getFirstName(), "first_name"));
        statement.setDate(4, java.sql.Date.valueOf(employee.getBirthDate()));
        statement.setString(5, nullIfBlank(employee.getAddress()));
        statement.setString(6, nullIfBlank(employee.getPhone()));
        statement.setString(7, nullIfBlank(employee.getSssNumber()));
        statement.setString(8, nullIfBlank(employee.getPhilHealthNumber()));
        statement.setString(9, nullIfBlank(employee.getTinNumber()));
        statement.setString(10, nullIfBlank(employee.getPagIbigNumber()));
        statement.setString(11, requireText(employee.getStatus(), "status"));
        statement.setString(12, requireText(employee.getPosition(), "position"));
        statement.setString(13, nullIfBlank(employee.getSupervisor()));
        statement.setString(14, employee.getRole() != null ? employee.getRole().getName() : null);
        statement.setBigDecimal(15, nonNullDecimal(employee.getBasicSalary()));
        statement.setBigDecimal(16, nonNullDecimal(employee.getRiceSubsidy()));
        statement.setBigDecimal(17, nonNullDecimal(employee.getPhoneAllowance()));
        statement.setBigDecimal(18, nonNullDecimal(employee.getClothingAllowance()));
        statement.setBigDecimal(19, nonNullDecimal(employee.getGrossSemiMonthlyRate()));
        statement.setBigDecimal(20, nonNullDecimal(employee.getHourlyRate()));
    }

    private String requireText(String value, String fieldName) throws IOException {
        String trimmed = safeTrim(value);
        if (trimmed.isBlank()) {
            throw new IOException("Employee record is missing required field: " + fieldName);
        }
        return trimmed;
    }

    private String nullIfBlank(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isBlank() ? null : trimmed;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal nonNullDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void setIfNotBlankPhone(Employee employee, String value) {
        if (!value.isBlank()) {
            employee.setPhone(value);
        }
    }

    private void setIfNotBlankSss(Employee employee, String value) {
        if (!value.isBlank()) {
            employee.setSssNumber(value);
        }
    }

    private void setIfNotBlankPhilHealth(Employee employee, String value) {
        if (!value.isBlank()) {
            employee.setPhilHealthNumber(value);
        }
    }

    private void setIfNotBlankTin(Employee employee, String value) {
        if (!value.isBlank()) {
            employee.setTinNumber(value);
        }
    }

    private void setIfNotBlankPagIbig(Employee employee, String value) {
        if (!value.isBlank()) {
            employee.setPagIbigNumber(value);
        }
    }
}

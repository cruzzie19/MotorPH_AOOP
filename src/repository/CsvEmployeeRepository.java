/**
 *
 * @author Leianna Cruz
 */

package repository;

import RBAC.RBACSetup;
import RBAC.Role;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.Employee;
import model.ProbationaryEmployee;
import model.RegularEmployee;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvEmployeeRepository implements EmployeeRepository {

    private final Path csvPath;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("M/d/yyyy");

    public CsvEmployeeRepository(String path) {
        this.csvPath = Paths.get(path);
    }

    @Override
    public List<Employee> loadAll() throws IOException {
        List<Employee> employees = new ArrayList<>();
        Map<String, Role> roles = RBACSetup.setupRoles();

        try (CSVReader csv = new CSVReader(
                new FileReader(csvPath.toFile(), StandardCharsets.UTF_8))) {

            csv.readNext(); // skip header
            String[] parts;

            while ((parts = csv.readNext()) != null) {
                if (parts.length < 20) {
                    continue;
                }

                String id = safe(parts, 0);
                String last = safe(parts, 1);
                String first = safe(parts, 2);
                LocalDate birth = parseDateOrNow(safe(parts, 3));

                String address = safe(parts, 4);
                String phone = fixLongNumber(safe(parts, 5));
                String sss = fixLongNumber(safe(parts, 6));
                String philHealth = fixLongNumber(safe(parts, 7));
                String tin = fixLongNumber(safe(parts, 8));
                String pagIbig = fixLongNumber(safe(parts, 9));

                String status = safe(parts, 10);
                String position = safe(parts, 11);
                String supervisor = safe(parts, 12);

                BigDecimal basic = parseDecimalOrZero(cleanNumber(safe(parts, 13)));
                BigDecimal rice = parseDecimalOrZero(cleanNumber(safe(parts, 14)));
                BigDecimal phoneA = parseDecimalOrZero(cleanNumber(safe(parts, 15)));
                BigDecimal clothA = parseDecimalOrZero(cleanNumber(safe(parts, 16)));
                BigDecimal semi = parseDecimalOrZero(cleanNumber(safe(parts, 17)));
                BigDecimal hour = parseDecimalOrZero(cleanNumber(safe(parts, 18)));

                String roleName = safe(parts, 19).toUpperCase();
                Role role = roles.get(roleName);

                Employee employee = createEmployeeByStatus(
                        status,
                        id,
                        first,
                        last,
                        birth,
                        basic,
                        rice,
                        phoneA,
                        clothA,
                        semi,
                        hour
                );

                employee.setAddress(address);
                setIfNotBlankPhone(employee, phone);
                setIfNotBlankSss(employee, sss);
                setIfNotBlankPhilHealth(employee, philHealth);
                setIfNotBlankTin(employee, tin);
                setIfNotBlankPagIbig(employee, pagIbig);

                employee.setStatus(status);
                employee.setPosition(position);
                employee.setSupervisor(supervisor);

                if (role != null) {
                    employee.setRole(role);
                }

                employees.add(employee);
            }

        } catch (CsvValidationException ex) {
            throw new IOException("Invalid CSV format.", ex);
        }

        return employees;
    }

    @Override
    public void saveAll(List<Employee> employees) throws IOException {
        Path temp = csvPath.resolveSibling(csvPath.getFileName() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
             CSVWriter csv = new CSVWriter(writer)) {

            String[] header = {
                    "Employee #",
                    "Last Name",
                    "First Name",
                    "Birthday",
                    "Address",
                    "Phone Number",
                    "SSS #",
                    "Philhealth #",
                    "TIN #",
                    "Pag-ibig #",
                    "Status",
                    "Position",
                    "Immediate Supervisor",
                    "Basic Salary",
                    "Rice Subsidy",
                    "Phone Allowance",
                    "Clothing Allowance",
                    "Gross Semi-monthly Rate",
                    "Hourly Rate",
                    "Role"
            };

            csv.writeNext(header);

            for (Employee e : employees) {
                String[] row = {
                        e.getId(),
                        e.getLastName(),
                        e.getFirstName(),
                        e.getBirthDate().format(DATE_FMT),
                        e.getAddress(),
                        e.getPhone(),
                        e.getSssNumber(),
                        e.getPhilHealthNumber(),
                        e.getTinNumber(),
                        e.getPagIbigNumber(),
                        e.getStatus(),
                        e.getPosition(),
                        e.getSupervisor(),
                        e.getBasicSalary().toPlainString(),
                        e.getRiceSubsidy().toPlainString(),
                        e.getPhoneAllowance().toPlainString(),
                        e.getClothingAllowance().toPlainString(),
                        e.getGrossSemiMonthlyRate().toPlainString(),
                        e.getHourlyRate().toPlainString(),
                        e.getRole() != null ? e.getRole().getName() : ""
                };

                csv.writeNext(row);
            }
        }

        Files.deleteIfExists(csvPath);
        Files.move(temp, csvPath);
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

    private LocalDate parseDateOrNow(String txt) {
        try {
            return LocalDate.parse(txt, DATE_FMT);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private BigDecimal parseDecimalOrZero(String txt) {
        try {
            return new BigDecimal(txt);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private String fixLongNumber(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        value = value.trim();

        if (value.contains("E") || value.contains("e")) {
            try {
                value = new BigDecimal(value).toPlainString();
            } catch (NumberFormatException ignored) {
            }
        }

        return value;
    }

    private String cleanNumber(String value) {
        if (value == null) {
            return "0";
        }
        return value.replace(",", "").trim();
    }

    private String safe(String[] parts, int index) {
        if (parts == null || index < 0 || index >= parts.length || parts[index] == null) {
            return "";
        }
        return parts[index].trim();
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
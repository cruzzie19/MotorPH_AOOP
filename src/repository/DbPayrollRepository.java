/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Payslip;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed implementation of {@link PayrollRepository}.
 *
 * <p>Uses {@link DbConnectionFactory} for connections, the same way
 * {@link DbEmployeeRepository} does, so it honors the same
 * {@code motorph.db.url} / {@code MOTORPH_DB_URL} / {@code config/motorph-db.properties}
 * configuration already used elsewhere in the app.</p>
 *
 * <p>A payslip is uniquely identified by (employee_id, period_start,
 * period_end), matching the {@code uq_payroll_employee_period} constraint
 * on the {@code payroll} table. Saving re-generates (re-runs) a payslip for
 * the same employee + month, so {@code save} performs an upsert rather than
 * always inserting a new row.</p>
 */
public class DbPayrollRepository implements PayrollRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO payroll (
                employee_id,
                employee_name,
                position,
                employee_type,
                period_start,
                period_end,
                hours_worked,
                hourly_rate,
                basic_pay,
                rice_subsidy,
                phone_allowance,
                clothing_allowance,
                total_allowance,
                gross_pay,
                sss_deduction,
                philhealth_deduction,
                pag_ibig_deduction,
                tax_withheld,
                total_deductions,
                net_pay
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (employee_id, period_start, period_end) DO UPDATE SET
                employee_name = EXCLUDED.employee_name,
                position = EXCLUDED.position,
                employee_type = EXCLUDED.employee_type,
                hours_worked = EXCLUDED.hours_worked,
                hourly_rate = EXCLUDED.hourly_rate,
                basic_pay = EXCLUDED.basic_pay,
                rice_subsidy = EXCLUDED.rice_subsidy,
                phone_allowance = EXCLUDED.phone_allowance,
                clothing_allowance = EXCLUDED.clothing_allowance,
                total_allowance = EXCLUDED.total_allowance,
                gross_pay = EXCLUDED.gross_pay,
                sss_deduction = EXCLUDED.sss_deduction,
                philhealth_deduction = EXCLUDED.philhealth_deduction,
                pag_ibig_deduction = EXCLUDED.pag_ibig_deduction,
                tax_withheld = EXCLUDED.tax_withheld,
                total_deductions = EXCLUDED.total_deductions,
                net_pay = EXCLUDED.net_pay
            RETURNING payroll_id
            """;

    private static final String SELECT_BASE = """
            SELECT payroll_id,
                   employee_id,
                   employee_name,
                   position,
                   employee_type,
                   period_start,
                   period_end,
                   hours_worked,
                   hourly_rate,
                   basic_pay,
                   rice_subsidy,
                   phone_allowance,
                   clothing_allowance,
                   total_allowance,
                   gross_pay,
                   sss_deduction,
                   philhealth_deduction,
                   pag_ibig_deduction,
                   tax_withheld,
                   total_deductions,
                   net_pay
              FROM payroll
            """;

    @Override
    public void save(Payslip payslip) throws IOException {
        if (payslip == null) {
            throw new IOException("Payslip cannot be null.");
        }
        if (isBlank(payslip.getEmployeeId())) {
            throw new IOException("Payslip is missing an employee ID.");
        }
        if (payslip.getPayrollMonth() == null) {
            throw new IOException("Payslip is missing a payroll month.");
        }

        LocalDate periodStart = payslip.getPayrollMonth().atDay(1);
        LocalDate periodEnd = payslip.getPayrollMonth().atEndOfMonth();

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {

            statement.setString(1, payslip.getEmployeeId().trim());
            statement.setString(2, nullToEmpty(payslip.getEmployeeName()));
            statement.setString(3, nullToEmpty(payslip.getPosition()));
            statement.setString(4, nullToEmpty(payslip.getEmployeeType()));
            statement.setDate(5, Date.valueOf(periodStart));
            statement.setDate(6, Date.valueOf(periodEnd));
            statement.setBigDecimal(7, nonNull(payslip.getHoursWorked()));
            statement.setBigDecimal(8, nonNull(payslip.getHourlyRate()));
            statement.setBigDecimal(9, nonNull(payslip.getBasicPay()));
            statement.setBigDecimal(10, nonNull(payslip.getRiceSubsidy()));
            statement.setBigDecimal(11, nonNull(payslip.getPhoneAllowance()));
            statement.setBigDecimal(12, nonNull(payslip.getClothingAllowance()));
            statement.setBigDecimal(13, nonNull(payslip.getTotalAllowance()));
            statement.setBigDecimal(14, nonNull(payslip.getGrossPay()));
            statement.setBigDecimal(15, nonNull(payslip.getSss()));
            statement.setBigDecimal(16, nonNull(payslip.getPhilHealth()));
            statement.setBigDecimal(17, nonNull(payslip.getPagIbig()));
            statement.setBigDecimal(18, nonNull(payslip.getTax()));
            statement.setBigDecimal(19, nonNull(payslip.getTotalDeductions()));
            statement.setBigDecimal(20, nonNull(payslip.getNetPay()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    payslip.setPayslipId(resultSet.getLong("payroll_id"));
                }
            }

        } catch (SQLException ex) {
            throw new IOException("Failed to save payslip to the database.", ex);
        }
    }

    @Override
    public Payslip findByEmployeeAndMonth(String employeeId, YearMonth month) throws IOException {
        if (isBlank(employeeId) || month == null) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE employee_id = ? AND period_start = ? AND period_end = ?";

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId.trim());
            statement.setDate(2, Date.valueOf(month.atDay(1)));
            statement.setDate(3, Date.valueOf(month.atEndOfMonth()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPayslip(resultSet);
                }
            }

            return null;

        } catch (SQLException ex) {
            throw new IOException("Failed to load payslip from the database.", ex);
        }
    }

    @Override
    public List<Payslip> findByEmployeeId(String employeeId) throws IOException {
        List<Payslip> results = new ArrayList<>();
        if (isBlank(employeeId)) {
            return results;
        }

        String sql = SELECT_BASE + " WHERE employee_id = ? ORDER BY period_start DESC";

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapPayslip(resultSet));
                }
            }

        } catch (SQLException ex) {
            throw new IOException("Failed to load payslips from the database.", ex);
        }

        return results;
    }

    @Override
    public List<Payslip> findByMonth(YearMonth month) throws IOException {
        List<Payslip> results = new ArrayList<>();
        if (month == null) {
            return results;
        }

        String sql = SELECT_BASE + " WHERE period_start = ? AND period_end = ? ORDER BY employee_id";

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(month.atDay(1)));
            statement.setDate(2, Date.valueOf(month.atEndOfMonth()));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapPayslip(resultSet));
                }
            }

        } catch (SQLException ex) {
            throw new IOException("Failed to load payslips from the database.", ex);
        }

        return results;
    }

    @Override
    public List<Payslip> findAll() throws IOException {
        List<Payslip> results = new ArrayList<>();

        String sql = SELECT_BASE + " ORDER BY period_start DESC, employee_id";

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(mapPayslip(resultSet));
            }

        } catch (SQLException ex) {
            throw new IOException("Failed to load payslips from the database.", ex);
        }

        return results;
    }

    @Override
    public void delete(String employeeId, YearMonth month) throws IOException {
        if (isBlank(employeeId) || month == null) {
            return;
        }

        String sql = "DELETE FROM payroll WHERE employee_id = ? AND period_start = ? AND period_end = ?";

        try (Connection connection = DbConnectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId.trim());
            statement.setDate(2, Date.valueOf(month.atDay(1)));
            statement.setDate(3, Date.valueOf(month.atEndOfMonth()));

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IOException("Failed to delete payslip from the database.", ex);
        }
    }

    private Payslip mapPayslip(ResultSet resultSet) throws SQLException {
        Payslip payslip = new Payslip();

        payslip.setPayslipId(resultSet.getLong("payroll_id"));
        payslip.setEmployeeId(resultSet.getString("employee_id"));
        payslip.setEmployeeName(resultSet.getString("employee_name"));
        payslip.setPosition(resultSet.getString("position"));
        payslip.setEmployeeType(resultSet.getString("employee_type"));

        LocalDate periodStart = resultSet.getDate("period_start").toLocalDate();
        payslip.setPayrollMonth(YearMonth.from(periodStart));

        payslip.setHoursWorked(resultSet.getBigDecimal("hours_worked"));
        payslip.setHourlyRate(resultSet.getBigDecimal("hourly_rate"));
        payslip.setBasicPay(resultSet.getBigDecimal("basic_pay"));
        payslip.setRiceSubsidy(resultSet.getBigDecimal("rice_subsidy"));
        payslip.setPhoneAllowance(resultSet.getBigDecimal("phone_allowance"));
        payslip.setClothingAllowance(resultSet.getBigDecimal("clothing_allowance"));
        payslip.setTotalAllowance(resultSet.getBigDecimal("total_allowance"));
        payslip.setGrossPay(resultSet.getBigDecimal("gross_pay"));
        payslip.setSss(resultSet.getBigDecimal("sss_deduction"));
        payslip.setPhilHealth(resultSet.getBigDecimal("philhealth_deduction"));
        payslip.setPagIbig(resultSet.getBigDecimal("pag_ibig_deduction"));
        payslip.setTax(resultSet.getBigDecimal("tax_withheld"));
        payslip.setTotalDeductions(resultSet.getBigDecimal("total_deductions"));
        payslip.setNetPay(resultSet.getBigDecimal("net_pay"));

        return payslip;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

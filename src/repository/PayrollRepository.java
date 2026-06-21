/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Payslip;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

/**
 * Repository contract for payslip persistence.
 *
 * <p>Implementations only handle data access (save / load / list / delete).
 * Payroll computation belongs in {@code service.PayrollComputationService};
 * data-handling orchestration for the GUI belongs in
 * {@code service.PayslipService}.</p>
 */
public interface PayrollRepository {

    /**
     * Inserts a new payslip, or updates the existing payslip for the same
     * employee + payroll month if one already exists.
     *
     * @param payslip payslip to persist
     * @throws IOException if the payslip cannot be saved
     */
    void save(Payslip payslip) throws IOException;

    /**
     * Finds the payslip for a specific employee and payroll month.
     *
     * @param employeeId employee ID
     * @param month payroll month
     * @return matching payslip, or {@code null} if none exists
     * @throws IOException if the lookup fails
     */
    Payslip findByEmployeeAndMonth(String employeeId, YearMonth month) throws IOException;

    /**
     * Finds every payslip belonging to a specific employee, most recent
     * payroll month first.
     *
     * @param employeeId employee ID
     * @return matching payslips
     * @throws IOException if the lookup fails
     */
    List<Payslip> findByEmployeeId(String employeeId) throws IOException;

    /**
     * Finds every payslip generated for a specific payroll month.
     *
     * @param month payroll month
     * @return matching payslips
     * @throws IOException if the lookup fails
     */
    List<Payslip> findByMonth(YearMonth month) throws IOException;

    /**
     * Finds every payslip on record.
     *
     * @return all payslips
     * @throws IOException if the lookup fails
     */
    List<Payslip> findAll() throws IOException;

    /**
     * Deletes the payslip for a specific employee and payroll month.
     *
     * @param employeeId employee ID
     * @param month payroll month
     * @throws IOException if the delete fails
     */
    void delete(String employeeId, YearMonth month) throws IOException;
}

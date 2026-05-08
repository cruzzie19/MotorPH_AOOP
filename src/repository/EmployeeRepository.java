/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Employee;
import java.io.IOException;
import java.util.List;

public interface EmployeeRepository {
    List<Employee> loadAll() throws IOException;
    void saveAll(List<Employee> employees) throws IOException;
}
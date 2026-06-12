/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Leave;
import java.util.List;

import java.io.IOException;

public interface LeaveRepository {
    List<Leave> findAll() throws IOException;
    List<Leave> findByEmployeeId(String employeeId) throws IOException;
    List<Leave> findByStatus(String status) throws IOException;

    void add(Leave leave) throws IOException ;
    void update(Leave leave) throws IOException ;
    void delete(int leaveId) throws IOException ;

    Leave findById(int leaveId) throws IOException;
}
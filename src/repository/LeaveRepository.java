/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Leave;
import java.util.List;

public interface LeaveRepository {
    List<Leave> findAll();
    List<Leave> findByEmployeeId(String employeeId);
    List<Leave> findByStatus(String status);

    void add(Leave leave);
    void update(Leave leave);
    void delete(int leaveId);

    Leave findById(int leaveId);
    int getNextLeaveId();
}
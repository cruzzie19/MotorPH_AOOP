/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import model.Leave;
import java.time.OffsetDateTime;

/**
 *
 * @author Elizabeth
 */
public class DbLeaveRepository implements LeaveRepository{
    
    @Override
    public List<Leave> findAll() throws IOException{
        String sql = "SELECT * FROM leave_request ORDER BY created_at DESC";
        
        List<Leave> leaves = new ArrayList<>();
        
        try (Connection connection = DbConnectionFactory.open();) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Leave leave = new Leave();
                
                leave.setLeaveId((int) resultSet.getLong("leave_id"));
                leave.setEmployeeId(resultSet.getString("employee_id"));
                leave.setLeaveType(resultSet.getString("leave_type"));
                leave.setNotes(resultSet.getString("notes"));
                leave.setStatus(resultSet.getString("status"));
                leave.setReviewedBy(resultSet.getString("reviewed_by"));
                if (resultSet.getDate("start_date") != null) {
                    leave.setStartDate(resultSet.getDate("start_date").toLocalDate().toString());
                }
                if (resultSet.getDate("end_date") != null) {
                    leave.setEndDate(resultSet.getDate("end_date").toLocalDate().toString());
                }
                leave.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
                
                leaves.add(leave);
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load employees from the database", ex);
        }

        return leaves;
    }
    
    
    @Override
    public List<Leave> findByEmployeeId(String employeeId) throws IOException{
        String sql = "SELECT * FROM leave_request WHERE employee_id = ? ORDER BY created_at ";
        
        List<Leave> leaves = new ArrayList<>();
        
        try (Connection connection = DbConnectionFactory.open();PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            
            try(ResultSet resultSet = statement.executeQuery()){
            
                while (resultSet.next()) {
                    Leave leave = new Leave();

                    leave.setLeaveId((int) resultSet.getLong("leave_id"));
                    leave.setEmployeeId(resultSet.getString("employee_id"));
                    leave.setLeaveType(resultSet.getString("leave_type"));
                    leave.setNotes(resultSet.getString("notes"));
                    leave.setStatus(resultSet.getString("status"));
                    leave.setReviewedBy(resultSet.getString("reviewed_by"));
                    if (resultSet.getDate("start_date") != null) {
                        leave.setStartDate(resultSet.getDate("start_date").toLocalDate().toString());
                    }
                    if (resultSet.getDate("end_date") != null) {
                        leave.setEndDate(resultSet.getDate("end_date").toLocalDate().toString());
                    }
                    leave.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));

                    leaves.add(leave);
                }
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load employees from the database", ex);
        }

        return leaves;
    }
    
    @Override
    public List<Leave> findByStatus(String status) throws IOException{
        String sql = "SELECT * FROM leave_request WHERE status = ? ORDER BY created_at DESC";
        
        List<Leave> leaves = new ArrayList<>();
        
        try (Connection connection = DbConnectionFactory.open();PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, status);
            
            try(ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()) {
                    Leave leave = new Leave();

                    leave.setLeaveId((int) resultSet.getLong("leave_id"));
                    leave.setEmployeeId(resultSet.getString("employee_id"));
                    leave.setLeaveType(resultSet.getString("leave_type"));
                    leave.setNotes(resultSet.getString("notes"));
                    leave.setStatus(resultSet.getString("status"));
                    leave.setReviewedBy(resultSet.getString("reviewed_by"));
                    if (resultSet.getDate("start_date") != null) {
                        leave.setStartDate(resultSet.getDate("start_date").toLocalDate().toString());
                    }
                    if (resultSet.getDate("end_date") != null) {
                        leave.setEndDate(resultSet.getDate("end_date").toLocalDate().toString());
                    }
                    leave.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));

                    leaves.add(leave);
                }
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load employees from the database", ex);
        }

        return leaves;
    }
    
    @Override
    public Leave findById(int leaveId) throws IOException{
        String sql = "SELECT * FROM leave_request WHERE leave_id = ?";
        
        try (Connection connection = DbConnectionFactory.open();PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leaveId);

            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()) {
                    Leave leave = new Leave();

                    leave.setLeaveId((int) resultSet.getLong("leave_id"));
                    leave.setEmployeeId(resultSet.getString("employee_id"));
                    leave.setLeaveType(resultSet.getString("leave_type"));
                    leave.setNotes(resultSet.getString("notes"));
                    leave.setStatus(resultSet.getString("status"));
                    leave.setReviewedBy(resultSet.getString("reviewed_by"));
                    if (resultSet.getDate("start_date") != null) {
                        leave.setStartDate(resultSet.getDate("start_date").toLocalDate().toString());
                    }
                    if (resultSet.getDate("end_date") != null) {
                        leave.setEndDate(resultSet.getDate("end_date").toLocalDate().toString());
                    }
                    leave.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));

                    return leave;
                }
            }
        } catch (SQLException ex) {
            throw new IOException("Failed to load employees from the database", ex);
        }
        
        return null;
    }
    
    
    @Override
    public void add(Leave leave) throws IOException {
        String sql = """
        INSERT INTO leave_request (employee_id, leave_type, start_date, end_date, notes, status, reviewed_by)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leave.getEmployeeId());
            statement.setString(2, leave.getLeaveType());
            statement.setDate(3, leave.getStartDate() != null ? java.sql.Date.valueOf(leave.getStartDate()) : null);
            statement.setDate(4, leave.getEndDate() != null ? java.sql.Date.valueOf(leave.getEndDate()) : null);
            statement.setString(5, leave.getNotes());
            statement.setString(6, leave.getStatus() != null ? leave.getStatus() : "Pending");
            statement.setString(7, leave.getReviewedBy());

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IOException("Failed to add new leave request to the database.", ex);
        }
    }
    
    @Override
    public void update(Leave leave) throws IOException {
        String sql = """
                    UPDATE leave_request 
                    SET employee_id = ?, leave_type = ?, start_date = ?, end_date = ?, notes = ?, status = ?, reviewed_by = ?
                    WHERE leave_id = ?
                    """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leave.getEmployeeId());
            statement.setString(2, leave.getLeaveType());
            statement.setDate(3, leave.getStartDate() != null ? java.sql.Date.valueOf(leave.getStartDate()) : null);
            statement.setDate(4, leave.getEndDate() != null ? java.sql.Date.valueOf(leave.getEndDate()) : null);
            statement.setString(5, leave.getNotes());
            statement.setString(6, leave.getStatus());
            statement.setString(7, leave.getReviewedBy());
            statement.setLong(8, leave.getLeaveId());

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IOException("Failed to update leave request." + leave.getLeaveId(), ex);
        }
    }
    
    @Override
    public void delete(int leaveId) throws IOException {
        String sql = "DELETE FROM leave_request WHERE leave_id = ?";

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leaveId);
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IOException("Failed to delete leave request." + leaveId, ex);
        }
    }
}

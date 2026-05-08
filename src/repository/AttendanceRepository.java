/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

/**
 * Repository contract for attendance record persistence.
 * Provides CRUD-style operations scoped by employee and date.
 *
 * @author Rhynne Gracelle
 * @see model.AttendanceRecord
 */

import model.AttendanceRecord;
import java.util.List;

public interface AttendanceRepository {
    /**
     * Retrieves all attendance records.
     *
     * @return complete attendance list
     */
    List<AttendanceRecord> findAll();

    /**
     * Retrieves attendance records for a specific employee.
     *
     * @param employeeId employee ID filter
     * @return matching records
     */
    List<AttendanceRecord> findByEmployeeId(String employeeId);

    /**
     * Finds a single attendance record by employee and date.
     *
     * @param employeeId employee ID
     * @param date date key (MM/dd/yyyy)
     * @return matching record, or null if not found
     */
    AttendanceRecord findByEmployeeIdAndDate(String employeeId, String date);

    /**
     * Inserts a new attendance record.
     *
     * @param record attendance entry to add
     */
    void add(AttendanceRecord record);

    /**
     * Updates an existing attendance record.
     *
     * @param record attendance entry to update
     */
    void update(AttendanceRecord record);

    /**
     * Deletes an attendance record by employee and date.
     *
     * @param employeeId employee ID
     * @param date date key (MM/dd/yyyy)
     */
    void delete(String employeeId, String date);
}
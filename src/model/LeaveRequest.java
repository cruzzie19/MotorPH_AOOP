/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Leianna Cruz
 */
package model;

import java.time.OffsetDateTime;

public interface LeaveRequest {
    int getLeaveId();
    String getEmployeeId();
    String getLeaveType();
    String getStartDate();
    String getEndDate();
    String getNotes();
    String getStatus();
    String getReviewedBy();
    OffsetDateTime getCreatedAt();
}

package repository;

import java.sql.PreparedStatement;

import model.AttendanceRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DbAttendanceRepository implements AttendanceRepository {

    @Override
    public List<AttendanceRecord> findAll() {

        String sql = """
        SELECT
            a.employee_id,
            e.last_name,
            e.first_name,
            a.work_date,
            a.log_in,
            a.log_out
        FROM attendance a
        JOIN employee e
            ON a.employee_id = e.employee_id
        ORDER BY a.work_date DESC
        """;

        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection connection = DbConnectionFactory.open(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {

                AttendanceRecord record = new AttendanceRecord();

                record.setEmployeeId(
                        resultSet.getString("employee_id"));

                record.setLastName(
                        resultSet.getString("last_name"));

                record.setFirstName(
                        resultSet.getString("first_name"));

                record.setDate(
                        resultSet.getDate("work_date")
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

                record.setLogIn(
                        resultSet.getTime("log_in") == null
                        ? ""
                        : resultSet.getTime("log_in").toString());

                record.setLogOut(
                        resultSet.getTime("log_out") == null
                        ? ""
                        : resultSet.getTime("log_out").toString());

                records.add(record);
            }

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to load attendance records",
                    ex);
        }

        return records;
    }

    @Override
    public List<AttendanceRecord> findByEmployeeId(String employeeId) {

        String sql = """
        SELECT
            a.employee_id,
            e.last_name,
            e.first_name,
            a.work_date,
            a.log_in,
            a.log_out
        FROM attendance a
        JOIN employee e
            ON a.employee_id = e.employee_id
        WHERE a.employee_id = ?
        ORDER BY a.work_date DESC
        """;

        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    AttendanceRecord record = new AttendanceRecord();

                    record.setEmployeeId(resultSet.getString("employee_id"));
                    record.setLastName(resultSet.getString("last_name"));
                    record.setFirstName(resultSet.getString("first_name"));
                    record.setDate(
                            resultSet.getDate("work_date")
                                    .toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

                    record.setLogIn(
                            resultSet.getTime("log_in") == null
                            ? ""
                            : resultSet.getTime("log_in").toString());

                    record.setLogOut(
                            resultSet.getTime("log_out") == null
                            ? ""
                            : resultSet.getTime("log_out").toString());

                    records.add(record);
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to load employee attendance records",
                    ex);
        }

        return records;
    }

    @Override
    public AttendanceRecord findByEmployeeIdAndDate(String employeeId, String date) {

        String sql = """
        SELECT
            a.employee_id,
            e.last_name,
            e.first_name,
            a.work_date,
            a.log_in,
            a.log_out
        FROM attendance a
        JOIN employee e
            ON a.employee_id = e.employee_id
        WHERE a.employee_id = ?
        AND a.work_date = ?
        """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);

            statement.setDate(
                    2,
                    toSqlDate(date));

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    AttendanceRecord record = new AttendanceRecord();

                    record.setEmployeeId(resultSet.getString("employee_id"));
                    record.setLastName(resultSet.getString("last_name"));
                    record.setFirstName(resultSet.getString("first_name"));

                    record.setDate(
                            resultSet.getDate("work_date")
                                    .toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

                    record.setLogIn(
                            resultSet.getTime("log_in") == null
                            ? ""
                            : resultSet.getTime("log_in").toString());

                    record.setLogOut(
                            resultSet.getTime("log_out") == null
                            ? ""
                            : resultSet.getTime("log_out").toString());

                    return record;
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to load attendance record",
                    ex);
        }

        return null;
    }
   

    @Override
    public void add(AttendanceRecord record) {

        String sql = """
        INSERT INTO attendance
        (
            employee_id,
            work_date,
            log_in,
            log_out
        )
        VALUES (?, ?, ?, ?)
        """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, record.getEmployeeId());

            statement.setDate(
                    2,
                    toSqlDate(record.getDate()));

            if (record.getLogIn() == null || record.getLogIn().isBlank()) {
                statement.setTime(3, null);
            } else {
                statement.setTime(
                        3,
                        java.sql.Time.valueOf(record.getLogIn() + ":00"));
            }

            if (record.getLogOut() == null || record.getLogOut().isBlank()) {
                statement.setTime(4, null);
            } else {
                statement.setTime(
                        4,
                        java.sql.Time.valueOf(record.getLogOut() + ":00"));
            }

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to add attendance record",
                    ex);
        }
    }

    @Override
    public void update(AttendanceRecord record) {

        String sql = """
        UPDATE attendance
        SET
            log_in = ?,
            log_out = ?
        WHERE employee_id = ?
        AND work_date = ?
        """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            if (record.getLogIn() == null || record.getLogIn().isBlank()) {
                statement.setTime(1, null);
            } else {
                statement.setTime(
                        1,
                        java.sql.Time.valueOf(record.getLogIn() + ":00"));
            }

            if (record.getLogOut() == null || record.getLogOut().isBlank()) {
                statement.setTime(2, null);
            } else {
                statement.setTime(
                        2,
                        java.sql.Time.valueOf(record.getLogOut() + ":00"));
            }

            statement.setString(3, record.getEmployeeId());

            statement.setDate(
                    4,
                    toSqlDate(record.getDate()));

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to update attendance record",
                    ex);
        }
    }

    @Override
    public void delete(String employeeId, String date) {

        String sql = """
        DELETE FROM attendance
        WHERE employee_id = ?
        AND work_date = ?
        """;

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employeeId);

            statement.setDate(
                    2,
                    toSqlDate(date));

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to delete attendance record",
                    ex);
        }
    }
private java.sql.Date toSqlDate(String date) {

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    LocalDate localDate =
            LocalDate.parse(date, formatter);

    return java.sql.Date.valueOf(localDate);
}

}



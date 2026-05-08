/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * CSV-backed implementation of AttendanceRepository.
 * Stores attendance rows in a local CSV file and provides basic CRUD operations.
 *
 * <p>Behavior highlights:</p>
 * <ul>
 *   <li>Auto-creates the attendance file with header if missing</li>
 *   <li>Supports multiple fallback search paths for the attendance file</li>
 *   <li>Reads all rows into memory for filter/update/delete workflows</li>
 * </ul>
 *
 * @author Rhynne Gracelle
 */

package repository;

import model.AttendanceRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CsvAttendanceRepository implements AttendanceRepository {

    private static final String HEADER = "Employee #,Last Name,First Name,Date,Log In,Log Out";
    private final Path filePath;

    /**
     * Creates repository instance and ensures backing CSV exists.
     */
    public CsvAttendanceRepository() {
        this.filePath = resolveAttendancePath();
        ensureFileExists();
    }

    private Path resolveAttendancePath() {
        String[] fileNames = {
                "MotorPH Attendance Record",
                "MotorPH Attendance Record.csv"
        };

        Path[] roots = new Path[] {
                Paths.get("data"),
                Paths.get("src", "data"),
                Paths.get(System.getProperty("user.dir"), "data"),
                Paths.get(System.getProperty("user.dir"), "src", "data")
        };

        for (Path root : roots) {
            for (String fileName : fileNames) {
                Path candidate = root.resolve(fileName);
                if (Files.exists(candidate)) {
                    return candidate.toAbsolutePath().normalize();
                }
            }
        }

        return Paths.get("data", "MotorPH Attendance Record.csv").toAbsolutePath().normalize();
    }

    private void ensureFileExists() {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(filePath)) {
                Files.writeString(filePath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            } else if (Files.size(filePath) == 0) {
                Files.writeString(filePath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare attendance CSV file.", e);
        }
    }

    @Override
    /**
     * Reads all attendance records from CSV.
     *
     * @return attendance list
     */
    public List<AttendanceRecord> findAll() {
        List<AttendanceRecord> records = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstRow = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstRow) {
                    firstRow = false;
                    if (line.startsWith("Employee #")) {
                        continue;
                    }
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                AttendanceRecord record = new AttendanceRecord();
                record.setEmployeeId(parts[0]);
                record.setLastName(parts[1]);
                record.setFirstName(parts[2]);
                record.setDate(parts[3]);
                record.setLogIn(parts[4]);
                record.setLogOut(parts[5]);

                records.add(record);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read attendance CSV.", e);
        }

        return records;
    }

    @Override
    /**
     * Finds attendance rows for a specific employee.
     *
     * @param employeeId employee ID filter
     * @return matching records
     */
    public List<AttendanceRecord> findByEmployeeId(String employeeId) {
        List<AttendanceRecord> result = new ArrayList<>();

        for (AttendanceRecord record : findAll()) {
            if (record.getEmployeeId().equalsIgnoreCase(employeeId == null ? "" : employeeId.trim())) {
                result.add(record);
            }
        }

        return result;
    }

    @Override
    /**
     * Finds a single attendance row by employee ID and date.
     *
     * @param employeeId employee ID key
     * @param date date key (MM/dd/yyyy)
     * @return matching row, or null
     */
    public AttendanceRecord findByEmployeeIdAndDate(String employeeId, String date) {
        for (AttendanceRecord record : findAll()) {
            if (record.getEmployeeId().equalsIgnoreCase(employeeId == null ? "" : employeeId.trim())
                    && record.getDate().equalsIgnoreCase(date == null ? "" : date.trim())) {
                return record;
            }
        }
        return null;
    }

    @Override
    /**
     * Adds a new attendance row.
     *
     * @param record row to insert
     */
    public void add(AttendanceRecord record) {
        List<AttendanceRecord> all = findAll();
        all.add(record);
        writeAll(all);
    }

    @Override
    /**
     * Updates an existing attendance row matched by employee ID and date.
     *
     * @param updatedRecord row with updated values
     * @throws IllegalArgumentException if target row does not exist
     */
    public void update(AttendanceRecord updatedRecord) {
        List<AttendanceRecord> all = findAll();

        for (int i = 0; i < all.size(); i++) {
            AttendanceRecord current = all.get(i);

            if (current.getEmployeeId().equalsIgnoreCase(updatedRecord.getEmployeeId())
                    && current.getDate().equalsIgnoreCase(updatedRecord.getDate())) {
                all.set(i, updatedRecord);
                writeAll(all);
                return;
            }
        }

        throw new IllegalArgumentException("Attendance record not found for update.");
    }
    
    @Override
    /**
     * Deletes an attendance row by employee ID and date.
     *
     * @param employeeId employee ID key
     * @param date date key (MM/dd/yyyy)
     */
    public void delete(String employeeId, String date) {
        List<AttendanceRecord> all = findAll();

        all.removeIf(record ->
                record.getEmployeeId().equalsIgnoreCase(employeeId == null ? "" : employeeId.trim())
                        && record.getDate().equalsIgnoreCase(date == null ? "" : date.trim())
        );

        writeAll(all);
    }

    private void writeAll(List<AttendanceRecord> records) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (AttendanceRecord record : records) {
                writer.write(toCsvLine(record));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write attendance CSV.", e);
        }
    }

    private String toCsvLine(AttendanceRecord record) {
        return safe(record.getEmployeeId()) + "," +
               safe(record.getLastName()) + "," +
               safe(record.getFirstName()) + "," +
               safe(record.getDate()) + "," +
               safe(record.getLogIn()) + "," +
               safe(record.getLogOut());
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.replace(",", " ").trim();
    }

    public interface AttendanceRepository {
        List<AttendanceRecord> findAll();
        List<AttendanceRecord> findByEmployeeId(String employeeId);
        AttendanceRecord findByEmployeeIdAndDate(String employeeId, String date);
        void add(AttendanceRecord record);
        void update(AttendanceRecord record);
        void delete(String employeeId, String date);
    }
    

}
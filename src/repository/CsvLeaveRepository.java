/**
 *
 * @author Leianna Cruz
 */

package repository;

import model.Leave;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvLeaveRepository implements LeaveRepository {

    private static final String FILE_PATH = "data/leaves.csv";
    private static final String HEADER = "leaveId,employeeId,leaveType,startDate,endDate,notes,status";

    public CsvLeaveRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            File file = new File(FILE_PATH);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println(HEADER);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare leave CSV file.", e);
        }
    }

    @Override
    public List<Leave> findAll() {
        List<Leave> leaves = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstRow = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstRow) {
                    firstRow = false;
                    if (line.toLowerCase().startsWith("leaveid,")) {
                        continue;
                    }
                }

                String[] parts = line.split(",", -1);

                // leaveId,employeeId,leaveType,startDate,endDate,notes,status
                if (parts.length < 7) continue;

                Leave leave = new Leave();
                leave.setLeaveId(parseIntSafe(parts[0]));
                leave.setEmployeeId(safe(parts[1]));
                leave.setLeaveType(safe(parts[2]));
                leave.setStartDate(safe(parts[3]));
                leave.setEndDate(safe(parts[4]));
                leave.setNotes(safe(parts[5]));
                leave.setStatus(safe(parts[6]));

                leaves.add(leave);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read leaves from CSV.", e);
        }

        return leaves;
    }

    @Override
    public List<Leave> findByEmployeeId(String employeeId) {
        List<Leave> result = new ArrayList<>();

        for (Leave leave : findAll()) {
            if (leave.getEmployeeId() != null &&
                    leave.getEmployeeId().equalsIgnoreCase(employeeId)) {
                result.add(leave);
            }
        }

        return result;
    }

    @Override
    public List<Leave> findByStatus(String status) {
        List<Leave> result = new ArrayList<>();

        for (Leave leave : findAll()) {
            if (leave.getStatus() != null &&
                    leave.getStatus().equalsIgnoreCase(status)) {
                result.add(leave);
            }
        }

        return result;
    }

    @Override
    public void add(Leave leave) {
        List<Leave> leaves = findAll();
        leaves.add(leave);
        writeAll(leaves);
    }

    @Override
    public void update(Leave updatedLeave) {
        List<Leave> leaves = findAll();

        for (int i = 0; i < leaves.size(); i++) {
            if (leaves.get(i).getLeaveId() == updatedLeave.getLeaveId()) {
                leaves.set(i, updatedLeave);
                writeAll(leaves);
                return;
            }
        }

        throw new IllegalArgumentException("Leave ID " + updatedLeave.getLeaveId() + " not found.");
    }

    @Override
    public void delete(int leaveId) {
        List<Leave> leaves = findAll();
        leaves.removeIf(leave -> leave.getLeaveId() == leaveId);
        writeAll(leaves);
    }

    @Override
    public Leave findById(int leaveId) {
        for (Leave leave : findAll()) {
            if (leave.getLeaveId() == leaveId) {
                return leave;
            }
        }
        return null;
    }

    @Override
    public int getNextLeaveId() {
        int max = 0;

        for (Leave leave : findAll()) {
            if (leave.getLeaveId() > max) {
                max = leave.getLeaveId();
            }
        }

        return max + 1;
    }

    private void writeAll(List<Leave> leaves) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            pw.println(HEADER);
            for (Leave leave : leaves) {
                pw.println(toCsvLine(leave));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write leaves to CSV.", e);
        }
    }

    private String toCsvLine(Leave leave) {
        return escape(String.valueOf(leave.getLeaveId())) + "," +
               escape(leave.getEmployeeId()) + "," +
               escape(leave.getLeaveType()) + "," +
               escape(leave.getStartDate()) + "," +
               escape(leave.getEndDate()) + "," +
               escape(leave.getNotes()) + "," +
               escape(leave.getStatus());
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace(",", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
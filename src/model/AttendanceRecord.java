/**
 *
 * @author Leianna Cruz
 */

package model;

public class AttendanceRecord implements TimeTrackable {

    private String employeeId;
    private String lastName;
    private String firstName;
    private String date;     // MM/dd/yyyy
    private String logIn;    // H:mm
    private String logOut;   // H:mm

    public AttendanceRecord() {
    }

    public AttendanceRecord(String employeeId, String lastName, String firstName,
                            String date, String logIn, String logOut) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.logIn = logIn;
        this.logOut = logOut;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId == null ? "" : employeeId.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? "" : lastName.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? "" : firstName.trim();
    }

    @Override
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date == null ? "" : date.trim();
    }

    @Override
    public String getLogIn() {
        return logIn;
    }

    public void setLogIn(String logIn) {
        this.logIn = logIn == null ? "" : logIn.trim();
    }

    @Override
    public String getLogOut() {
        return logOut;
    }

    public void setLogOut(String logOut) {
        this.logOut = logOut == null ? "" : logOut.trim();
    }
}
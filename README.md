# MO-IT110 Object-Oriented Programming | Group 19

## MotorPH Payroll System
A Java Swing desktop application for managing HR and payroll operations at MotorPH.
Developed as the course project for MO-IT110 Object-Oriented Programming, with the guidance of Sir Glenn Baluyot.

---

## Project Overview
The MotorPH Payroll System is a Java desktop application for managing core HR and payroll operations. The system enforces role-based access control so that each employee only sees and performs actions permitted by their department role.

## Key Features
- Secure login with role-based session management
- Employee record management (add, view, update, delete)
- Attendance tracking with time-in / time-out logging
- Leave request submission and HR approval workflow
- Payroll and payslip processing
- Permission-restricted UI; buttons and views adapt per role

## Tools & Technologies
- **Language:** Java
- **GUI Framework:** Java Swing
- **IDE:** Apache NetBeans
- **Build Tool:** Apache Ant (build.xml)
- **Data Storage:** CSV flat files
- **Version Control:** GitHub

## Folder Structure
```
MotorPH_OOP/
├── data/
│   ├── leaves.csv
│   ├── MotorPH Attendance Record
│   └── MotorPH Employee Record.csv
├── lib/
├── nbproject/
│   ├── private/
│   ├── build-impl.xml
│   ├── genfiles.properties
│   ├── project.properties
│   └── project.xml
├── src/
│   ├── asset/
│   ├── gui/
│   │   ├── AttendanceFormPanel.java
│   │   ├── AttendancePanel.java
│   │   ├── DashboardPanel.java
│   │   ├── EmployeeDetailsPanel.java
│   │   ├── EmployeeFormPanel.java
│   │   ├── EmployeeLeavesPanel.java
│   │   ├── EmployeeManagementLauncher.java
│   │   ├── EmployeeManagementPanel.java
│   │   ├── LeaveFormPanel.java
│   │   ├── LoginDialog.java
│   │   ├── MainDashboardFrame.java
│   │   ├── MainDashboardLauncher.java
│   │   ├── PayrollDetailsPanel.java
│   │   ├── PayrollPanel.java
│   │   ├── ResetCredentials.java
│   ├── model/
│   │   ├── AccountingDepartment.java
│   │   ├── AttendanceRecord.java
│   │   ├── Employee.java
│   │   ├── EmployeeFactory.java
│   │   ├── ExecutiveDepartment.java
│   │   ├── HRDepartment.java
│   │   ├── ITDepartment.java
│   │   ├── Leave.java
│   │   ├── LeaveRequest.java
│   │   ├── Payable.java
│   │   ├── PayrollDepartment.java
│   │   ├── PersonRecord.java
│   │   ├── ProbationaryEmployee.java
│   │   ├── RegularEmployee.java
│   │   ├── SalesDepartment.java
│   │   └── TimeTrackable.java
│   ├── RBAC/
│   │   ├── Permission.java
│   │   ├── RBACSetup.java
│   │   └── Role.java
│   ├── repository/
│   │   ├── AttendanceRepository.java
│   │   ├── BulkAccountGenerator.java
│   │   ├── CsvAttendanceRepository.java
│   │   ├── CsvEmployeeRepository.java
│   │   ├── CsvEmployeeTimeRecord.java
│   │   ├── CsvLeaveRepository.java
│   │   ├── EmployeeRepository.java
│   │   └── LeaveRepository.java
│   └── Service/
│       ├── AttendanceService.java
│       ├── AuthenticationService.java
│       ├── AuthorizationService.java
│       ├── EmployeeRequest.java
│       ├── EmployeeService.java
│       ├── EmployeeValidator.java
│       ├── LeaveRequestValidator.java
│       ├── LeaveService.java
│       ├── PayrollComputationService.java
│       ├── SessionManager.java
│       └── auth/                    # Service authentication utilities
│           ├── AccountService.java
│           └── PasswordManager.java
├── build.xml
├── manifest.mf
└── README.md
```

## System Features Description:

### Login
**Files:** LoginDialog.java, AuthenticationService.java, AccountService.java, PasswordManager.java, ResetCredentials.java

Presents a full-screen login form rendered over a custom background image. The dialog scales responsively to screen resolution. On submit, AuthenticationService validates credentials against the employee CSV. A successful login stores the authenticated Employee in SessionManager and launches the main dashboard. A "Forgot Password?" link exposes a credential reset workflow.

### Main Dashboard
**Files:** MainDashboardFrame.java, MainDashboardLauncher.java, DashboardPanel.java

A JFrame with a black sidebar and a CardLayout-powered content area. Sidebar navigation links switch between Dashboard, Employees, Payroll, Leave, and Attendance without opening new windows. The current user's name and position are displayed in the sidebar header. Navigation options are filtered at runtime based on the user's RBAC permissions.

### Employee Management
**Files:** EmployeeManagementPanel.java, AddRecordDialog.java, UpdateDialog.java, ViewRecordDialog.java, EmployeeManagementLauncher.java

Displays all employees in a searchable JTable loaded from the CSV repository. Action buttons (Add, Update, Delete, View) are shown or hidden based on RBAC permissions; only HR may add, edit, or delete records. Add and Update operations open modal dialogs covering all employee fields, including salary, allowances, position, and role assignment.

### Attendance
**Files:** AttendancePanel.java, AttendanceFormPanel.java

Uses CardLayout to switch between a list view and a form view. The list is filterable by Employee ID. Employees may Time In / Time Out; HR may edit and delete records. Each action button is gated by its corresponding permission (TIME_IN, TIME_OUT, EDIT_ATTENDANCE, DELETE_ATTENDANCE). Records are persisted via CsvAttendanceRepository.

### Leave Management
**Files:** EmployeeLeavesPanel.java, LeaveManagementFrame.java, LeaveFormPanel.java, AddLeaveDialog.java, LeaveRequestDialog.java, HrLeaveRequestsDialog.java, EmployeeLeaveTableModel.java

Employees submit leave requests specifying type, date range, and reason. LeaveService validates each request before saving to leaves.csv. HR users access a dedicated dialog (HrLeaveRequestsDialog) listing all pending requests and may approve or reject them. Employees can review their own leave history through the panel.

### Payroll
**Files:** PayrollPanel.java, PayslipSplitDialog.java

RBAC restricts access to the PROCESS_PAYROLL and GENERATE_PAYSLIP permissions, which are assigned exclusively to the Payroll role.

### Role-Based Access Control (RBAC)
The system implements a custom RBAC layer in the RBAC package. Each employee record includes a Role column. After login, the role is resolved and its permission set is used throughout the GUI to show, hide, or disable controls.

#### RBAC Classes
| Class       | Description                                                      |
|-------------|------------------------------------------------------------------|
| Permission  | Enum of all 23 granular permissions across Employee, Payroll, Leave, Attendance, and System categories |
| Role        | Holds a role name and an immutable Set<Permission>; exposes hasPermission(Permission) |
| RBACSetup   | Static factory returning a Map<String, Role> with all six preconfigured roles |

#### Class Reference

##### GUI Package
| Class                     | Type                | Description                                  |
|---------------------------|---------------------|----------------------------------------------|
| LoginDialog               | JDialog             | Full-screen login form with background image and responsive layout |
| MainDashboardFrame        | JFrame              | Root application window with sidebar and CardLayout content area |
| DashboardPanel            | JPanel              | Home screen panel                            |
| EmployeeManagementPanel   | JPanel              | Employee table with RBAC-filtered CRUD toolbar |
| AttendancePanel           | JPanel              | CardLayout panel switching between attendance list and form |
| AttendanceFormPanel       | JPanel              | Form for adding and editing attendance entries |
| EmployeeLeavesPanel       | JPanel              | Employee-facing leave history list and submission form |
| LeaveFormPanel            | JPanel              | Leave request form (type, dates, reason)      |
| HrLeaveRequestsDialog     | JDialog             | HR view of pending requests with approve/reject actions |
| PayrollPanel              | JPanel              | Payroll stub screen                          |
| PayslipSplitDialog        | JDialog             | Payslip viewer scaffold                      |
| AddRecordDialog           | JDialog             | Form dialog for creating a new employee record |
| UpdateDialog              | JDialog             | Form dialog for editing an existing employee record |
| ViewRecordDialog          | JDialog             | Read-only employee detail view               |
| ResetCredentials          | Dialog              | Credential reset workflow for IT role and forgotten passwords |
| EmployeeLeaveTableModel   | AbstractTableModel  | Custom table model for the leave management table |

##### Service Layer
| Class                    | Description                                            |
|--------------------------|--------------------------------------------------------|
| AuthenticationService    | Validates login credentials against the employee repository |
| AuthorizationService     | Permission-checking helpers used by GUI panels         |
| SessionManager           | Static singleton storing the currently logged-in Employee |
| LeaveService             | Validates and delegates leave CRUD to LeaveRepository  |
| AttendanceService        | Time-in/time-out logic and attendance record retrieval |
| AccountService (`service.auth`) | Handles credential storage and account lookup by employee ID |
| PasswordManager (`service.auth`) | Hashing, verification, and credential reset support |
| EmployeeLeaveUiService   | Interface abstracting leave UI operations              |
| InMemoryEmployeeLeaveUiService | In-memory implementation of EmployeeLeaveUiService |

## OOP Concepts Applied
| Concept        | Implementation                                                                 |
|----------------|-------------------------------------------------------------------------------|
| Encapsulation  | Role returns an unmodifiable permission set; SessionManager controls write access to the current user; all model fields are private with public getters |
| Abstraction    | EmployeeLeaveUiService interface abstracts leave UI operations; EmployeeRepository interface abstracts all data access, with a CSV-backed concrete implementation |
| Inheritance    | To be updated                                                                  |
| Polymorphism   | To be updated                                                                  |

## Setup & How to Run

### Prerequisites
- Java JDK 11 or later
- Apache NetBeans IDE (recommended) or any IDE with Ant support

### Steps
1. Download or clone the repository:

   ```sh
   git clone https://github.com/your-repo/MO-IT110_Group19.git
   ```
   Or click Code → Download ZIP and extract.

2. Open NetBeans → File → Open Project → select the MO-IT110_Group19 folder.

3. NetBeans will detect the Ant build (build.xml) automatically.

4. Press Shift + F6 (or Run → Run Project) to build and launch.

5. Log in using an Employee # from data/MotorPH Employee Record.csv as the username.

### Login Credentials
Login credentials are created by the employee's number being the username. Password is their username + the first letter of their first name capitalized.

**Employee:**
- User: 10001
- Password: 10001M

**HR/Admin:**
- User: 10005
- Password: 10005E

### Notes
- All data files must remain in the data/ directory relative to the project root.
- Default passwords are managed by PasswordManager. Use the Forgot Password? link on the login screen to reset credentials.
- The Payroll module is currently scaffolded and will be completed in a bit.

## Group Members

### Developers
- Trisha Gayle Dela Cruz
- Rhynne Gracelle Pontanilla
- Ma. Dennise Berja
- Leianna Khay Cruz

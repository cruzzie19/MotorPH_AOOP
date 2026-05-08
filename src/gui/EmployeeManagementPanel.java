/**
 *
 * @author Leianna Cruz
 */

package gui;

import RBAC.Permission;
import RBAC.Role;
import model.Employee;
import repository.EmployeeRepository;
import service.AuthorizationService;
import service.EmployeeRequest;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EmployeeManagementPanel extends JPanel {

    private static final String CARD_LIST = "LIST";
    private static final String CARD_DETAILS = "DETAILS";

    private static final String[] TABLE_COLUMNS = {
            "Employee No.", "Name", "Status", "Position", "Immediate Supervisor", "Role"
    };

    private static final Color BORDER = new Color(220, 220, 220);

    private final EmployeeService employeeService;
    private final Path employeeCsvPath;
    private final Employee currentUser;

    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField searchField;
    private final JLabel infoLabel;

    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCardPanel = new JPanel(contentCardLayout);

    private EmployeeDetailsPanel detailsPanel;

    private JButton searchBtn;
    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton viewBtn;
    private JButton refreshBtn;

    public EmployeeManagementPanel(EmployeeRepository repo, Path employeeCsvPath, Employee currentUser) {
        this.employeeService = new EmployeeService(repo);
        this.employeeCsvPath = employeeCsvPath;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        this.model = createTableModel();
        this.table = createEmployeeTable();
        this.searchField = createSearchField();
        this.infoLabel = createInfoLabel();

        add(createMainLayout(), BorderLayout.CENTER);

        if (canViewEmployeeList()) {
            loadTable();
            showListCard();
        } else {
            showAccessLimitedState();
            showListCard();
        }
    }

    private JPanel createMainLayout() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));
        outer.add(createContentArea(), BorderLayout.CENTER);
        return outer;
    }

    private JPanel createContentArea() {
        contentCardPanel.setOpaque(false);

        contentCardPanel.add(createListCard(), CARD_LIST);

        detailsPanel = new EmployeeDetailsPanel(this::showListCard);
        detailsPanel.setActionListener(new EmployeeDetailsPanel.EmployeeDetailsActionListener() {
            @Override
            public void onCreate(EmployeeDetailsPanel panel) {
                handleCreateFromDetails(panel);
            }

            @Override
            public void onUpdate(EmployeeDetailsPanel panel) {
                handleUpdateFromDetails(panel);
            }
        });

        contentCardPanel.add(detailsPanel, CARD_DETAILS);
        return contentCardPanel;
    }

    private JPanel createListCard() {
        JPanel listCard = new JPanel(new BorderLayout(0, 16));
        listCard.setOpaque(false);
        listCard.add(createActionBar(), BorderLayout.NORTH);
        listCard.add(createTableCard(), BorderLayout.CENTER);
        return listCard;
    }

    private JPanel createActionBar() {
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        searchBtn = createSearchButton("Search");
        addBtn = createActionButton("Add");
        updateBtn = createActionButton("Update");
        deleteBtn = createActionButton("Delete");
        viewBtn = createActionButton("View");
        refreshBtn = createRefreshButton("Refresh");

        leftActions.add(searchField);
        leftActions.add(searchBtn);

        rightActions.add(addBtn);
        rightActions.add(updateBtn);
        rightActions.add(deleteBtn);
        rightActions.add(viewBtn);
        rightActions.add(refreshBtn);

        actions.add(leftActions, BorderLayout.WEST);
        actions.add(rightActions, BorderLayout.EAST);

        bindActionEvents();
        applyPermissions();
        updateActionButtonStates();

        return actions;
    }

    private JPanel createTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(new Color(245, 245, 245));
        tableScrollPane.setBackground(new Color(245, 245, 245));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 14, 10, 14));
        footer.add(infoLabel, BorderLayout.WEST);

        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        tableCard.add(footer, BorderLayout.SOUTH);

        return tableCard;
    }

    private JTextField createSearchField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(210, 44));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 12, 0, 12)
        ));
        return field;
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(130, 130, 130));
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 44));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSearchButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 44));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createRefreshButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 44));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createEmployeeTable() {
        JTable employeeTable = new JTable(model);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(42);
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setFillsViewportHeight(true);

        employeeTable.setBackground(new Color(245, 245, 245));
        employeeTable.setForeground(new Color(35, 35, 35));
        employeeTable.setSelectionBackground(new Color(200, 212, 232));
        employeeTable.setSelectionForeground(new Color(25, 25, 25));

        employeeTable.setGridColor(new Color(235, 235, 235));
        employeeTable.setShowVerticalLines(false);
        employeeTable.setShowHorizontalLines(true);
        employeeTable.setIntercellSpacing(new Dimension(0, 1));
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = employeeTable.getTableHeader();
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                label.setOpaque(true);
                label.setBackground(Color.BLACK);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });

        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focus, int row, int column) {

                super.getTableCellRendererComponent(table, value, selected, focus, row, column);

                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setForeground(new Color(35, 35, 35));

                if (selected) {
                    setBackground(new Color(200, 212, 232));
                } else {
                    setBackground(row % 2 == 0
                            ? new Color(245, 245, 245)
                            : new Color(239, 239, 239));
                }

                return this;
            }
        });

        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        employeeTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        employeeTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(220);
        employeeTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        employeeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtonStates();
            }
        });

        return employeeTable;
    }

    private void bindActionEvents() {
        searchBtn.addActionListener(e -> filterTable());

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadTable();
            showListCard();
        });

        addBtn.addActionListener(e -> handleAddEmployee());
        updateBtn.addActionListener(e -> handleUpdateEmployee());
        deleteBtn.addActionListener(e -> handleDeleteEmployee());
        viewBtn.addActionListener(e -> handleViewEmployee());

        searchField.addActionListener(e -> filterTable());
    }

    private void applyPermissions() {
        boolean canViewList = canViewEmployeeList();
        boolean canAdd = canAddEmployee();
        boolean canEdit = canEditEmployee();
        boolean canDelete = canDeleteEmployee();
        boolean canViewDetails = canViewEmployeeDetails();

        searchField.setEnabled(canViewList);
        searchBtn.setVisible(canViewList);

        addBtn.setVisible(canAdd);
        updateBtn.setVisible(canEdit);
        deleteBtn.setVisible(canDelete);
        viewBtn.setVisible(canViewDetails);
        refreshBtn.setVisible(canViewList);

        table.setEnabled(canViewList);
    }

    private void updateActionButtonStates() {
        boolean rowSelected = table.getSelectedRow() >= 0;

        if (viewBtn != null) {
            viewBtn.setEnabled(rowSelected && canViewEmployeeDetails());
        }

        if (updateBtn != null) {
            updateBtn.setEnabled(rowSelected && canEditEmployee());
        }

        if (deleteBtn != null) {
            deleteBtn.setEnabled(rowSelected && canDeleteEmployee());
        }
    }

    private boolean canViewEmployeeList() {
        return hasPermission(Permission.VIEW_EMPLOYEE_LIST) || hasPermission(Permission.VIEW_EMPLOYEE);
    }

    private boolean canViewEmployeeDetails() {
        return hasPermission(Permission.VIEW_EMPLOYEE_BASIC_DETAILS) || hasPermission(Permission.VIEW_EMPLOYEE);
    }

    private boolean canViewPersonalDetails() {
        return hasPermission(Permission.VIEW_EMPLOYEE_PERSONAL_DETAILS);
    }

    private boolean canViewGovernmentIds() {
        return hasPermission(Permission.VIEW_EMPLOYEE_GOVERNMENT_IDS);
    }

    private boolean canViewCompensation() {
        return hasPermission(Permission.VIEW_EMPLOYEE_COMPENSATION);
    }

    private boolean canAddEmployee() {
        return hasPermission(Permission.ADD_EMPLOYEE);
    }

    private boolean canEditEmployee() {
        return hasPermission(Permission.EDIT_EMPLOYEE);
    }

    private boolean canDeleteEmployee() {
        return hasPermission(Permission.DELETE_EMPLOYEE);
    }

    private boolean hasPermission(Permission permission) {
        return AuthorizationService.hasPermission(currentUser, permission);
    }

    private void showAccessLimitedState() {
        model.setRowCount(0);
        infoLabel.setText("You do not have permission to view the employee directory.");
    }

    private void showListCard() {
        contentCardLayout.show(contentCardPanel, CARD_LIST);
        revalidate();
        repaint();
    }

    private void showDetailsCard() {
        contentCardLayout.show(contentCardPanel, CARD_DETAILS);
        revalidate();
        repaint();
    }

    private void showDetailsCard(
            Employee employee,
            EmployeeDetailsPanel.Mode mode,
            boolean showPersonalDetails,
            boolean showGovernmentIds,
            boolean showCompensation
    ) {
        detailsPanel.displayEmployee(employee, showPersonalDetails, showGovernmentIds, showCompensation);
        detailsPanel.setMode(mode);
        contentCardLayout.show(contentCardPanel, CARD_DETAILS);
        revalidate();
        repaint();
    }

    private void handleAddEmployee() {
        if (!canAddEmployee()) {
            showAccessDenied();
            return;
        }

        detailsPanel.showCreateMode(
                () -> {
                    loadTable();
                    showListCard();
                },
                this::showListCard
        );

        showDetailsCard();
    }

    private void handleUpdateEmployee() {
        if (!canEditEmployee()) {
            showAccessDenied();
            return;
        }

        Employee employee = getSelectedEmployee();
        if (employee == null) {
            showWarning("Please select an employee to update.");
            return;
        }

        showDetailsCard(
                employee,
                EmployeeDetailsPanel.Mode.UPDATE,
                canViewPersonalDetails(),
                canViewGovernmentIds(),
                canViewCompensation()
        );
    }

    private void handleViewEmployee() {
        if (!canViewEmployeeDetails()) {
            showAccessDenied();
            return;
        }

        Employee employee = getSelectedEmployee();
        if (employee == null) {
            showWarning("Please select an employee to view.");
            return;
        }

        showDetailsCard(
                employee,
                EmployeeDetailsPanel.Mode.VIEW,
                canViewPersonalDetails(),
                canViewGovernmentIds(),
                canViewCompensation()
        );
    }

    private void handleDeleteEmployee() {
        if (!canDeleteEmployee()) {
            showAccessDenied();
            return;
        }

        Employee employee = getSelectedEmployee();
        if (employee == null) {
            showWarning("Please select an employee to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete employee " + employee.getId() + " - " + employee.getLastName() + ", " + employee.getFirstName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            employeeService.deleteEmployee(employee.getId());
            showInfo("Employee deleted successfully.");
            loadTable();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCreateFromDetails(EmployeeDetailsPanel panel) {
        try {
            EmployeeRequest request = buildRequestFromPanel(panel);
            employeeService.addEmployee(request);

            showInfo("Employee added successfully.");
            loadTable();
            showListCard();

        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleUpdateFromDetails(EmployeeDetailsPanel panel) {
        try {
            Employee original = detailsPanel.getCurrentEmployee();

            if (original == null) {
                throw new IllegalArgumentException("No employee is selected for update.");
            }

            EmployeeRequest request = buildRequestFromPanel(panel);
            employeeService.updateEmployee(original.getId(), request);

            showInfo("Employee updated successfully.");
            loadTable();
            showListCard();

        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private EmployeeRequest buildRequestFromPanel(EmployeeDetailsPanel panel) {
        EmployeeRequest request = new EmployeeRequest();

        request.setId(requireValue(panel.getEmployeeIdInput(), "Employee No."));
        request.setFirstName(requireValue(panel.getFirstNameInput(), "First Name"));
        request.setLastName(requireValue(panel.getLastNameInput(), "Last Name"));
        request.setBirthDate(parseBirthDate(panel.getBirthDateInput()));

        request.setDepartment("");
        request.setAddress(requireValue(panel.getAddressInput(), "Address"));
        request.setPhone(requireValue(panel.getPhoneInput(), "Phone Number"));
        request.setSssNumber(requireValue(panel.getSssInput(), "SSS No."));
        request.setPhilHealthNumber(requireValue(panel.getPhilHealthInput(), "PhilHealth No."));
        request.setTinNumber(requireValue(panel.getTinInput(), "TIN"));
        request.setPagIbigNumber(requireValue(panel.getPagIbigInput(), "Pag-IBIG No."));

        request.setStatus(requireValue(panel.getStatusInput(), "Status"));
        request.setPosition(requireValue(panel.getPositionInput(), "Position"));
        request.setSupervisor(requireValue(panel.getSupervisorInput(), "Immediate Supervisor"));
        request.setRoleName(requireValue(panel.getRoleInput(), "Role"));

        request.setBasicSalary(parseMoney(panel.getBasicSalaryInput(), "Basic Salary"));
        request.setRiceSubsidy(parseMoney(panel.getRiceSubsidyInput(), "Rice Subsidy"));
        request.setPhoneAllowance(parseMoney(panel.getPhoneAllowanceInput(), "Phone Allowance"));
        request.setClothingAllowance(parseMoney(panel.getClothingAllowanceInput(), "Clothing Allowance"));
        request.setGrossSemiMonthlyRate(parseMoney(panel.getGrossSemiMonthlyInput(), "Gross Semi-Monthly Rate"));
        request.setHourlyRate(parseMoney(panel.getHourlyRateInput(), "Hourly Rate"));

        return request;
    }

    private String requireValue(String value, String fieldName) {
        String cleaned = safe(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return cleaned;
    }

    private LocalDate parseBirthDate(String input) {
        String cleaned = requireValue(input, "Birth date");

        try {
            return LocalDate.parse(
                    cleaned,
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            );
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Birth date must use format: Month dd, yyyy (e.g. January 05, 2000)."
            );
        }
    }

    private BigDecimal parseMoney(String input, String fieldName) {
        String cleaned = safe(input).replace(",", "");

        if (cleaned.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    private String getRoleName(Employee employee) {
        if (employee == null) {
            return "";
        }

        Role role = employee.getRole();
        return role == null ? "" : safe(role.getName());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void addEmployeeRow(Employee emp) {
        model.addRow(new Object[]{
                safe(emp.getId()),
                (safe(emp.getLastName()) + ", " + safe(emp.getFirstName())).trim(),
                safe(emp.getStatus()),
                safe(emp.getPosition()),
                safe(emp.getSupervisor()),
                getRoleName(emp)
        });
    }

    private void populateTable(List<Employee> employees) {
        model.setRowCount(0);

        for (Employee emp : employees) {
            addEmployeeRow(emp);
        }

        updateActionButtonStates();
    }

    private void loadTable() {
        if (!canViewEmployeeList()) {
            showAccessLimitedState();
            return;
        }

        List<Employee> employees = employeeService.getAllEmployees();
        populateTable(employees);

        if (employees.isEmpty()) {
            infoLabel.setText("No employee records found. Please check: " + employeeCsvPath.toAbsolutePath());
        } else {
            infoLabel.setText(employees.size() + " employee record(s) loaded.");
        }
    }

    private void filterTable() {
        if (!canViewEmployeeList()) {
            showAccessDenied();
            return;
        }

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        List<Employee> employees = employeeService.searchEmployees(keyword);

        populateTable(employees);
        infoLabel.setText(employees.size() + " employee record(s) found.");
        showListCard();
    }

    private Employee getSelectedEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        String employeeId = String.valueOf(model.getValueAt(selectedRow, 0));
        return employeeService.findById(employeeId);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showAccessDenied() {
        JOptionPane.showMessageDialog(
                this,
                "You do not have permission for this action.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
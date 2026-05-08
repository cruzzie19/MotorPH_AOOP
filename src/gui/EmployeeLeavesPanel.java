/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Rhynne Gracelle
 */

package gui;

import model.Employee;
import model.Leave;
import repository.EmployeeRepository;
import service.LeaveService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class EmployeeLeavesPanel extends JPanel {

    private static final String LIST_CARD = "LIST";
    private static final String FORM_CARD = "FORM";

    private static final Color PAGE_BG = new Color(242, 242, 242);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;

    private static final Color TABLE_BORDER = new Color(220, 220, 220);
    private static final Color TABLE_GRID = new Color(232, 232, 232);
    private static final Color TABLE_ROW_EVEN = new Color(245, 245, 245);
    private static final Color TABLE_ROW_ODD = new Color(239, 239, 239);
    private static final Color TABLE_SELECTION = new Color(220, 228, 240);

    private static final Color TEXT_DARK = new Color(35, 35, 35);
    private static final Color TEXT_MUTED = new Color(130, 130, 130);

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;
    private final String currentEmployeeId;
    private final Employee currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private JTable table;
    private DefaultTableModel model;
    private LeaveFormPanel formPanel;
    private JLabel emptyStateLabel;
    private JScrollPane tableScrollPane;
    private JLabel infoLabel;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnMyRecords;
    private JButton btnViewAll;

    private Leave workingLeave;
    private boolean editMode = false;
    private boolean hrReviewMode = false;
    private boolean showAllRecords = false; // false = My Records, true = View All

    public EmployeeLeavesPanel(LeaveService leaveService,
                               EmployeeRepository employeeRepository,
                               String currentEmployeeId,
                               String currentEmployeeName,
                               String currentDepartment,
                               String currentPosition) {
        this.leaveService = leaveService;
        this.employeeRepository = employeeRepository;
        this.currentEmployeeId = currentEmployeeId;
        this.currentUser = SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        contentPanel.setOpaque(false);
        contentPanel.add(buildListPage(), LIST_CARD);
        contentPanel.add(buildFormPage(), FORM_CARD);

        add(contentPanel, BorderLayout.CENTER);

        loadLeaveRequests();
        refreshActionButtons();
        showListPage();
    }

    private JPanel buildListPage() {
        JPanel outer = new JPanel(new BorderLayout(0, 16));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        btnAdd = createBlackButton("Add");
        btnUpdate = createBlackButton("Update");
        btnDelete = createBlackButton("Delete");
        btnRefresh = createWhiteButton("Refresh");
        btnMyRecords = createBlackButton("My Records");
        btnViewAll = createBlackButton("View All");

        btnAdd.addActionListener(e -> openAddForm());
        btnUpdate.addActionListener(e -> openUpdateForm());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadLeaveRequests());

        btnMyRecords.addActionListener(e -> {
            if (!isHrUser()) {
                return;
            }
            showAllRecords = false;
            loadLeaveRequests();
            refreshActionButtons();
        });

        btnViewAll.addActionListener(e -> {
            if (!isHrUser()) {
                return;
            }
            showAllRecords = true;
            loadLeaveRequests();
            refreshActionButtons();
        });

        if (isHrUser()) {
            leftActions.add(btnMyRecords);
            leftActions.add(btnViewAll);
        }

        rightActions.add(btnAdd);
        rightActions.add(btnUpdate);
        rightActions.add(btnDelete);
        rightActions.add(btnRefresh);

        actions.add(leftActions, BorderLayout.WEST);
        actions.add(rightActions, BorderLayout.EAST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TABLE_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        model = new DefaultTableModel(
                new Object[]{"Leave ID", "Employee ID", "Leave Type", "Start Date", "End Date", "Notes", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable();

        // Hide Leave ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Visible columns
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(320);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(TABLE_ROW_EVEN);
        tableScrollPane.setBackground(WHITE);

        emptyStateLabel = new JLabel("No leave requests found.", SwingConstants.CENTER);
        emptyStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emptyStateLabel.setForeground(TEXT_MUTED);
        emptyStateLabel.setOpaque(true);
        emptyStateLabel.setBackground(WHITE);
        emptyStateLabel.setBorder(new EmptyBorder(30, 20, 30, 20));

        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_MUTED);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 14, 10, 14));
        footer.add(infoLabel, BorderLayout.WEST);

        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        tableCard.add(footer, BorderLayout.SOUTH);

        outer.add(actions, BorderLayout.NORTH);
        outer.add(tableCard, BorderLayout.CENTER);

        return outer;
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT_DARK);
        table.setBackground(TABLE_ROW_EVEN);
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        table.setGridColor(TABLE_GRID);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BLACK);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                label.setOpaque(true);
                label.setBackground(BLACK);
                label.setForeground(WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean focus, int row, int column) {

                super.getTableCellRendererComponent(table, value, selected, focus, row, column);

                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setForeground(TEXT_DARK);

                if (selected) {
                    setBackground(TABLE_SELECTION);
                } else {
                    setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
                }

                return this;
            }
        });
    }

    private JPanel buildFormPage() {
        formPanel = new LeaveFormPanel();
        formPanel.addBackListener(e -> showListPage());
        formPanel.addSubmitListener(e -> submitForm());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void openAddForm() {
        editMode = false;
        hrReviewMode = false;

        workingLeave = new Leave();

        formPanel.clearForm();

        if (isHrUser() && showAllRecords) {
            workingLeave.setEmployeeId("");
            formPanel.setEmployeeFieldEditable(true);
            formPanel.setEmployeeIdValue("");
            formPanel.setManagerEntryMode(true);
        } else {
            workingLeave.setEmployeeId(currentEmployeeId);
            formPanel.setEmployeeFieldEditable(false);
            formPanel.setEmployeeIdValue(currentEmployeeId);
            formPanel.setManagerEntryMode(false);
        }

        workingLeave.setStatus("Pending");

        formPanel.setFormMode(false);
        formPanel.setHrReviewMode(false);
        formPanel.setLeaveData(workingLeave);

        cardLayout.show(contentPanel, FORM_CARD);
    }

    private void openUpdateForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
            return;
        }

        int leaveId = getSelectedLeaveId(row);
        if (leaveId < 0) {
            JOptionPane.showMessageDialog(this, "Selected leave request was not found.");
            return;
        }

        Leave selected = findSelectedLeave(leaveId);

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selected leave request was not found.");
            return;
        }

        boolean ownRecord = currentEmployeeId.equals(selected.getEmployeeId());

        if (isHrUser() && showAllRecords && !ownRecord) {
            editMode = false;
            hrReviewMode = true;
            workingLeave = selected;

            formPanel.setEmployeeFieldEditable(false);
            formPanel.setEmployeeIdValue(selected.getEmployeeId());
            formPanel.setManagerEntryMode(false);
            formPanel.setFormMode(false);
            formPanel.setHrReviewMode(true);
            formPanel.setLeaveData(workingLeave);

            cardLayout.show(contentPanel, FORM_CARD);
            return;
        }

        if (!ownRecord) {
            JOptionPane.showMessageDialog(
                    this,
                    "You can only update your own leave request here.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!"Pending".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending leave requests can be updated.");
            return;
        }

        editMode = true;
        hrReviewMode = false;
        workingLeave = selected;
        workingLeave.setStatus("Pending");

        formPanel.setEmployeeFieldEditable(false);
        formPanel.setEmployeeIdValue(selected.getEmployeeId());
        formPanel.setManagerEntryMode(false);
        formPanel.setFormMode(true);
        formPanel.setHrReviewMode(false);
        formPanel.setLeaveData(workingLeave);

        cardLayout.show(contentPanel, FORM_CARD);
    }

    private void submitForm() {
        if (workingLeave == null) {
            return;
        }

        formPanel.fillLeave(workingLeave);

        try {
            boolean ownRecord = currentEmployeeId.equals(workingLeave.getEmployeeId());

            if (hrReviewMode && isHrUser() && !ownRecord) {
                leaveService.updateLeave(workingLeave);
                JOptionPane.showMessageDialog(this, "Leave record updated successfully.");
            } else if (editMode) {
                workingLeave.setEmployeeId(currentEmployeeId);
                workingLeave.setStatus("Pending");
                leaveService.updateOwnPendingLeave(workingLeave, currentEmployeeId);
                JOptionPane.showMessageDialog(this, "Leave request updated successfully.");
            } else {
                if (isHrUser() && showAllRecords) {
                    if (!validateManagerEnteredEmployeeId(workingLeave.getEmployeeId())) {
                        return;
                    }
                } else {
                    workingLeave.setEmployeeId(currentEmployeeId);
                }

                workingLeave.setStatus("Pending");
                leaveService.requestLeave(workingLeave);
                JOptionPane.showMessageDialog(this, "Leave request filed successfully.");
            }

            workingLeave = null;
            editMode = false;
            hrReviewMode = false;
            showListPage();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showListPage() {
        editMode = false;
        hrReviewMode = false;
        loadLeaveRequests();
        refreshActionButtons();
        cardLayout.show(contentPanel, LIST_CARD);
    }

    private void loadLeaveRequests() {
        if (model == null) {
            return;
        }

        model.setRowCount(0);

        List<Leave> leaves = getDisplayedLeaves();

        for (Leave leave : leaves) {
            model.addRow(new Object[]{
                    leave.getLeaveId(),
                    leave.getEmployeeId(),
                    leave.getLeaveType(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    leave.getNotes(),
                    leave.getStatus()
            });
        }

        if (infoLabel != null) {
            infoLabel.setText(model.getRowCount() + " leave record(s) loaded.");
        }

        refreshEmptyState();
    }

    private List<Leave> getDisplayedLeaves() {
        if (isHrUser()) {
            if (showAllRecords) {
                return leaveService.getAll();
            } else {
                return leaveService.getByEmployeeId(currentEmployeeId);
            }
        }
        return leaveService.getByEmployeeId(currentEmployeeId);
    }

    private void refreshEmptyState() {
        if (tableScrollPane == null) {
            return;
        }

        if (model.getRowCount() == 0) {
            tableScrollPane.setViewportView(emptyStateLabel);
        } else {
            tableScrollPane.setViewportView(table);
        }

        tableScrollPane.revalidate();
        tableScrollPane.repaint();
    }

    private void refreshActionButtons() {
        if (btnAdd == null || btnUpdate == null || btnDelete == null || btnRefresh == null) {
            return;
        }

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
        btnRefresh.setEnabled(true);
    }

    private int getSelectedLeaveId(int row) {
        if (row < 0) {
            return -1;
        }

        Object value = model.getValueAt(row, 0);
        if (value == null) {
            return -1;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
            return;
        }

        int leaveId = getSelectedLeaveId(row);
        if (leaveId < 0) {
            JOptionPane.showMessageDialog(this, "Selected leave request was not found.");
            return;
        }

        Leave selected = findSelectedLeave(leaveId);

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selected leave request was not found.");
            return;
        }

        boolean ownRecord = currentEmployeeId.equals(selected.getEmployeeId());

        if (isHrUser() && !ownRecord) {
            JOptionPane.showMessageDialog(
                    this,
                    "HR cannot delete another employee's leave record.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!ownRecord) {
            JOptionPane.showMessageDialog(
                    this,
                    "You can only delete your own leave requests.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!"Pending".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending leave requests can be deleted.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this leave request?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            leaveService.deleteOwnPendingLeave(leaveId, currentEmployeeId);
            loadLeaveRequests();
            JOptionPane.showMessageDialog(this, "Leave request deleted successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Leave findSelectedLeave(int leaveId) {
        List<Leave> leaves = getDisplayedLeaves();

        for (Leave leave : leaves) {
            if (leave.getLeaveId() == leaveId) {
                return leave;
            }
        }
        return null;
    }

    private boolean employeeExists(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }

        try {
            List<Employee> employees = employeeRepository.loadAll();
            for (Employee employee : employees) {
                if (employee != null
                        && employee.getId() != null
                        && employee.getId().trim().equalsIgnoreCase(employeeId.trim())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to validate Employee ID: " + e.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private boolean validateManagerEnteredEmployeeId(String employeeId) {
        String value = employeeId == null ? "" : employeeId.trim();

        if (value.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Employee ID is required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!employeeExists(value)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Employee ID was not found.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
    }

    private JButton createBlackButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(104, 40));
        button.setBackground(BLACK);
        button.setForeground(WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createWhiteButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(90, 40));
        button.setBackground(WHITE);
        button.setForeground(BLACK);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(TABLE_BORDER));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private boolean isHrUser() {
        return currentUser != null
                && currentUser.getRole() != null
                && "HR".equalsIgnoreCase(currentUser.getRole().getName());
    }
}
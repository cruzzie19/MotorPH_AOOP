/**
 *
 * @author Leianna Cruz
 */

package gui;

import model.AttendanceRecord;
import model.Employee;
import repository.CsvAttendanceRepository;
import service.AttendanceService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class AttendancePanel extends JPanel {

    private static final String LIST_CARD = "LIST";
    private static final String FORM_CARD = "FORM";

    private static final Color PAGE_BG = new Color(242, 242, 242);
    private static final Color TEXT_DARK = new Color(35, 35, 35);
    private static final Color MUTED_TEXT = new Color(130, 130, 130);

    private static final Color TABLE_BORDER = new Color(220, 220, 220);
    private static final Color TABLE_GRID = new Color(232, 232, 232);
    private static final Color TABLE_ROW_EVEN = new Color(245, 245, 245);
    private static final Color TABLE_ROW_ODD = new Color(239, 239, 239);
    private static final Color TABLE_SELECTION = new Color(220, 228, 240);

    private static final Color FIELD_BORDER = new Color(180, 180, 180);

    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;

    private static final String SEARCH_PLACEHOLDER = "Employee ID";

    private final AttendanceService attendanceService;
    private final Employee currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private JTable table;
    private DefaultTableModel model;
    private JScrollPane tableScrollPane;
    private JLabel emptyStateLabel;
    private JLabel infoLabel;

    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnTimeIn;
    private JButton btnTimeOut;
    private JButton btnRefresh;
    private JButton btnViewAll;
    private JButton btnViewMine;

    private JTextField txtEmployeeFilter;

    private AttendanceFormPanel formPanel;
    private AttendanceRecord selectedRecordForUpdate;

    public AttendancePanel() {
        this(new AttendanceService(new CsvAttendanceRepository()));
    }

    public AttendancePanel(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
        this.currentUser = SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        contentPanel.setOpaque(false);
        contentPanel.add(buildListPage(), LIST_CARD);
        contentPanel.add(buildFormPage(), FORM_CARD);

        add(contentPanel, BorderLayout.CENTER);

        loadAttendanceHistory();
        showListPage();
    }

    private JPanel buildListPage() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.add(buildTopArea(), BorderLayout.NORTH);
        wrapper.add(buildTableArea(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildTopArea() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(0, 0, 0, 0));

        boolean canViewBroader = attendanceService.canViewBroaderAttendance(currentUser);
        boolean canUpdateAny = attendanceService.canUpdateAnyAttendance(currentUser);

        if (canViewBroader) {
            JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            searchRow.setOpaque(false);

            txtEmployeeFilter = createSearchField();
            txtEmployeeFilter.addActionListener(e -> refreshBasedOnRole());

            searchRow.add(txtEmployeeFilter);

            top.add(searchRow);
            top.add(Box.createVerticalStrut(12));
        }

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);

        if (canViewBroader) {
            btnViewMine = createBlackButton("My Records");
            btnViewAll = createBlackButton("View All");

            btnViewMine.addActionListener(e -> loadMyAttendanceHistory());
            btnViewAll.addActionListener(e -> loadAttendanceHistory());

            leftButtons.add(btnViewMine);
            leftButtons.add(btnViewAll);
        }

        btnUpdate = createBlackButton("Update");
        btnDelete = createBlackButton("Delete");
        btnTimeIn = createBlackButton("Time In");
        btnTimeOut = createBlackButton("Time Out");
        btnRefresh = createWhiteButton("Refresh");

        if (canUpdateAny) {
            rightButtons.add(btnUpdate);
            rightButtons.add(btnDelete);
        }

        rightButtons.add(btnTimeIn);
        rightButtons.add(btnTimeOut);
        rightButtons.add(btnRefresh);

        btnUpdate.addActionListener(e -> openUpdateForm());
        btnDelete.addActionListener(e -> deleteSelectedAttendance());
        btnTimeIn.addActionListener(e -> handleTimeIn());
        btnTimeOut.addActionListener(e -> handleTimeOut());
        btnRefresh.addActionListener(e -> refreshBasedOnRole());

        buttonRow.add(leftButtons, BorderLayout.WEST);
        buttonRow.add(rightButtons, BorderLayout.EAST);

        top.add(buttonRow);

        return top;
    }

    private JPanel buildTableArea() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TABLE_BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        model = new DefaultTableModel(
                new Object[]{"Employee ID", "Date", "Time In", "Time Out"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable();

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(TABLE_ROW_EVEN);
        tableScrollPane.setBackground(WHITE);

        emptyStateLabel = new JLabel("No attendance history found.", SwingConstants.CENTER);
        emptyStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emptyStateLabel.setForeground(MUTED_TEXT);
        emptyStateLabel.setOpaque(true);
        emptyStateLabel.setBackground(WHITE);
        emptyStateLabel.setBorder(new EmptyBorder(30, 20, 30, 20));

        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(MUTED_TEXT);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 14, 10, 14));
        footer.add(infoLabel, BorderLayout.WEST);

        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        tableCard.add(footer, BorderLayout.SOUTH);

        return tableCard;
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
        formPanel = new AttendanceFormPanel();
        formPanel.addBackListener(e -> showListPage());
        formPanel.addSubmitListener(e -> submitUpdateForm());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void handleTimeIn() {
        try {
            attendanceService.timeIn(currentUser);
            loadAttendanceHistory();
            JOptionPane.showMessageDialog(this, "Time In recorded successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Time In Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTimeOut() {
        try {
            attendanceService.timeOut(currentUser);
            loadAttendanceHistory();
            JOptionPane.showMessageDialog(this, "Time Out recorded successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Time Out Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openUpdateForm() {
        if (!attendanceService.canUpdateAnyAttendance(currentUser)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to update attendance records.");
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendance record first.");
            return;
        }

        String employeeId = String.valueOf(model.getValueAt(row, 0));
        String date = String.valueOf(model.getValueAt(row, 1));
        String timeIn = String.valueOf(model.getValueAt(row, 2));
        String timeOut = String.valueOf(model.getValueAt(row, 3));

        selectedRecordForUpdate = new AttendanceRecord();
        selectedRecordForUpdate.setEmployeeId(employeeId);
        selectedRecordForUpdate.setDate(date);
        selectedRecordForUpdate.setLogIn(timeIn);
        selectedRecordForUpdate.setLogOut(timeOut);

        formPanel.setAttendanceData(employeeId, date, timeIn, timeOut);
        formPanel.setEditableFields(false, false, true, true);

        cardLayout.show(contentPanel, FORM_CARD);
    }

    private void submitUpdateForm() {
        if (selectedRecordForUpdate == null) {
            showListPage();
            return;
        }

        try {
            selectedRecordForUpdate.setLogIn(formPanel.getTimeIn());
            selectedRecordForUpdate.setLogOut(formPanel.getTimeOut());

            attendanceService.updateAttendance(currentUser, selectedRecordForUpdate);

            selectedRecordForUpdate = null;
            loadAttendanceHistory();
            JOptionPane.showMessageDialog(this, "Attendance updated successfully.");
            showListPage();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAttendance() {
        if (!attendanceService.canDeleteAnyAttendance(currentUser)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to delete attendance records.");
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendance record first.");
            return;
        }

        String employeeId = String.valueOf(model.getValueAt(row, 0));
        String date = String.valueOf(model.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this attendance record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            attendanceService.deleteAttendance(currentUser, employeeId, date);
            loadAttendanceHistory();
            JOptionPane.showMessageDialog(this, "Attendance deleted successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceHistory() {
        model.setRowCount(0);

        if (currentUser == null) {
            if (infoLabel != null) {
                infoLabel.setText("0 attendance record(s) loaded.");
            }
            refreshEmptyState();
            return;
        }

        List<AttendanceRecord> records = attendanceService.getVisibleAttendance(currentUser);

        for (AttendanceRecord record : records) {
            model.addRow(new Object[]{
                    record.getEmployeeId(),
                    record.getDate(),
                    record.getLogIn(),
                    record.getLogOut()
            });
        }

        if (infoLabel != null) {
            infoLabel.setText(model.getRowCount() + " attendance record(s) loaded.");
        }

        refreshEmptyState();
    }

    private void loadMyAttendanceHistory() {
        model.setRowCount(0);

        if (currentUser == null) {
            if (infoLabel != null) {
                infoLabel.setText("0 attendance record(s) loaded.");
            }
            refreshEmptyState();
            return;
        }

        List<AttendanceRecord> records = attendanceService.getAttendanceByEmployee(currentUser.getId());

        for (AttendanceRecord record : records) {
            model.addRow(new Object[]{
                    record.getEmployeeId(),
                    record.getDate(),
                    record.getLogIn(),
                    record.getLogOut()
            });
        }

        if (infoLabel != null) {
            infoLabel.setText(model.getRowCount() + " attendance record(s) loaded.");
        }

        refreshEmptyState();
    }

    private void refreshBasedOnRole() {
        if (currentUser == null) {
            if (infoLabel != null) {
                infoLabel.setText("0 attendance record(s) loaded.");
            }
            refreshEmptyState();
            return;
        }

        if (attendanceService.canViewBroaderAttendance(currentUser)) {
            loadAttendanceHistory();
            return;
        }

        loadMyAttendanceHistory();
    }

    private void showListPage() {
        selectedRecordForUpdate = null;
        cardLayout.show(contentPanel, LIST_CARD);
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

    private JTextField createSearchField() {
        JTextField field = new JTextField(14);
        field.setPreferredSize(new Dimension(180, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_DARK);
        field.setBackground(WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        field.setText(SEARCH_PLACEHOLDER);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(SEARCH_PLACEHOLDER);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private void refreshEmptyState() {
        if (model.getRowCount() == 0) {
            tableScrollPane.setViewportView(emptyStateLabel);
        } else {
            tableScrollPane.setViewportView(table);
        }

        tableScrollPane.revalidate();
        tableScrollPane.repaint();
    }
}
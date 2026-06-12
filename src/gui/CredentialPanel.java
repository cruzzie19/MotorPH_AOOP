/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Employee;
import service.SessionManager;
import model.CredentialRecord;
import repository.CredentialRepository;
import repository.EmployeeRepository;
import service.auth.AccountService;

import javax.swing.*;

import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Elizabeth
 */
public class CredentialPanel extends JPanel{
    
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
    
    private static final Color MAIN_BLUE = new Color(0, 102, 204); 
    private static final Color HOVER_BLUE = new Color(0, 82, 163);

    private static final String SEARCH_PLACEHOLDER = "Employee ID";

    private final AccountService accountService;
    private final Employee currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private JButton btnRefresh;
    
    private JTable table;
    private DefaultTableModel model;
    private JScrollPane tableScrollPane;
    private JLabel emptyStateLabel;
    private JLabel infoLabel;

    private int hoveredRow = -1;

    public CredentialPanel() {
        this(AccountService.createDefault(), null, null);
    }
    
    public CredentialPanel(AccountService accountService, CredentialRepository credRepo, EmployeeRepository empRepo) {
        if (accountService != null) {
            this.accountService = accountService;
        } else {
            this.accountService = new AccountService(credRepo, empRepo);
        }
        
        this.currentUser = SessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        contentPanel.setOpaque(false);
        contentPanel.add(buildListPage(), LIST_CARD);

        add(contentPanel, BorderLayout.CENTER);

        loadEmployeesCredentialOptions();
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

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);

        //txtEmployeeFilter = createSearchField();
        //txtEmployeeFilter.addActionListener(e -> refresh());

        //searchRow.add(txtEmployeeFilter, BorderLayout.WEST);

        top.add(searchRow);
        top.add(Box.createVerticalStrut(12));

        
        btnRefresh = createWhiteButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());

        searchRow.add(btnRefresh, BorderLayout.EAST);
        
        top.add(searchRow);

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
                new Object[]{"Employee ID", "Last Password Change", "Reset Password"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable();

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        
        setupInlineButtonColumn();

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(TABLE_ROW_EVEN);
        tableScrollPane.setBackground(WHITE);

        emptyStateLabel = new JLabel("No credential records found.", SwingConstants.CENTER);
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

    private void setupInlineButtonColumn() {
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            private final JButton buttonBase = createBlueButton("");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                buttonBase.setText(value != null ? value.toString() : "");

                if (row == hoveredRow) {
                    buttonBase.setBackground(HOVER_BLUE);
                } else {
                    buttonBase.setBackground(MAIN_BLUE);
                }

                return buttonBase;
            }
        });
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());

                if (column == 2 && row != -1) {
                    String targetEmployeeId = table.getValueAt(row, 0).toString();
                    showChangePasswordDialog(targetEmployeeId);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredRow != -1) {
                    hoveredRow = -1;
                    table.repaint();
                    table.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        
        MouseInputAdapter trackHover = new MouseInputAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());

                int oldHoveredRow = hoveredRow;

                if (column == 2 && row != -1) {
                    hoveredRow = row;
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    hoveredRow = -1;
                    table.setCursor(Cursor.getDefaultCursor());
                }

                if (hoveredRow != oldHoveredRow) {
                    table.repaint();
                }
            }
        };
        
        table.addMouseMotionListener(trackHover);
    }

    private void loadEmployeesCredentialOptions() {
        model.setRowCount(0);

        if (currentUser == null) {
            if (infoLabel != null) {
                infoLabel.setText("0 credential record(s) loaded.");
            }
            refreshEmptyState();
            return;
        }
        
        //AccountService accountService = AccountService.createDefault();
        
        try{
            List<CredentialRecord> records = accountService.retrieveAll();
            for (CredentialRecord record : records) {
            model.addRow(new Object[]{
                record.getUsername(),
                record.getLastPaswordChange(),
                "Reset Password"
            });
        }
        } catch(Exception ex){
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Login failed due to a system error.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        if (infoLabel != null) {
            infoLabel.setText(model.getRowCount() + " credential record(s) loaded.");
        }

        refreshEmptyState();
    }

    private void showChangePasswordDialog(String targetEmployeeId) {
        JLabel lblUsernameValue = new JLabel(targetEmployeeId);
        lblUsernameValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsernameValue.setForeground(TEXT_DARK);

        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Username"));
        form.add(lblUsernameValue);
        form.add(new JLabel("New Password"));
        form.add(newPasswordField);
        form.add(new JLabel("Confirm New Password"));
        form.add(confirmPasswordField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        try {
            if (newPassword.length == 0) {
                JOptionPane.showMessageDialog(this, "New password cannot be blank.", "Change Password", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Arrays.equals(newPassword, confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Change Password", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //AccountService accountService = AccountService.createDefault();
            boolean changed = accountService.registerOrUpdate(targetEmployeeId, newPassword);

            if (!changed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to change the password due to system error.",
                        "Change Password",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Change Password", JOptionPane.INFORMATION_MESSAGE);

            refresh(); 

        } finally {
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }
    
    private void refresh() {
        if (currentUser == null) {
            if (infoLabel != null) {
                infoLabel.setText("0 credential record(s) loaded.");
            }
            refreshEmptyState();
            return;
        }

        loadEmployeesCredentialOptions();
    }

    private void showListPage() {
        cardLayout.show(contentPanel, LIST_CARD);
    }

    private JButton createBlueButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(104, 40));
        button.setBackground(MAIN_BLUE);
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

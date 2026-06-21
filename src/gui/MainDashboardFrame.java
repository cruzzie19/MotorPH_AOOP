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

import RBAC.Permission;

import repository.EmployeeRepository;

import service.AuthorizationService;
import service.LeaveService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import repository.CredentialRepository;
import service.auth.AccountService;

public class MainDashboardFrame extends JFrame {

    private static final String CARD_DASHBOARD = "dashboard";
    private static final String CARD_EMPLOYEES = "employees";
    private static final String CARD_PAYROLL = "payroll";
    private static final String CARD_LEAVE = "leave";
    private static final String CARD_ATTENDANCE = "attendance";
    private static final String CARD_CREDENTIAL = "credential";

    private static final Color SIDEBAR_BG = Color.BLACK;
    private static final Color MAIN_BG = new Color(242, 242, 242);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color TEXT_DARK = new Color(35, 35, 35);
    private static final Color ACCENT = new Color(20, 20, 90);
    private static final Color MUTED = new Color(145, 145, 145);
    private static final Color WHITE = Color.WHITE;
    private static final Color MAIN_BLUE = new Color(0, 102, 204);
    private static final Color HOVER_BLUE = new Color(0, 82, 163);

    private final EmployeeRepository employeeRepo;
    private final Employee currentUser;
    private final LeaveService leaveService;
    private final AccountService accountService;

    private final String currentUserId;
    private final String currentUserName;
    private final String currentUserDepartment;
    private final String currentUserPosition;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    public MainDashboardFrame(EmployeeRepository employeeRepo, Employee loggedInEmployee, AccountService accountService, CredentialRepository credRepo) {
        super("MotorPH Payroll System");
        
        if (accountService != null) {
            this.accountService = accountService;
        } else {
            this.accountService = new AccountService(credRepo, employeeRepo);
        }

        this.employeeRepo = employeeRepo;
        this.currentUser = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser()
                : loggedInEmployee;

        this.leaveService = LeaveService.createDefault();

        this.currentUserId = currentUser != null ? safe(currentUser.getId()) : "";
        this.currentUserName = currentUser != null
                ? (safe(currentUser.getFirstName()) + " " + safe(currentUser.getLastName())).trim()
                : "";
        this.currentUserDepartment = currentUser != null ? safe(currentUser.getDepartment()) : "";
        this.currentUserPosition = currentUser != null ? safe(currentUser.getPosition()) : "";

        applyGlobalFont();

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.contentPanel.setOpaque(false);

        initFrame();
        initCards();

        showCard(CARD_EMPLOYEES);
    }

    private void applyGlobalFont() {
        Font segoe = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", segoe);
        UIManager.put("Button.font", segoe);
        UIManager.put("Table.font", segoe);
        UIManager.put("TableHeader.font", segoe.deriveFont(Font.BOLD, 14f));
        UIManager.put("TextField.font", segoe);
        UIManager.put("PasswordField.font", segoe);
        UIManager.put("ComboBox.font", segoe);
        UIManager.put("OptionPane.font", segoe);
        UIManager.put("OptionPane.messageFont", segoe);
        UIManager.put("OptionPane.buttonFont", segoe);
    }

    private void initFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 680));
        setSize(1280, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAIN_BG);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(270, 0));
        sidebar.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel("MotorPH");
        logoLabel.setForeground(TEXT_LIGHT);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(logoLabel);
        topSection.add(Box.createVerticalStrut(50));

        topSection.add(createNavLink("Dashboard", () -> showCard(CARD_DASHBOARD)));
        topSection.add(Box.createVerticalStrut(22));

        if (AuthorizationService.hasPermission(currentUser, Permission.VIEW_EMPLOYEE_LIST)
                || AuthorizationService.hasPermission(currentUser, Permission.VIEW_EMPLOYEE)) {
            topSection.add(createNavLink("Employees", () -> showCard(CARD_EMPLOYEES)));
            topSection.add(Box.createVerticalStrut(22));
        }

        topSection.add(createNavLink("Payroll", () -> showCard(CARD_PAYROLL)));
        topSection.add(Box.createVerticalStrut(22));
        topSection.add(createNavLink("Leave", () -> showCard(CARD_LEAVE)));
        topSection.add(Box.createVerticalStrut(22));
        topSection.add(createNavLink("Attendance", () -> showCard(CARD_ATTENDANCE)));
        topSection.add(Box.createVerticalStrut(22));

        if (AuthorizationService.hasPermission(currentUser, Permission.ACCESS_SYSTEM_TOOLS)) {
            topSection.add(createNavLink("Credential", () -> showCard(CARD_CREDENTIAL)));
        }
        
        JPanel bottomSection = new JPanel();
        bottomSection.setOpaque(false);
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));

        JLabel logoutLabel = createNavLink("Log Out", this::handleLogout);
        bottomSection.add(logoutLabel);

        sidebar.add(topSection, BorderLayout.NORTH);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    private JLabel createNavLink(String text, Runnable action) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_LIGHT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        label.setPreferredSize(new Dimension(180, 28));

        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                label.setForeground(new Color(210, 210, 210));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                label.setForeground(TEXT_LIGHT);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });

        return label;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(MAIN_BG);
        mainArea.setBorder(new EmptyBorder(24, 28, 24, 28));

        mainArea.add(createTopBar(), BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel rightProfile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightProfile.setOpaque(false);
        
        JButton btnChangePassword = new JButton("Change Password");
        btnChangePassword.setPreferredSize(new Dimension(140, 32));
        btnChangePassword.setBackground(MAIN_BLUE);
        btnChangePassword.setForeground(WHITE);
        btnChangePassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.setBorder(BorderFactory.createEmptyBorder());
        btnChangePassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnChangePassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnChangePassword.setBackground(HOVER_BLUE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnChangePassword.setBackground(MAIN_BLUE);
            }
        });
        
        btnChangePassword.addActionListener(e -> {
            Employee user = SessionManager.getCurrentUser();
            if (user == null) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(topBar), "No active user session detected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String targetEmployeeId = user.getId();
            
            JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));

            Color textDark = (this.TEXT_DARK != null) ? this.TEXT_DARK : new Color(35, 35, 35);

            JLabel lblUsernameTitle = new JLabel("Username");
            JLabel lblUsernameValue = new JLabel(targetEmployeeId);
            lblUsernameValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblUsernameValue.setForeground(textDark);

            JPasswordField newPasswordField = new JPasswordField(20);
            JPasswordField confirmPasswordField = new JPasswordField(20);

            form.add(lblUsernameTitle);
            form.add(lblUsernameValue);
            form.add(new JLabel("New Password"));
            form.add(newPasswordField);
            form.add(new JLabel("Confirm New Password"));
            form.add(confirmPasswordField);

            int option = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(topBar),
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
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(topBar), "New password cannot be blank.", "Change Password", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!java.util.Arrays.equals(newPassword, confirmPassword)) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(topBar), "New passwords do not match.", "Change Password", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean changed = this.accountService.registerOrUpdate(targetEmployeeId, newPassword);

                if (!changed) {
                    JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(topBar),
                        "Failed to change the password due to system error.",
                        "Change Password",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(topBar), "Password changed successfully.", "Change Password", JOptionPane.INFORMATION_MESSAGE);

            } finally {
                java.util.Arrays.fill(newPassword, '\0');
                java.util.Arrays.fill(confirmPassword, '\0');
            }
        });

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(currentUserName.isBlank() ? "Name" : currentUserName);
        nameLabel.setForeground(ACCENT);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel positionLabel = new JLabel(currentUserPosition.isBlank() ? "Position" : currentUserPosition);
        positionLabel.setForeground(MUTED);
        positionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        positionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(positionLabel);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(56, 56));

        rightProfile.add(btnChangePassword);
        rightProfile.add(textPanel);
        rightProfile.add(avatar);

        topBar.add(rightProfile, BorderLayout.EAST);

        return topBar;
    }

    private void initCards() {
        addCard(CARD_DASHBOARD, createDashboardCard());
        addCard(CARD_EMPLOYEES, createEmployeesCard());
        addCard(CARD_PAYROLL, createPayrollCard());
        addCard(CARD_LEAVE, createLeaveCard());
        addCard(CARD_ATTENDANCE, createAttendanceCard());
        addCard(CARD_CREDENTIAL, createCredentialCard());
    }

    private void addCard(String cardName, JPanel panel) {
        contentPanel.add(panel, cardName);
    }

    private JPanel createDashboardCard() {
        return new DashboardPanel();
    }

    private JPanel createEmployeesCard() {
        return new EmployeeManagementPanel(employeeRepo, "the employee database", currentUser);
    }

    private JPanel createPayrollCard() {
        return new PayrollPanel(currentUser, employeeRepo);
    }

    private JPanel createLeaveCard() {
        return new EmployeeLeavesPanel(
                leaveService,
                employeeRepo,
                currentUserId,
                currentUserName,
                currentUserDepartment,
                currentUserPosition
        );
    }

    private JPanel createAttendanceCard() {
        return new AttendancePanel();
    }
    
    private JPanel createCredentialCard() {
        return new CredentialPanel();
    }

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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

        } finally {
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }
    
    

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Log Out",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SessionManager.logout();
        dispose();

        SwingUtilities.invokeLater(() -> {
            JFrame dummy = new JFrame();
            dummy.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            LoginDialog loginDialog = new LoginDialog(dummy);
            loginDialog.setVisible(true);

            if (!loginDialog.isSucceeded()) {
                dummy.dispose();
                System.exit(0);
                return;
            }

            Employee loggedInEmployee = loginDialog.getLoggedInEmployee();
            dummy.dispose();

            MainDashboardLauncher.launch(loggedInEmployee);
        });
    }
}
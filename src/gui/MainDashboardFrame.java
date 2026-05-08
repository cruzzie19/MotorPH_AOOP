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
import repository.CsvLeaveRepository;

import service.AuthorizationService;
import service.LeaveService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;

public class MainDashboardFrame extends JFrame {

    private static final String CARD_DASHBOARD = "dashboard";
    private static final String CARD_EMPLOYEES = "employees";
    private static final String CARD_PAYROLL = "payroll";
    private static final String CARD_LEAVE = "leave";
    private static final String CARD_ATTENDANCE = "attendance";

    private static final Color SIDEBAR_BG = Color.BLACK;
    private static final Color MAIN_BG = new Color(242, 242, 242);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color ACCENT = new Color(20, 20, 90);
    private static final Color MUTED = new Color(145, 145, 145);

    private final EmployeeRepository employeeRepo;
    private final Path employeeCsvPath;
    private final Employee currentUser;
    private final LeaveService leaveService;

    private final String currentUserId;
    private final String currentUserName;
    private final String currentUserDepartment;
    private final String currentUserPosition;

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    public MainDashboardFrame(EmployeeRepository employeeRepo, Path employeeCsvPath, Employee loggedInEmployee) {
        super("MotorPH Payroll System");

        this.employeeRepo = employeeRepo;
        this.employeeCsvPath = employeeCsvPath;
        this.currentUser = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser()
                : loggedInEmployee;

        this.leaveService = new LeaveService(new CsvLeaveRepository());

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
    }

    private void addCard(String cardName, JPanel panel) {
        contentPanel.add(panel, cardName);
    }

    private JPanel createDashboardCard() {
        return new DashboardPanel();
    }

    private JPanel createEmployeesCard() {
        return new EmployeeManagementPanel(employeeRepo, employeeCsvPath, currentUser);
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

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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
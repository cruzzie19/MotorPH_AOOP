/**
 *
 * @author Leianna Cruz
 */

package gui;

import model.Employee;
import service.auth.AccountService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LoginDialog extends JDialog {

    private static final String TITLE_TEXT = "MotorPH Payroll System";

    private static final Color FALLBACK_BG = new Color(230, 238, 247);
    private static final Color CARD_BORDER = new Color(235, 235, 235);
    private static final Color FIELD_BORDER = new Color(125, 125, 125);
    private static final Color TEXT_COLOR = new Color(35, 35, 35);
    private static final Color LINK_COLOR = new Color(120, 120, 120);
    private static final Color LINK_HOVER_COLOR = new Color(70, 70, 70);

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin;
    private JLabel lblForgotPassword;

    private boolean succeeded;
    private Image backgroundImage;

    private Font fontTitle;
    private Font fontLabel;
    private Font fontField;
    private Font fontButton;
    private Font fontLink;

    private int cardWidth;
    private int cardHeight;
    private int titleWidth;
    private int fieldWidth;
    private int buttonWidth;
    private int fieldHeight;
    private int buttonHeight;

    private int outerPadding;
    private int cardPaddingVertical;
    private int cardPaddingHorizontal;

    private int topGap;
    private int afterTitleGap;
    private int labelToFieldGap;
    private int sectionGap;
    private int forgotGap;
    private int beforeButtonGap;

    // Added fields for logged-in user info
    private String loggedInName = "Name";
    private String loggedInPosition = "Position";
    
    private Employee loggedInEmployee;

    public LoginDialog() {
        this((Frame) null);
    }

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        loadBackgroundImage();
        calculateResponsiveMetrics();
        initializeUI(parent);
        ensureInitialAccount();
    }

    public String getLoggedInName() {
        return loggedInName;
    }

    public String getLoggedInPosition() {
        return loggedInPosition;
    }
    
    public Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }

    private void loadBackgroundImage() {
        java.net.URL imageUrl = getClass().getResource("/asset/LoginBackground.png");
        if (imageUrl != null) {
            backgroundImage = new ImageIcon(imageUrl).getImage();
        } else {
            System.out.println("Background image not found: /asset/LoginBackground.png");
        }
    }

    private void calculateResponsiveMetrics() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double scale = Math.min(screen.width / 1365.0, screen.height / 768.0);

        double panelScale = scale * 0.64;

        fontTitle = new Font("Segoe UI", Font.BOLD, clamp((int) Math.round(34 * panelScale), 22, 34));
        fontLabel = new Font("Segoe UI", Font.PLAIN, clamp((int) Math.round(17 * panelScale), 14, 17));
        fontField = new Font("Segoe UI", Font.PLAIN, clamp((int) Math.round(18 * panelScale), 14, 18));
        fontButton = new Font("Segoe UI", Font.PLAIN, clamp((int) Math.round(18 * panelScale), 14, 18));
        fontLink = new Font("Segoe UI", Font.PLAIN, clamp((int) Math.round(15 * panelScale), 12, 15));

        cardWidth = clamp((int) Math.round(620 * panelScale), 360, 500);
        cardHeight = clamp((int) Math.round(540 * panelScale), 320, 440);

        cardPaddingHorizontal = clamp((int) Math.round(58 * panelScale), 24, 44);
        cardPaddingVertical = clamp((int) Math.round(42 * panelScale), 20, 34);
        outerPadding = clamp((int) Math.round(40 * panelScale), 16, 32);

        titleWidth = measureTextWidth(TITLE_TEXT, fontTitle);
        fieldWidth = (int) Math.round(titleWidth * 0.75);
        buttonWidth = (int) Math.round(titleWidth * 0.50);

        fieldHeight = clamp((int) Math.round(62 * panelScale), 38, 52);
        buttonHeight = fieldHeight;

        topGap = clamp((int) Math.round(8 * panelScale), 4, 8);
        afterTitleGap = clamp((int) Math.round(42 * panelScale), 14, 28);
        labelToFieldGap = clamp((int) Math.round(10 * panelScale), 5, 8);
        sectionGap = clamp((int) Math.round(28 * panelScale), 10, 20);
        forgotGap = clamp((int) Math.round(10 * panelScale), 5, 8);
        beforeButtonGap = clamp((int) Math.round(38 * panelScale), 14, 24);

        int minCardContentWidth = Math.max(titleWidth, fieldWidth) + cardPaddingHorizontal * 2;
        cardWidth = Math.max(cardWidth, minCardContentWidth);

        int estimatedContentHeight =
                topGap
                        + getTextHeight(fontTitle)
                        + afterTitleGap
                        + getTextHeight(fontLabel)
                        + labelToFieldGap
                        + fieldHeight
                        + sectionGap
                        + getTextHeight(fontLabel)
                        + labelToFieldGap
                        + fieldHeight
                        + forgotGap
                        + getTextHeight(fontLink)
                        + beforeButtonGap
                        + buttonHeight
                        + cardPaddingVertical * 2
                        + 24;

        cardHeight = Math.max(cardHeight, estimatedContentHeight);
    }

    private void initializeUI(Frame parent) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel backgroundPanel = createBackgroundPanel();
        backgroundPanel.setBorder(new EmptyBorder(outerPadding, outerPadding, outerPadding, outerPadding));

        JPanel cardPanel = createCardPanel();

        backgroundPanel.add(cardPanel, new GridBagConstraints());
        setContentPane(backgroundPanel);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocationRelativeTo(parent);

        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel createBackgroundPanel() {
        return new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(FALLBACK_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
    }

    private JPanel createCardPanel() {
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(cardPaddingVertical, cardPaddingHorizontal, cardPaddingVertical, cardPaddingHorizontal)
        ));
        cardPanel.setPreferredSize(new Dimension(cardWidth, cardHeight));

        JLabel titleLabel = createLabel(TITLE_TEXT, fontTitle, Color.BLACK, titleWidth, SwingConstants.CENTER);
        JLabel usernameLabel = createLabel("Username", fontLabel, TEXT_COLOR, fieldWidth, SwingConstants.LEFT);
        JLabel passwordLabel = createLabel("Password", fontLabel, TEXT_COLOR, fieldWidth, SwingConstants.LEFT);

        tfUsername = createStyledTextField();
        pfPassword = createStyledPasswordField();

        lblForgotPassword = new JLabel("<html><u>Forgot Password?</u></html>");
        lblForgotPassword.setFont(fontLink);
        lblForgotPassword.setForeground(LINK_COLOR);
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showChangePasswordDialog();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblForgotPassword.setForeground(LINK_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblForgotPassword.setForeground(LINK_COLOR);
            }
        });

        JPanel forgotPanel = new JPanel(new BorderLayout());
        forgotPanel.setOpaque(false);
        forgotPanel.setPreferredSize(new Dimension(fieldWidth, getTextHeight(fontLink) + 4));
        forgotPanel.add(lblForgotPassword, BorderLayout.EAST);

        btnLogin = new JButton("Login");
        btnLogin.setFont(fontButton);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(Color.BLACK);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        btnLogin.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnLogin.addActionListener(e -> attemptLogin());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 0;
        gbc.insets = new Insets(topGap, 0, 0, 0);
        cardPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(afterTitleGap, 0, 0, 0);
        cardPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(labelToFieldGap, 0, 0, 0);
        cardPanel.add(tfUsername, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(sectionGap, 0, 0, 0);
        cardPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(labelToFieldGap, 0, 0, 0);
        cardPanel.add(pfPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(forgotGap, 0, 0, 0);
        cardPanel.add(forgotPanel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(beforeButtonGap, 0, 0, 0);
        cardPanel.add(btnLogin, gbc);

        return cardPanel;
    }

    private JLabel createLabel(String text, Font font, Color color, int width, int alignment) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setHorizontalAlignment(alignment);
        label.setPreferredSize(new Dimension(width, getTextHeight(font) + 4));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(fontField);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        field.setBorder(new CompoundBorder(
                new LineBorder(FIELD_BORDER, 2, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(fontField);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        field.setBorder(new CompoundBorder(
                new LineBorder(FIELD_BORDER, 2, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private int measureTextWidth(String text, Font font) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setFont(font);
            return g2.getFontMetrics().stringWidth(text);
        } finally {
            g2.dispose();
        }
    }

    private int getTextHeight(Font font) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setFont(font);
            return g2.getFontMetrics().getHeight();
        } finally {
            g2.dispose();
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void attemptLogin() {

        String username = getUsername();
        String password = getPassword();

        try {

            // Create authentication service
            repository.CsvEmployeeRepository repo =
                    new repository.CsvEmployeeRepository("data/MotorPH Employee Record.csv");

            service.auth.AccountService accountService =
                    new service.auth.AccountService(repo);

            service.AuthenticationService authService =
                    new service.AuthenticationService(accountService);

            // Attempt login
            Employee employee = authService.login(username, password);

            if (employee != null) {

                succeeded = true;

                loggedInEmployee = employee;
                loggedInName = employee.getFirstName() + " " + employee.getLastName();
                loggedInPosition = employee.getPosition();

                // Store user in session
                service.SessionManager.setCurrentUser(employee);

                dispose();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login",
                        JOptionPane.ERROR_MESSAGE
                );

                tfUsername.setText("");
                pfPassword.setText("");
                succeeded = false;
                loggedInEmployee = null;
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Login failed due to a system error.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showChangePasswordDialog() {
        JTextField usernameField = new JTextField(20);
        usernameField.setText(getUsername());
        JPasswordField oldPasswordField = new JPasswordField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel("Current Password"));
        form.add(oldPasswordField);
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

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        char[] oldPassword = oldPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        try {
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required.", "Change Password", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (oldPassword.length == 0 || newPassword.length == 0) {
                JOptionPane.showMessageDialog(this, "Current and new password are required.", "Change Password", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Arrays.equals(newPassword, confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Change Password", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AccountService accountService = AccountService.createDefault();
            boolean changed = accountService.changePassword(username, oldPassword, newPassword);
            if (!changed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to change password. Check your username and current password.",
                        "Change Password",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Change Password", JOptionPane.INFORMATION_MESSAGE);
            tfUsername.setText(username);
            pfPassword.setText("");
        } finally {
            Arrays.fill(oldPassword, '\0');
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }

    private void ensureInitialAccount() {
        AccountService accountService = AccountService.createDefault();
        if (accountService.hasAccounts()) {
            return;
        }

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel setupPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        setupPanel.add(new JLabel("No login account found. Create admin account:"));
        setupPanel.add(new JLabel("Username"));
        setupPanel.add(usernameField);
        setupPanel.add(new JLabel("Password"));
        setupPanel.add(passwordField);
        setupPanel.add(new JLabel("Confirm Password"));
        setupPanel.add(confirmField);

        int option = JOptionPane.showConfirmDialog(
                this,
                setupPanel,
                "Initial Account Setup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();

        try {
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required.", "Setup", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length == 0) {
                JOptionPane.showMessageDialog(this, "Password is required.", "Setup", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Arrays.equals(password, confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Setup", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean saved = accountService.registerOrUpdate(username, password);
            if (!saved) {
                JOptionPane.showMessageDialog(this, "Unable to save account.", "Setup", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Account created. Please log in.", "Setup", JOptionPane.INFORMATION_MESSAGE);
            tfUsername.setText(username);
            pfPassword.setText("");
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
   

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
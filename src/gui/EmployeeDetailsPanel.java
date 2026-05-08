/**
 *
 * @author Leianna Cruz
 */

package gui;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class EmployeeDetailsPanel extends JPanel {

    public enum Mode {
        VIEW,
        UPDATE,
        CREATE
    }

    public interface EmployeeDetailsActionListener {
        void onCreate(EmployeeDetailsPanel panel);
        void onUpdate(EmployeeDetailsPanel panel);
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    private static final Color PAGE_BG = new Color(242, 242, 242);
    private static final Color TEXT_PRIMARY = new Color(20, 20, 20);
    private static final Color LINK_COLOR = new Color(95, 95, 95);
    private static final Color LINK_HOVER_COLOR = new Color(55, 55, 55);
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color FIELD_BORDER = new Color(110, 110, 110);
    private static final Color READONLY_BG = Color.WHITE;
    private static final Color EDITABLE_BG = Color.WHITE;
    private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
    private static final Color NORMAL_TEXT_COLOR = Color.BLACK;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BACK = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.PLAIN, 15);

    private final Runnable onBack;
    private final JPanel formGridPanel = new JPanel(new GridBagLayout());

    private JButton submitButton;

    private Mode mode = Mode.VIEW;
    private Employee currentEmployee;

    private boolean showPersonalDetails;
    private boolean showGovernmentIds;
    private boolean showCompensation;

    private EmployeeDetailsActionListener actionListener;

    private JTextField employeeNoField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField statusField;
    private JTextField positionField;
    private JTextField supervisorField;
    private JTextField roleField;

    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField addressField;

    private JTextField sssField;
    private JTextField philHealthField;
    private JTextField tinField;
    private JTextField pagIbigField;

    private JTextField basicSalaryField;
    private JTextField riceSubsidyField;
    private JTextField phoneAllowanceField;
    private JTextField clothingAllowanceField;
    private JTextField grossSemiMonthlyField;
    private JTextField hourlyRateField;

    public EmployeeDetailsPanel(Runnable onBack) {
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(2, 20, 24, 34));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createScrollableCenter(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    public void setActionListener(EmployeeDetailsActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.VIEW : mode;
        applyMode();
    }

    public Mode getMode() {
        return mode;
    }

    public Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public void displayEmployee(
            Employee employee,
            boolean showPersonalDetails,
            boolean showGovernmentIds,
            boolean showCompensation
    ) {
        this.currentEmployee = employee;
        this.showPersonalDetails = showPersonalDetails;
        this.showGovernmentIds = showGovernmentIds;
        this.showCompensation = showCompensation;

        formGridPanel.removeAll();
        resetFieldReferences();

        int visibleSections = 1;
        if (showPersonalDetails) visibleSections++;
        if (showGovernmentIds) visibleSections++;
        if (showCompensation) visibleSections++;

        int columnIndex = 0;

        addSection(createBasicSection(employee), columnIndex++, visibleSections);

        if (showPersonalDetails) {
            addSection(createPersonalSection(employee), columnIndex++, visibleSections);
        }

        if (showGovernmentIds) {
            addSection(createGovernmentSection(employee), columnIndex++, visibleSections);
        }

        if (showCompensation) {
            addSection(createCompensationSection(employee), columnIndex, visibleSections);
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 1;
        filler.gridwidth = visibleSections;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        formGridPanel.add(Box.createGlue(), filler);

        applyMode();

        revalidate();
        repaint();
    }

    public void showCreateMode(Runnable onSaveSuccess, Runnable onCancel) {
        displayEmployee(null, true, true, true);
        setMode(Mode.CREATE);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel backLabel = new JLabel("<html><u>Back</u></html>");
        backLabel.setFont(FONT_BACK);
        backLabel.setForeground(LINK_COLOR);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onBack != null) {
                    onBack.run();
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backLabel.setForeground(LINK_HOVER_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backLabel.setForeground(LINK_COLOR);
            }
        });

        topPanel.add(backLabel);
        return topPanel;
    }

    private JScrollPane createScrollableCenter() {
        formGridPanel.setOpaque(false);
        formGridPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(formGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(18, 0, 0, 0));

        submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(130, 38));
        submitButton.setBackground(Color.BLACK);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(FONT_BUTTON);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> handleSubmit());

        bottomPanel.add(submitButton);
        return bottomPanel;
    }

    private void handleSubmit() {
        switch (mode) {
            case CREATE:
                if (actionListener != null) {
                    actionListener.onCreate(this);
                }
                break;

            case UPDATE:
                if (actionListener != null) {
                    actionListener.onUpdate(this);
                }
                break;

            case VIEW:
            default:
                break;
        }
    }

    private void applyMode() {
        boolean editable = (mode == Mode.CREATE || mode == Mode.UPDATE);

        setFieldEditable(employeeNoField, mode == Mode.CREATE);
        setFieldEditable(firstNameField, editable);
        setFieldEditable(lastNameField, editable);
        setFieldEditable(statusField, editable);
        setFieldEditable(positionField, editable);
        setFieldEditable(supervisorField, editable);
        setFieldEditable(roleField, editable);

        setFieldEditable(birthDateField, editable && showPersonalDetails);
        setFieldEditable(phoneField, editable && showPersonalDetails);
        setFieldEditable(addressField, editable && showPersonalDetails);

        setFieldEditable(sssField, editable && showGovernmentIds);
        setFieldEditable(philHealthField, editable && showGovernmentIds);
        setFieldEditable(tinField, editable && showGovernmentIds);
        setFieldEditable(pagIbigField, editable && showGovernmentIds);

        setFieldEditable(basicSalaryField, editable && showCompensation);
        setFieldEditable(riceSubsidyField, editable && showCompensation);
        setFieldEditable(phoneAllowanceField, editable && showCompensation);
        setFieldEditable(clothingAllowanceField, editable && showCompensation);
        setFieldEditable(grossSemiMonthlyField, editable && showCompensation);
        setFieldEditable(hourlyRateField, editable && showCompensation);

        if (submitButton != null) {
            submitButton.setVisible(mode != Mode.VIEW);

            if (mode == Mode.CREATE) {
                submitButton.setText("Add Employee");
            } else if (mode == Mode.UPDATE) {
                submitButton.setText("Update Employee");
            } else {
                submitButton.setText("Submit");
            }
        }
    }

    private void setFieldEditable(JTextField field, boolean editable) {
        if (field == null) {
            return;
        }

        field.setEditable(editable);
        field.setFocusable(editable);
        field.setBackground(editable ? EDITABLE_BG : READONLY_BG);
        field.setForeground(field.getForeground().equals(PLACEHOLDER_COLOR) ? PLACEHOLDER_COLOR : TEXT_PRIMARY);
    }

    private void resetFieldReferences() {
        employeeNoField = null;
        firstNameField = null;
        lastNameField = null;
        statusField = null;
        positionField = null;
        supervisorField = null;
        roleField = null;

        birthDateField = null;
        phoneField = null;
        addressField = null;

        sssField = null;
        philHealthField = null;
        tinField = null;
        pagIbigField = null;

        basicSalaryField = null;
        riceSubsidyField = null;
        phoneAllowanceField = null;
        clothingAllowanceField = null;
        grossSemiMonthlyField = null;
        hourlyRateField = null;
    }

    private void addSection(JPanel section, int gridx, int visibleSections) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = 0;
        gbc.weightx = 1.0 / visibleSections;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, gridx == 0 ? 0 : 18, 0, 0);

        formGridPanel.add(section, gbc);
    }

    private JPanel createBasicSection(Employee e) {
        JPanel section = createSectionPanel("Basic Information");

        employeeNoField = addField(section, "Employee No.", safe(e != null ? e.getId() : ""));
        firstNameField = addField(section, "First Name", safe(e != null ? e.getFirstName() : ""));
        lastNameField = addField(section, "Last Name", safe(e != null ? e.getLastName() : ""));
        statusField = addField(section, "Status", safe(e != null ? e.getStatus() : ""));
        positionField = addField(section, "Position", safe(e != null ? e.getPosition() : ""));
        supervisorField = addField(section, "Immediate Supervisor", safe(e != null ? e.getSupervisor() : ""));
        roleField = addField(
                section,
                "Role",
                e != null && e.getRole() != null ? safe(e.getRole().getName()) : ""
        );

        return section;
    }

    private JPanel createPersonalSection(Employee e) {
        JPanel section = createSectionPanel("Personal Details");

        birthDateField = addField(
                section,
                "Birth Date",
                e != null && e.getBirthDate() != null ? e.getBirthDate().format(DATE_FORMAT) : ""
        );
        phoneField = addField(section, "Phone No.", safe(e != null ? e.getPhone() : ""));
        addressField = addField(section, "Address", safe(e != null ? e.getAddress() : ""));

        return section;
    }

    private JPanel createGovernmentSection(Employee e) {
        JPanel section = createSectionPanel("Government ID");

        sssField = addField(section, "SSS No.", safe(e != null ? e.getSssNumber() : ""));
        philHealthField = addField(section, "PhilHealth No.", safe(e != null ? e.getPhilHealthNumber() : ""));
        tinField = addField(section, "TIN", safe(e != null ? e.getTinNumber() : ""));
        pagIbigField = addField(section, "Pag-IBIG No.", safe(e != null ? e.getPagIbigNumber() : ""));

        return section;
    }

    private JPanel createCompensationSection(Employee e) {
        JPanel section = createSectionPanel("Compensation");

        basicSalaryField = addField(section, "Basic Salary", formatMoney(e != null ? e.getBasicSalary() : null));
        riceSubsidyField = addField(section, "Rice Subsidy", formatMoney(e != null ? e.getRiceSubsidy() : null));
        phoneAllowanceField = addField(section, "Phone Allowance", formatMoney(e != null ? e.getPhoneAllowance() : null));
        clothingAllowanceField = addField(section, "Clothing Allowance", formatMoney(e != null ? e.getClothingAllowance() : null));
        grossSemiMonthlyField = addField(section, "Gross Semi-Monthly Rate", formatMoney(e != null ? e.getGrossSemiMonthlyRate() : null));
        hourlyRateField = addField(section, "Hourly Rate", formatMoney(e != null ? e.getHourlyRate() : null));

        return section;
    }

    private JPanel createSectionPanel(String title) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(titleLabel);
        section.add(Box.createVerticalStrut(6));

        return section;
    }

    private JTextField addField(JPanel section, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FONT_LABEL);
        labelComp.setForeground(TEXT_PRIMARY);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField(value == null ? "" : value);
        field.setFont(FONT_VALUE);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(FIELD_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(6, 10, 6, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(150, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        String placeholder = getPlaceholderForLabel(label);
        if ((value == null || value.trim().isEmpty()) && !placeholder.isEmpty()) {
            applyPlaceholder(field, placeholder);
        }

        section.add(labelComp);
        section.add(Box.createVerticalStrut(3));
        section.add(field);
        section.add(Box.createVerticalStrut(8));

        return field;
    }

    private void applyPlaceholder(JTextField field, String placeholder) {
        field.putClientProperty("placeholder", placeholder);

        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.setText(placeholder);
            field.setForeground(PLACEHOLDER_COLOR);
        }

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(field.getText()) && field.isEditable()) {
                    field.setText("");
                    field.setForeground(NORMAL_TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.isEditable() && field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    private String getPlaceholderForLabel(String label) {
        if (label.contains("Birth Date")) return "MMMM dd, yyyy";
        if (label.contains("Address")) {
            return "House No., Street, Barangay, Municipality, Postal Code, City";
        }
        if (label.contains("Phone No.")) return "09171234567";
        if (label.contains("SSS")) return "12-1234567-1";
        if (label.contains("PhilHealth")) return "12 digits";
        if (label.contains("TIN")) return "123-456-789-000";
        if (label.contains("Pag-IBIG")) return "12 digits";
        if (label.contains("Basic Salary")) return "25000.00";
        if (label.contains("Rice Subsidy")) return "1500.00";
        if (label.contains("Phone Allowance")) return "1000.00";
        if (label.contains("Clothing Allowance")) return "1000.00";
        if (label.contains("Gross Semi-Monthly")) return "12500.00";
        if (label.contains("Hourly Rate")) return "150.75";
        return "";
    }

    private String cleanInput(JTextField field) {
        if (field == null) {
            return "";
        }

        String text = safe(field.getText());
        String placeholder = (String) field.getClientProperty("placeholder");

        if (placeholder != null && placeholder.equals(text)) {
            return "";
        }
        return text;
    }

    public String getEmployeeIdInput() {
        return textOf(employeeNoField);
    }

    public String getFirstNameInput() {
        return textOf(firstNameField);
    }

    public String getLastNameInput() {
        return textOf(lastNameField);
    }

    public String getStatusInput() {
        return textOf(statusField);
    }

    public String getPositionInput() {
        return textOf(positionField);
    }

    public String getSupervisorInput() {
        return textOf(supervisorField);
    }

    public String getRoleInput() {
        return textOf(roleField);
    }

    public String getBirthDateInput() {
        return textOf(birthDateField);
    }

    public String getPhoneInput() {
        return textOf(phoneField);
    }

    public String getAddressInput() {
        return textOf(addressField);
    }

    public String getSssInput() {
        return textOf(sssField);
    }

    public String getPhilHealthInput() {
        return textOf(philHealthField);
    }

    public String getTinInput() {
        return textOf(tinField);
    }

    public String getPagIbigInput() {
        return textOf(pagIbigField);
    }

    public String getBasicSalaryInput() {
        return textOf(basicSalaryField);
    }

    public String getRiceSubsidyInput() {
        return textOf(riceSubsidyField);
    }

    public String getPhoneAllowanceInput() {
        return textOf(phoneAllowanceField);
    }

    public String getClothingAllowanceInput() {
        return textOf(clothingAllowanceField);
    }

    public String getGrossSemiMonthlyInput() {
        return textOf(grossSemiMonthlyField);
    }

    public String getHourlyRateInput() {
        return textOf(hourlyRateField);
    }

    private String textOf(JTextField field) {
        return cleanInput(field);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return new DecimalFormat("#,##0.00").format(value);
    }
}
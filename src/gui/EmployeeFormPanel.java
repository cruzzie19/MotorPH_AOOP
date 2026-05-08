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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EmployeeFormPanel extends JPanel {

    public interface SaveListener {
        void onSave(EmployeeFormPanel panel);
    }

    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
    private static final Color NORMAL_TEXT_COLOR = Color.BLACK;

    private static final String BIRTHDAY_PLACEHOLDER = "MMMM dd, yyyy";
    private static final String ADDRESS_PLACEHOLDER =
            "House No., Street, Barangay, Municipality, Postal Code, City";

    private final Runnable onBack;
    private final JLabel titleLabel = new JLabel();

    private SaveListener saveListener;
    private Employee editingEmployee;

    private final JTextField idField = new JTextField(10);
    private final JTextField lastNameField = new JTextField(18);
    private final JTextField firstNameField = new JTextField(18);
    private final JSpinner birthdaySpinner = new JSpinner(new SpinnerDateModel());
    private final JTextField birthdayTextField = new JTextField(18);
    private final JTextField addressField = new JTextField(22);
    private final JTextField phoneField = new JTextField(14);
    private final JTextField sssField = new JTextField(16);
    private final JTextField philHealthField = new JTextField(16);
    private final JTextField tinField = new JTextField(16);
    private final JTextField pagIbigField = new JTextField(16);
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Regular", "Probationary"});
    private final JTextField positionField = new JTextField(18);
    private final JTextField supervisorField = new JTextField(18);
    private final JTextField basicSalaryField = new JTextField(12);
    private final JTextField riceSubsidyField = new JTextField(12);
    private final JTextField phoneAllowanceField = new JTextField(12);
    private final JTextField clothingAllowanceField = new JTextField(12);
    private final JTextField semiMonthlyRateField = new JTextField(12);
    private final JTextField hourlyRateField = new JTextField(12);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{
            "EXECUTIVE", "HR", "PAYROLL", "ACCOUNTING", "IT", "SALES"
    });

    public EmployeeFormPanel(
            String title,
            Employee employee,
            Runnable onBack
    ) {
        this.editingEmployee = employee;
        this.onBack = onBack;

        configureDateSpinner();
        configureComponents();
        buildLayout(title);
        applyInputPlaceholders();
        setEmployee(employee);
    }

    public void setSaveListener(SaveListener saveListener) {
        this.saveListener = saveListener;
    }

    private void configureDateSpinner() {
        JSpinner.DateEditor editor = new JSpinner.DateEditor(birthdaySpinner, "MM/dd/yyyy");
        birthdaySpinner.setEditor(editor);

        JFormattedTextField spinnerTextField = editor.getTextField();
        spinnerTextField.setFont(INPUT_FONT);
        spinnerTextField.setPreferredSize(new Dimension(220, 36));

        birthdayTextField.setFont(INPUT_FONT);
        birthdayTextField.setPreferredSize(new Dimension(220, 36));
        birthdayTextField.setText(BIRTHDAY_PLACEHOLDER);
        birthdayTextField.setForeground(PLACEHOLDER_COLOR);

        birthdayTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (BIRTHDAY_PLACEHOLDER.equals(birthdayTextField.getText())) {
                    birthdayTextField.setText("");
                    birthdayTextField.setForeground(NORMAL_TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (birthdayTextField.getText() == null || birthdayTextField.getText().trim().isEmpty()) {
                    birthdayTextField.setText(BIRTHDAY_PLACEHOLDER);
                    birthdayTextField.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    private void configureComponents() {
        JTextField[] textFields = {
                idField, lastNameField, firstNameField, addressField, phoneField,
                sssField, philHealthField, tinField, pagIbigField, positionField,
                supervisorField, basicSalaryField, riceSubsidyField, phoneAllowanceField,
                clothingAllowanceField, semiMonthlyRateField, hourlyRateField
        };

        for (JTextField field : textFields) {
            field.setFont(INPUT_FONT);
            field.setPreferredSize(new Dimension(220, 36));
        }

        birthdaySpinner.setFont(INPUT_FONT);
        birthdaySpinner.setPreferredSize(new Dimension(220, 36));

        statusCombo.setFont(INPUT_FONT);
        statusCombo.setPreferredSize(new Dimension(220, 36));

        roleCombo.setFont(INPUT_FONT);
        roleCombo.setPreferredSize(new Dimension(220, 36));
    }

    private void buildLayout(String title) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JButton backButton = new JButton("Back");
        backButton.setFont(BUTTON_FONT);
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(90, 40));
        backButton.addActionListener(e -> onBack.run());

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setText(title);

        header.add(backButton, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addField(form, c, row, "Employee #", idField);
        row = addField(form, c, row, "Last Name", lastNameField);
        row = addField(form, c, row, "First Name", firstNameField);
        row = addBirthdayField(form, c, row, "Birthday", birthdaySpinner, birthdayTextField);
        row = addField(form, c, row, "Address", addressField);
        row = addField(form, c, row, "Phone Number", phoneField);
        row = addField(form, c, row, "SSS #", sssField);
        row = addField(form, c, row, "PhilHealth #", philHealthField);
        row = addField(form, c, row, "TIN #", tinField);
        row = addField(form, c, row, "Pag-IBIG #", pagIbigField);
        row = addField(form, c, row, "Status", statusCombo);
        row = addField(form, c, row, "Position", positionField);
        row = addField(form, c, row, "Immediate Supervisor", supervisorField);
        row = addField(form, c, row, "Basic Salary", basicSalaryField);
        row = addField(form, c, row, "Rice Subsidy", riceSubsidyField);
        row = addField(form, c, row, "Phone Allowance", phoneAllowanceField);
        row = addField(form, c, row, "Clothing Allowance", clothingAllowanceField);
        row = addField(form, c, row, "Gross Semi-monthly Rate", semiMonthlyRateField);
        row = addField(form, c, row, "Hourly Rate", hourlyRateField);
        row = addField(form, c, row, "Role", roleCombo);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton saveBtn = createBlackButton("Save");
        saveBtn.addActionListener(e -> {
            syncBirthdayTextToSpinner();
            if (saveListener != null) {
                saveListener.onSave(this);
            }
        });

        buttonPanel.add(saveBtn);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private int addField(JPanel panel, GridBagConstraints c, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(LABEL_FONT);

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.32;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 0.68;
        panel.add(field, c);

        return row + 1;
    }

    private int addBirthdayField(JPanel panel, GridBagConstraints c, int row, String labelText,
                                 JSpinner spinner, JTextField placeholderField) {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(LABEL_FONT);

        JPanel birthdayPanel = new JPanel(new OverlayLayout(new JPanel()));
        birthdayPanel.setOpaque(false);
        birthdayPanel.setPreferredSize(new Dimension(220, 36));

        spinner.setAlignmentX(0.5f);
        spinner.setAlignmentY(0.5f);
        placeholderField.setAlignmentX(0.5f);
        placeholderField.setAlignmentY(0.5f);

        JPanel layeredPanel = new JPanel();
        layeredPanel.setLayout(new OverlayLayout(layeredPanel));
        layeredPanel.setOpaque(false);
        layeredPanel.setPreferredSize(new Dimension(220, 36));
        layeredPanel.add(placeholderField);
        layeredPanel.add(spinner);

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.32;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 0.68;
        panel.add(layeredPanel, c);

        return row + 1;
    }

    private JButton createBlackButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(110, 40));
        return button;
    }

    private void applyInputPlaceholders() {
        applyPlaceholder(addressField, ADDRESS_PLACEHOLDER);
        applyPlaceholder(phoneField, "09171234567");
        applyPlaceholder(sssField, "12-1234567-1");
        applyPlaceholder(philHealthField, "12 digits");
        applyPlaceholder(tinField, "123-456-789-000");
        applyPlaceholder(pagIbigField, "12 digits");

        applyPlaceholder(basicSalaryField, "25000.00");
        applyPlaceholder(riceSubsidyField, "1500.00");
        applyPlaceholder(phoneAllowanceField, "1000.00");
        applyPlaceholder(clothingAllowanceField, "1000.00");
        applyPlaceholder(semiMonthlyRateField, "12500.00");
        applyPlaceholder(hourlyRateField, "150.75");
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
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(NORMAL_TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    private String cleanInput(JTextField field) {
        String text = field.getText() == null ? "" : field.getText().trim();
        String placeholder = (String) field.getClientProperty("placeholder");

        if (placeholder != null && placeholder.equals(text)) {
            return "";
        }
        return text;
    }

    private void restorePlaceholdersIfNeeded() {
        JTextField[] fields = {
                addressField, phoneField, sssField, philHealthField, tinField, pagIbigField,
                basicSalaryField, riceSubsidyField, phoneAllowanceField,
                clothingAllowanceField, semiMonthlyRateField, hourlyRateField
        };

        for (JTextField field : fields) {
            String placeholder = (String) field.getClientProperty("placeholder");
            if ((field.getText() == null || field.getText().trim().isEmpty()) && placeholder != null) {
                field.setText(placeholder);
                field.setForeground(PLACEHOLDER_COLOR);
            }
        }
    }

    private void showBirthdayPlaceholder() {
        birthdayTextField.setVisible(true);
        birthdayTextField.setText(BIRTHDAY_PLACEHOLDER);
        birthdayTextField.setForeground(PLACEHOLDER_COLOR);
    }

    private void hideBirthdayPlaceholder() {
        birthdayTextField.setVisible(false);
    }

    private void syncBirthdayTextToSpinner() {
        String text = birthdayTextField.getText() == null ? "" : birthdayTextField.getText().trim();

        if (text.isEmpty() || BIRTHDAY_PLACEHOLDER.equals(text)) {
            return;
        }

        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM/dd/yyyy");
            format.setLenient(false);
            Date parsedDate = format.parse(text);
            if (parsedDate != null) {
                birthdaySpinner.setValue(parsedDate);
            }
        } catch (Exception ignored) {
        }
    }

    public void setEmployee(Employee employee) {
        this.editingEmployee = employee;

        if (employee == null) {
            titleLabel.setText("Add Employee");
            clearFields();
            idField.setText("");
            idField.setEditable(false);
            statusCombo.setEnabled(true);
            restorePlaceholdersIfNeeded();
            showBirthdayPlaceholder();
            return;
        }

        titleLabel.setText("Update Employee");
        idField.setText(safeText(employee.getId()));
        idField.setEditable(false);

        lastNameField.setText(safeText(employee.getLastName()));
        lastNameField.setForeground(NORMAL_TEXT_COLOR);

        firstNameField.setText(safeText(employee.getFirstName()));
        firstNameField.setForeground(NORMAL_TEXT_COLOR);

        if (employee.getBirthDate() != null) {
            Date birthDate = Date.from(
                    employee.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            );
            birthdaySpinner.setValue(birthDate);
            birthdayTextField.setText(new java.text.SimpleDateFormat("MM/dd/yyyy").format(birthDate));
            birthdayTextField.setForeground(NORMAL_TEXT_COLOR);
            hideBirthdayPlaceholder();
        } else {
            showBirthdayPlaceholder();
        }

        setActualValue(addressField, safeText(employee.getAddress()));
        setActualValue(phoneField, safeText(employee.getPhone()));
        setActualValue(sssField, safeText(employee.getSssNumber()));
        setActualValue(philHealthField, safeText(employee.getPhilHealthNumber()));
        setActualValue(tinField, safeText(employee.getTinNumber()));
        setActualValue(pagIbigField, safeText(employee.getPagIbigNumber()));

        statusCombo.setSelectedItem(safeText(employee.getStatus()).isEmpty() ? "Regular" : employee.getStatus());

        setActualValue(positionField, safeText(employee.getPosition()));
        setActualValue(supervisorField, safeText(employee.getSupervisor()));
        setActualValue(basicSalaryField, toDisplayText(employee.getBasicSalary()));
        setActualValue(riceSubsidyField, toDisplayText(employee.getRiceSubsidy()));
        setActualValue(phoneAllowanceField, toDisplayText(employee.getPhoneAllowance()));
        setActualValue(clothingAllowanceField, toDisplayText(employee.getClothingAllowance()));
        setActualValue(semiMonthlyRateField, toDisplayText(employee.getGrossSemiMonthlyRate()));
        setActualValue(hourlyRateField, toDisplayText(employee.getHourlyRate()));

        roleCombo.setSelectedItem(employee.getRole() != null ? employee.getRole().getName() : "SALES");
    }

    private void setActualValue(JTextField field, String value) {
        if (value == null || value.trim().isEmpty()) {
            String placeholder = (String) field.getClientProperty("placeholder");
            if (placeholder != null) {
                field.setText(placeholder);
                field.setForeground(PLACEHOLDER_COLOR);
            } else {
                field.setText("");
                field.setForeground(NORMAL_TEXT_COLOR);
            }
        } else {
            field.setText(value.trim());
            field.setForeground(NORMAL_TEXT_COLOR);
        }
    }

    public void setCreateMode() {
        setEmployee(null);
    }

    private void clearFields() {
        JTextField[] textFields = {
                lastNameField, firstNameField, addressField, phoneField, sssField,
                philHealthField, tinField, pagIbigField, positionField, supervisorField,
                basicSalaryField, riceSubsidyField, phoneAllowanceField,
                clothingAllowanceField, semiMonthlyRateField, hourlyRateField
        };

        for (JTextField field : textFields) {
            field.setText("");
            field.setForeground(NORMAL_TEXT_COLOR);
        }

        birthdaySpinner.setValue(new Date());
        birthdayTextField.setText(BIRTHDAY_PLACEHOLDER);
        birthdayTextField.setForeground(PLACEHOLDER_COLOR);
        showBirthdayPlaceholder();

        statusCombo.setSelectedItem("Regular");
        roleCombo.setSelectedItem("SALES");
    }

    public void setEmployeeId(String employeeId) {
        idField.setText(employeeId == null ? "" : employeeId.trim());
    }

    public String getEmployeeIdInput() {
        return idField.getText().trim();
    }

    public String getLastNameInput() {
        return lastNameField.getText().trim();
    }

    public String getFirstNameInput() {
        return firstNameField.getText().trim();
    }

    public LocalDate getBirthDateInput() {
        syncBirthdayTextToSpinner();

        String text = birthdayTextField.getText() == null ? "" : birthdayTextField.getText().trim();
        if (text.isEmpty() || BIRTHDAY_PLACEHOLDER.equals(text)) {
            return null;
        }

        Date date = (Date) birthdaySpinner.getValue();
        return date == null ? null : toLocalDate(date);
    }

    public String getAddressInput() {
        return cleanInput(addressField);
    }

    public String getPhoneInput() {
        return cleanInput(phoneField);
    }

    public String getSssInput() {
        return cleanInput(sssField);
    }

    public String getPhilHealthInput() {
        return cleanInput(philHealthField);
    }

    public String getTinInput() {
        return cleanInput(tinField);
    }

    public String getPagIbigInput() {
        return cleanInput(pagIbigField);
    }

    public String getStatusInput() {
        Object selected = statusCombo.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    public String getPositionInput() {
        return positionField.getText().trim();
    }

    public String getSupervisorInput() {
        return supervisorField.getText().trim();
    }

    public String getBasicSalaryInput() {
        return cleanInput(basicSalaryField);
    }

    public String getRiceSubsidyInput() {
        return cleanInput(riceSubsidyField);
    }

    public String getPhoneAllowanceInput() {
        return cleanInput(phoneAllowanceField);
    }

    public String getClothingAllowanceInput() {
        return cleanInput(clothingAllowanceField);
    }

    public String getSemiMonthlyRateInput() {
        return cleanInput(semiMonthlyRateField);
    }

    public String getHourlyRateInput() {
        return cleanInput(hourlyRateField);
    }

    public String getRoleInput() {
        Object selected = roleCombo.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String toDisplayText(java.math.BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
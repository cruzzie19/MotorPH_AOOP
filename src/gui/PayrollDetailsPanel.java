/**
 *
 * @author Leianna Cruz
 */

package gui;

import service.PayrollComputationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PayrollDetailsPanel extends JPanel {

    public enum Mode {
        GENERATE,
        VIEW
    }

    private static final Color PAGE_BG = new Color(244, 244, 244);
    private static final Color TEXT_DARK = new Color(20, 20, 20);
    private static final Color LINK = new Color(95, 95, 95);
    private static final Color LINK_HOVER = new Color(55, 55, 55);
    private static final Color CARD_BORDER = new Color(200, 200, 200);
    private static final Color FIELD_BORDER = new Color(120, 120, 120);
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_LINK = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.PLAIN, 15);

    private final Mode mode;
    private PayrollComputationService.PayslipResult result;

    private final JLabel backLabel = new JLabel("<html><u>Back</u></html>");
    private final JComboBox<String> monthComboBox = new JComboBox<>();
    private final JButton submitButton = new JButton("Submit");

    private final JTextField employeeNameField = createField();
    private final JTextField employeeIdField = createField();
    private final JTextField positionField = createField();
    private final JTextField employeeTypeField = createField();

    private final JTextField hourlyRateField = createField();
    private final JTextField hoursWorkedField = createField();
    private final JTextField basicPayField = createFieldBold();
    private final JTextField riceAllowanceField = createField();
    private final JTextField phoneAllowanceField = createField();
    private final JTextField clothingAllowanceField = createField();
    private final JTextField grossPayField = createFieldBold();

    private final JTextField taxField = createField();
    private final JTextField sssField = createField();
    private final JTextField philHealthField = createField();
    private final JTextField pagIbigField = createField();
    private final JTextField totalDeductionsField = createFieldBold();
    private final JTextField netPayField = createFieldBold();

    private YearMonth monthStart = YearMonth.of(2024, 6);

    private final JLabel periodLabel = new JLabel("For the Period of", SwingConstants.CENTER);

    private Runnable backAction;
    private Runnable submitAction;

    public PayrollDetailsPanel(Mode mode) {
        this.mode = mode == null ? Mode.VIEW : mode;

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(28, 34, 28, 34));

        add(buildMainContent(), BorderLayout.CENTER);

        wireEvents();
        configureMonthCombo();
        applyMode();
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(buildCenterArea(), BorderLayout.CENTER);
        main.add(buildBottomBar(), BorderLayout.SOUTH);

        return main;
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        backLabel.setFont(FONT_LINK);
        backLabel.setForeground(LINK);
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(backLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        monthComboBox.setPreferredSize(new Dimension(150, 32));
        monthComboBox.setFont(FONT_NORMAL);

        right.add(monthComboBox);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        return topBar;
    }

    private JPanel buildCenterArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PAGE_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(36, 48, 40, 48)
        ));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildPayslipHeader());
        content.add(Box.createVerticalStrut(28));
        content.add(buildFormGrid());

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildPayslipHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel companyLabel = new JLabel("MotorPH");
        companyLabel.setFont(FONT_HEADER);
        companyLabel.setForeground(TEXT_DARK);
        companyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel address1 = new JLabel("7 Jupiter Avenue cor. F. Sandoval Jr.,");
        address1.setFont(FONT_SMALL);
        address1.setForeground(TEXT_DARK);
        address1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel address2 = new JLabel("Bagong Nayon, Quezon City");
        address2.setFont(FONT_SMALL);
        address2.setForeground(TEXT_DARK);
        address2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel payslipLabel = new JLabel("Payslip");
        payslipLabel.setFont(FONT_NORMAL);
        payslipLabel.setForeground(TEXT_DARK);
        payslipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        periodLabel.setFont(FONT_NORMAL);
        periodLabel.setForeground(TEXT_DARK);
        periodLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(companyLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(address1);
        header.add(address2);
        header.add(Box.createVerticalStrut(14));
        header.add(payslipLabel);
        header.add(periodLabel);

        return header;
    }

    private JPanel buildFormGrid() {
        JPanel formRow = new JPanel(new GridLayout(1, 2, 70, 0));
        formRow.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));

        leftColumn.add(createFormRow("Employee Name", employeeNameField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Employee ID", employeeIdField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Position", positionField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Employee Type", employeeTypeField, false));
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(createFormRow("Hourly Rate", hourlyRateField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Hours Worked", hoursWorkedField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Basic Pay", basicPayField, true));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Rice Allowance", riceAllowanceField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Phone Allowance", phoneAllowanceField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Clothing Allowance", clothingAllowanceField, false));
        leftColumn.add(Box.createVerticalStrut(8));
        leftColumn.add(createFormRow("Gross Pay", grossPayField, true));

        rightColumn.add(createSectionLabel("Less: Deductions"));
        rightColumn.add(Box.createVerticalStrut(10));
        rightColumn.add(createFormRow("Withholding Tax", taxField, false));
        rightColumn.add(Box.createVerticalStrut(8));
        rightColumn.add(createFormRow("SSS", sssField, false));
        rightColumn.add(Box.createVerticalStrut(8));
        rightColumn.add(createFormRow("PhilHealth", philHealthField, false));
        rightColumn.add(Box.createVerticalStrut(8));
        rightColumn.add(createFormRow("Pag-IBIG", pagIbigField, false));
        rightColumn.add(Box.createVerticalStrut(8));
        rightColumn.add(createFormRow("Total Deductions", totalDeductionsField, true));
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(createFormRow("Net Pay", netPayField, true));

        formRow.add(leftColumn);
        formRow.add(rightColumn);

        return formRow;
    }

    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(18, 0, 0, 0));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        submitButton.setFont(FONT_BUTTON);
        submitButton.setForeground(WHITE);
        submitButton.setBackground(BLACK);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(12, 34, 12, 34));
        submitButton.setPreferredSize(new Dimension(126, 46));

        right.add(submitButton);
        bottom.add(right, BorderLayout.EAST);

        return bottom;
    }

    private JPanel createFormRow(String labelText, JTextField field, boolean boldLabel) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_DARK);
        label.setFont(boldLabel ? FONT_SMALL_BOLD : FONT_SMALL);
        label.setPreferredSize(new Dimension(140, 22));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(label);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(field, BorderLayout.CENTER);
        center.setPreferredSize(new Dimension(180, 22));

        row.add(left, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);

        return row;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setFont(FONT_SMALL);
        field.setForeground(TEXT_DARK);
        field.setBackground(WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));
        field.setPreferredSize(new Dimension(180, 22));
        return field;
    }

    private JTextField createFieldBold() {
        JTextField field = createField();
        field.setFont(FONT_SMALL_BOLD);
        return field;
    }

    private void wireEvents() {
        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (backAction != null) {
                    backAction.run();
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backLabel.setForeground(LINK_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backLabel.setForeground(LINK);
            }
        });

        submitButton.addActionListener(e -> {
            if (submitAction != null) {
                submitAction.run();
            }
        });
    }

    private void configureMonthCombo() {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        YearMonth current = YearMonth.now();
        YearMonth cursor = monthStart;

        while (!cursor.isAfter(current)) {
            comboModel.addElement(cursor.format(formatter));
            cursor = cursor.plusMonths(1);
        }

        monthComboBox.setModel(comboModel);

        YearMonth defaultMonth = current.isBefore(monthStart) ? monthStart : current;
        monthComboBox.setSelectedItem(defaultMonth.format(formatter));
        updatePeriodLabel(defaultMonth);
    }

    public void displayPayslip(PayrollComputationService.PayslipResult result) {
        this.result = result;

        if (result == null) {
            clearFields();
            return;
        }

        employeeNameField.setText(safe(result.getEmployeeName()));
        employeeIdField.setText(safe(result.getEmployeeId()));
        positionField.setText(safe(result.getPosition()));
        employeeTypeField.setText(safe(result.getEmployeeType()));

        hourlyRateField.setText(formatMoney(result.getHourlyRate()));

        /*
         * IMPORTANT FIX:
         * Do NOT call hoursWorkedField.setText(...) while the user is typing in GENERATE mode.
         * That causes Swing's "Attempt to mutate in notification" exception because the field's
         * DocumentListener triggers a refresh, and the refresh tries to write back into the same
         * document during notification.
         *
         * In GENERATE mode, the hours worked field is the input source, so we leave the user's
         * current text untouched.
         *
         * In VIEW mode, it is safe to populate it because it is not acting as a live input field.
         */
        if (mode == Mode.VIEW) {
            hoursWorkedField.setText(formatMoney(result.getHoursWorked()));
        }

        basicPayField.setText(formatMoney(result.getBasicPay()));
        riceAllowanceField.setText(formatMoney(result.getRiceSubsidy()));
        phoneAllowanceField.setText(formatMoney(result.getPhoneAllowance()));
        clothingAllowanceField.setText(formatMoney(result.getClothingAllowance()));
        grossPayField.setText(formatMoney(result.getGrossPay()));

        taxField.setText(formatMoney(result.getTax()));
        sssField.setText(formatMoney(result.getSss()));
        philHealthField.setText(formatMoney(result.getPhilHealth()));
        pagIbigField.setText(formatMoney(result.getPagIbig()));
        totalDeductionsField.setText(formatMoney(result.getTotalDeductions()));
        netPayField.setText(formatMoney(result.getNetPay()));

        if (result.getPayrollMonth() != null) {
            updatePeriodLabel(result.getPayrollMonth());
        }

        if (mode == Mode.GENERATE) {
            hoursWorkedField.setEditable(true);
            hoursWorkedField.setFocusable(true);
            hoursWorkedField.setBackground(Color.WHITE);
        }
    }

    private void clearFields() {
        JTextField[] fields = {
                employeeNameField, employeeIdField, positionField, employeeTypeField,
                hourlyRateField, basicPayField, riceAllowanceField,
                phoneAllowanceField, clothingAllowanceField, grossPayField, taxField,
                sssField, philHealthField, pagIbigField, totalDeductionsField, netPayField
        };

        for (JTextField field : fields) {
            field.setText("");
        }

        if (mode != Mode.GENERATE) {
            hoursWorkedField.setText("");
        }
    }

    private void updatePeriodLabel(YearMonth yearMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        periodLabel.setText("For the Period of " + yearMonth.format(formatter));
    }

    private void applyMode() {
        boolean generate = mode == Mode.GENERATE;

        submitButton.setVisible(generate);

        employeeNameField.setEditable(false);
        employeeIdField.setEditable(false);
        positionField.setEditable(false);
        employeeTypeField.setEditable(false);

        hourlyRateField.setEditable(false);
        basicPayField.setEditable(false);
        riceAllowanceField.setEditable(false);
        phoneAllowanceField.setEditable(false);
        clothingAllowanceField.setEditable(false);
        grossPayField.setEditable(false);

        taxField.setEditable(false);
        sssField.setEditable(false);
        philHealthField.setEditable(false);
        pagIbigField.setEditable(false);
        totalDeductionsField.setEditable(false);
        netPayField.setEditable(false);

        hoursWorkedField.setEditable(generate);
        hoursWorkedField.setFocusable(generate);
        hoursWorkedField.setBackground(generate ? Color.WHITE : WHITE);

        monthComboBox.setEnabled(true);
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value == null ? BigDecimal.ZERO : value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    public void setSubmitAction(Runnable submitAction) {
        this.submitAction = submitAction;
    }

    public JComboBox<String> getMonthComboBox() {
        return monthComboBox;
    }

    public String getSelectedMonthText() {
        Object selected = monthComboBox.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    public void setMonthStart(YearMonth monthStart) {
        if (monthStart != null) {
            this.monthStart = monthStart;
        }
        configureMonthCombo();
    }

    public String getHoursWorkedInput() {
        return hoursWorkedField.getText() == null
                ? ""
                : hoursWorkedField.getText().trim().replace(",", "");
    }

    public void addHoursWorkedChangeListener(Runnable listener) {
        if (listener == null) {
            return;
        }

        hoursWorkedField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }
        });
    }
}
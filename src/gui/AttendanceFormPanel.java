/**
 *
 * @author Leianna Cruz
 */

package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class AttendanceFormPanel extends JPanel {

    private final JLabel lblBack = new JLabel("<html><u>Back</u></html>");
    private final JButton btnSubmit = new JButton("Submit");

    private final JTextField txtEmployeeId = new JTextField();
    private final JTextField txtDate = new JTextField();
    private final JTextField txtTimeIn = new JTextField();
    private final JTextField txtTimeOut = new JTextField();

    private ActionListener backListener;

    public AttendanceFormPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 32, 24, 32));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildFormArea(), BorderLayout.CENTER);
        root.add(buildBottomArea(), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);

        styleComponents();
        wireEvents();
        applyInputGuides();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 24, 0));

        lblBack.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblBack.setForeground(new Color(95, 95, 95));
        lblBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        top.add(lblBack);
        return top;
    }

    private JPanel buildFormArea() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JPanel leftColumn = buildLeftColumn();
        JPanel rightColumn = buildRightColumn();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 36);
        form.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(rightColumn, gbc);

        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1;
        wrapGbc.weighty = 1;
        wrapGbc.fill = GridBagConstraints.BOTH;

        wrapper.add(form, wrapGbc);
        return wrapper;
    }

    private JPanel buildLeftColumn() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(createFieldBlock("Employee ID", txtEmployeeId));
        left.add(Box.createVerticalStrut(22));
        left.add(createFieldBlock("Date (MM/dd/yyyy)", txtDate));

        return left;
    }

    private JPanel buildRightColumn() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(createFieldBlock("Time In (H:mm)", txtTimeIn));
        right.add(Box.createVerticalStrut(22));
        right.add(createFieldBlock("Time Out (H:mm)", txtTimeOut));

        return right;
    }

    private JPanel buildBottomArea() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(220, 0, 0, 0));
        bottom.add(btnSubmit);
        return bottom;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setOpaque(false);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(new Color(20, 20, 20));

        block.add(label, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);

        return block;
    }

    private void styleComponents() {
        Color borderColor = new Color(115, 115, 115);

        styleTextField(txtEmployeeId, borderColor);
        styleTextField(txtDate, borderColor);
        styleTextField(txtTimeIn, borderColor);
        styleTextField(txtTimeOut, borderColor);

        btnSubmit.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(Color.BLACK);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorder(BorderFactory.createEmptyBorder());
        btnSubmit.setPreferredSize(new Dimension(126, 46));
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTextField(JTextField field, Color borderColor) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(100, 54));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(25, 25, 25));
        field.setCaretColor(new Color(25, 25, 25));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(0, 14, 0, 14)
        ));
    }

    private void wireEvents() {
        lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (backListener != null) {
                    backListener.actionPerformed(
                            new java.awt.event.ActionEvent(
                                    AttendanceFormPanel.this,
                                    java.awt.event.ActionEvent.ACTION_PERFORMED,
                                    "back"
                            )
                    );
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblBack.setForeground(new Color(55, 55, 55));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblBack.setForeground(new Color(95, 95, 95));
            }
        });
    }

    private void applyInputGuides() {
        txtEmployeeId.setToolTipText("Enter the employee ID, e.g. 10001");
        txtDate.setToolTipText("Enter date using MM/dd/yyyy format");
        txtTimeIn.setToolTipText("Enter time-in using 24-hour format, e.g. 8:05 or 08:05");
        txtTimeOut.setToolTipText("Enter time-out using 24-hour format, e.g. 17:30");
    }

    public void setAttendanceData(String employeeId, String date, String timeIn, String timeOut) {
        txtEmployeeId.setText(employeeId == null ? "" : employeeId);
        txtDate.setText(date == null ? "" : date);
        txtTimeIn.setText(timeIn == null ? "" : timeIn);
        txtTimeOut.setText(timeOut == null ? "" : timeOut);
    }

    public void setEditableFields(boolean employeeIdEditable, boolean dateEditable,
                                  boolean timeInEditable, boolean timeOutEditable) {
        txtEmployeeId.setEditable(employeeIdEditable);
        txtDate.setEditable(dateEditable);
        txtTimeIn.setEditable(timeInEditable);
        txtTimeOut.setEditable(timeOutEditable);
    }

    public String getEmployeeId() {
        return txtEmployeeId.getText().trim();
    }

    public String getDate() {
        return txtDate.getText().trim();
    }

    public String getTimeIn() {
        return txtTimeIn.getText().trim();
    }

    public String getTimeOut() {
        return txtTimeOut.getText().trim();
    }

    public void addBackListener(ActionListener listener) {
        this.backListener = listener;
    }

    public void addSubmitListener(ActionListener listener) {
        btnSubmit.addActionListener(listener);
    }
}
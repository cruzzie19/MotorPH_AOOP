/**
 *
 * @author Leianna Cruz
 */

package gui;

import com.toedter.calendar.JDateChooser;
import model.Leave;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LeaveFormPanel extends JPanel {

    private final JLabel lblBack = new JLabel("<html><u>Back</u></html>");
    private final JButton btnSubmit = new JButton("Submit");

    private final JTextField txtEmployeeId = new JTextField();

    private final JDateChooser dcStartDate = new JDateChooser();
    private final JDateChooser dcEndDate = new JDateChooser();

    private final JComboBox<String> cmbLeaveType = new JComboBox<>(
            new String[]{"Vacation Leave", "Sick Leave", "Emergency Leave", "Personal Leave", "Other"}
    );

    private final JTextArea txtNotes = new JTextArea(7, 20);

    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"Pending", "Approved", "Rejected"}
    );

    private java.awt.event.ActionListener backListener;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private boolean hrReviewMode = false;
    private boolean managerEntryMode = false;

    public LeaveFormPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 32, 24, 32));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildFormArea(), BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);

        styleComponents();
        wireEvents();
        
        cmbStatus.setSelectedItem("Pending");
        cmbStatus.setEnabled(false);
        txtEmployeeId.setEditable(false);
        txtEmployeeId.setEnabled(false);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 22, 0));

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
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JPanel leftColumn = buildLeftColumn();
        JPanel rightColumn = buildRightColumn();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 34);
        form.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(rightColumn, gbc);

        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1;
        wrapGbc.weighty = 1;
        wrapGbc.fill = GridBagConstraints.BOTH;
        wrapGbc.anchor = GridBagConstraints.NORTHWEST;

        wrapper.add(form, wrapGbc);
        return wrapper;
    }

    private JPanel buildLeftColumn() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(createFieldBlock("Employee ID", txtEmployeeId));
        left.add(Box.createVerticalStrut(22));
        left.add(createFieldBlock("Start Date", dcStartDate));
        left.add(Box.createVerticalStrut(22));
        left.add(createFieldBlock("End Date", dcEndDate));

        return left;
    }

    private JPanel buildRightColumn() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(createFieldBlock("Leave Type", cmbLeaveType));
        right.add(Box.createVerticalStrut(22));
        right.add(createNotesBlock());
        right.add(Box.createVerticalStrut(22));
        right.add(createFieldBlock("Status", cmbStatus));
        right.add(Box.createVerticalStrut(22));
        right.add(createSubmitRow());

        return right;
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

    private JPanel createNotesBlock() {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setOpaque(false);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        JLabel label = new JLabel("Notes");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(new Color(20, 20, 20));

        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtNotes.setBackground(Color.WHITE);
        txtNotes.setForeground(new Color(25, 25, 25));
        txtNotes.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(txtNotes);
        scroll.setPreferredSize(new Dimension(100, 168));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 168));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 1));
        scroll.getViewport().setBackground(Color.WHITE);

        block.add(label, BorderLayout.NORTH);
        block.add(scroll, BorderLayout.CENTER);

        return block;
    }

    private JPanel createSubmitRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        row.add(btnSubmit);
        return row;
    }

    private void styleComponents() {
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Color borderColor = new Color(115, 115, 115);
        Dimension fieldSize = new Dimension(100, 54);

        txtEmployeeId.setFont(fieldFont);
        txtEmployeeId.setPreferredSize(fieldSize);
        txtEmployeeId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        txtEmployeeId.setBackground(Color.WHITE);
        txtEmployeeId.setForeground(new Color(25, 25, 25));
        txtEmployeeId.setCaretColor(new Color(25, 25, 25));
        txtEmployeeId.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(0, 14, 0, 14)
        ));

        styleDateChooser(dcStartDate, fieldFont, borderColor, fieldSize);
        styleDateChooser(dcEndDate, fieldFont, borderColor, fieldSize);

        cmbLeaveType.setFont(fieldFont);
        cmbLeaveType.setPreferredSize(fieldSize);
        cmbLeaveType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        cmbLeaveType.setBackground(Color.WHITE);
        cmbLeaveType.setForeground(new Color(25, 25, 25));
        cmbLeaveType.setOpaque(true);
        cmbLeaveType.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        cmbStatus.setFont(fieldFont);
        cmbStatus.setPreferredSize(fieldSize);
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, fieldSize.height));
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setForeground(new Color(25, 25, 25));
        cmbStatus.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        btnSubmit.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(Color.BLACK);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorder(BorderFactory.createEmptyBorder());
        btnSubmit.setPreferredSize(new Dimension(126, 46));
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleDateChooser(JDateChooser chooser, Font font, Color borderColor, Dimension size) {
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setPreferredSize(size);
        chooser.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        chooser.setBackground(Color.WHITE);
        chooser.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JTextField editor = (JTextField) chooser.getDateEditor().getUiComponent();
        editor.setFont(font);
        editor.setBackground(Color.WHITE);
        editor.setForeground(new Color(25, 25, 25));
        editor.setCaretColor(new Color(25, 25, 25));
        editor.setBorder(new EmptyBorder(0, 14, 0, 14));
        editor.setEditable(false);
    }

    private void wireEvents() {
        lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (backListener != null) {
                    backListener.actionPerformed(
                            new java.awt.event.ActionEvent(
                                    LeaveFormPanel.this,
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

    public void setFormMode(boolean editMode) {
        btnSubmit.setText(editMode ? "Update" : "Submit");
    }

    public void setLeaveData(Leave leave) {
        if (leave == null) {
            return;
        }

        txtEmployeeId.setText(leave.getEmployeeId() == null ? "" : leave.getEmployeeId());

        dcStartDate.setDate(parseDate(leave.getStartDate()));
        dcEndDate.setDate(parseDate(leave.getEndDate()));

        cmbLeaveType.setSelectedItem(
                leave.getLeaveType() == null || leave.getLeaveType().trim().isEmpty()
                        ? "Vacation Leave"
                        : leave.getLeaveType()
        );

        txtNotes.setText(leave.getNotes() == null ? "" : leave.getNotes());

        String status = leave.getStatus() == null || leave.getStatus().trim().isEmpty()
                ? "Pending"
                : leave.getStatus();

        cmbStatus.setSelectedItem(status);

        if (!hrReviewMode) {
            cmbStatus.setSelectedItem("Pending");
        }

        cmbStatus.setEnabled(hrReviewMode);
    }

    private Date parseDate(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return DATE_FORMAT.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public void clearForm() {
        txtEmployeeId.setText("");
        dcStartDate.setDate(null);
        dcEndDate.setDate(null);
        cmbLeaveType.setSelectedIndex(0);
        txtNotes.setText("");
        cmbStatus.setSelectedItem("Pending");
        cmbStatus.setEnabled(false);
        hrReviewMode = false;
        managerEntryMode = false;
        txtEmployeeId.setEditable(false);
        txtEmployeeId.setEnabled(false);
    }

    public void fillLeave(Leave leave) {
        if (leave == null) {
            return;
        }

        if (txtEmployeeId.isEnabled()) {
            leave.setEmployeeId(txtEmployeeId.getText().trim());
        }

        leave.setStartDate(formatDate(dcStartDate.getDate()));
        leave.setEndDate(formatDate(dcEndDate.getDate()));
        leave.setLeaveType((String) cmbLeaveType.getSelectedItem());
        leave.setNotes(txtNotes.getText().trim());

        if (cmbStatus.isEnabled()) {
            leave.setStatus(String.valueOf(cmbStatus.getSelectedItem()));
        } else {
            leave.setStatus("Pending");
        }
    }

    private String formatDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    public void addBackListener(java.awt.event.ActionListener listener) {
        this.backListener = listener;
    }

    public void addSubmitListener(java.awt.event.ActionListener listener) {
        btnSubmit.addActionListener(listener);
    }

    public void setHrReviewMode(boolean hrReviewMode) {
        this.hrReviewMode = hrReviewMode;

        dcStartDate.setEnabled(!hrReviewMode);
        dcEndDate.setEnabled(!hrReviewMode);
        cmbLeaveType.setEnabled(!hrReviewMode);
        txtNotes.setEditable(!hrReviewMode);
        txtNotes.setEnabled(true);

        cmbStatus.setEnabled(hrReviewMode);

        if (!hrReviewMode) {
            cmbStatus.setSelectedItem("Pending");
        }

        if (!managerEntryMode) {
            txtEmployeeId.setEditable(false);
            txtEmployeeId.setEnabled(false);
        }

        btnSubmit.setText("Submit");
    }

    public void setManagerEntryMode(boolean managerEntryMode) {
        this.managerEntryMode = managerEntryMode;

        txtEmployeeId.setEditable(managerEntryMode);
        txtEmployeeId.setEnabled(managerEntryMode);

        if (!hrReviewMode) {
            cmbStatus.setEnabled(false);
            cmbStatus.setSelectedItem("Pending");
        }
    }

    public void setEmployeeFieldEditable(boolean editable) {
        txtEmployeeId.setEditable(editable);
        txtEmployeeId.setEnabled(editable);
    }

    public void setEmployeeIdValue(String employeeId) {
        txtEmployeeId.setText(employeeId == null ? "" : employeeId);
    }
   
}
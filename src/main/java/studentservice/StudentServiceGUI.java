package studentservice;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * The main window of the Student Service Management System.
 * <p>
 * Layout:
 * <ul>
 *   <li>Left: form for entering a new service request</li>
 *   <li>Right: table of submitted requests</li>
 * </ul>
 * All business rules live in {@link StudentServiceSystem}; this class only
 * handles user interaction.
 */
public class StudentServiceGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String[] TABLE_COLUMNS =
            {"Request ID", "Student ID", "Student Name", "Service Type", "Status", "Created"};

    private static final Color ERROR_COLOUR = new Color(200, 40, 40);

    private final transient StudentServiceSystem system;

    // ----- Form components -----
    private final JTextField studentIdField = new JTextField(18);
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField contactField = new JTextField(18);
    private final JComboBox<ServiceType> serviceTypeCombo = new JComboBox<>();
    private final JTextArea descriptionArea = new JTextArea(6, 18);
    private final JLabel descriptionCounter = new JLabel();
    private transient Border defaultFieldBorder;

    // ----- Table -----
    private final DefaultTableModel tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // the table is for viewing only
        }
    };
    private final JTable requestTable = new JTable(tableModel);

    public StudentServiceGUI(StudentServiceSystem system) {
        super("Student Service Management System");
        this.system = system;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout(8, 8));
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildRequestsPanel(), BorderLayout.CENTER);

        refreshTable(system.getAllRequests());

        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
    }

    // ==================================================================
    // Building the window
    // ==================================================================

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 0),
                BorderFactory.createTitledBorder("New Service Request")));

        // Service type drop-down with an empty "please select" first option.
        serviceTypeCombo.addItem(null);
        for (ServiceType type : ServiceType.values()) {
            serviceTypeCombo.addItem(type);
        }
        serviceTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Select service type --");
                }
                return this;
            }
        });

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDescriptionCounter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDescriptionCounter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDescriptionCounter();
            }
        });
        updateDescriptionCounter();

        studentIdField.setToolTipText("6-10 letters or digits, e.g. S1234567");
        nameField.setToolTipText("Letters, spaces, hyphens and apostrophes only");
        emailField.setToolTipText("e.g. name@example.com");
        contactField.setToolTipText("8-15 digits, e.g. 0412 345 678 or +61412345678");
        descriptionArea.setToolTipText("10-500 characters");
        defaultFieldBorder = studentIdField.getBorder();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(panel, c, row++, "Student ID *", studentIdField);
        addFormRow(panel, c, row++, "Full Name *", nameField);
        addFormRow(panel, c, row++, "Email *", emailField);
        addFormRow(panel, c, row++, "Contact No. *", contactField);
        addFormRow(panel, c, row++, "Service Type *", serviceTypeCombo);

        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Description *"), c);
        c.gridx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(descriptionArea), c);
        row++;

        c.gridy = row++;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        panel.add(descriptionCounter, c);

        JButton submitButton = new JButton("Submit Request");
        submitButton.setMnemonic(KeyEvent.VK_S);
        submitButton.addActionListener(e -> handleSubmit());
        JButton clearButton = new JButton("Clear Form");
        clearButton.setMnemonic(KeyEvent.VK_C);
        clearButton.addActionListener(e -> clearForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.add(clearButton);
        buttons.add(submitButton);

        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        panel.add(buttons, c);

        c.gridy = row;
        JLabel requiredNote = new JLabel("* required field");
        requiredNote.setFont(requiredNote.getFont().deriveFont(Font.ITALIC, 11f));
        c.anchor = GridBagConstraints.WEST;
        panel.add(requiredNote, c);

        getRootPane().setDefaultButton(submitButton);
        panel.setPreferredSize(new Dimension(380, 0));
        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints c, int row, String label,
                            JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private JPanel buildRequestsPanel() {
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(24);
        requestTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(requestTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Service Requests"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================
    // Event handlers
    // ==================================================================

    /** Validates the form, creates a request and refreshes the table. */
    private void handleSubmit() {
        resetFieldHighlights();
        ServiceType selectedType = (ServiceType) serviceTypeCombo.getSelectedItem();
        try {
            ServiceRequest request = system.submitRequest(
                    studentIdField.getText(), nameField.getText(), emailField.getText(),
                    contactField.getText(), selectedType, descriptionArea.getText());

            refreshTable(system.getAllRequests());
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Service request submitted successfully.\n\nRequest ID: " + request.getRequestId()
                            + "\nStatus: " + request.getStatus(),
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            highlightInvalidFields(selectedType);
            JOptionPane.showMessageDialog(this,
                    "Please fix the following:\n\n- " + String.join("\n- ", ex.getErrors()),
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==================================================================
    // Helpers
    // ==================================================================

    private void refreshTable(List<ServiceRequest> requests) {
        tableModel.setRowCount(0);
        for (ServiceRequest r : requests) {
            tableModel.addRow(new Object[]{
                    r.getRequestId(),
                    r.getStudent().getStudentId(),
                    r.getStudent().getName(),
                    r.getServiceType(),
                    r.getStatus(),
                    r.getCreatedAt().format(ServiceRequest.DATE_FORMAT)
            });
        }
    }

    private void updateDescriptionCounter() {
        int length = descriptionArea.getText().trim().length();
        descriptionCounter.setText(length + " / " + InputValidator.DESCRIPTION_MAX_LENGTH + " characters");
        boolean valid = length == 0 || InputValidator.isValidDescription(descriptionArea.getText());
        descriptionCounter.setForeground(valid ? Color.DARK_GRAY : ERROR_COLOUR);
    }

    private void clearForm() {
        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        serviceTypeCombo.setSelectedIndex(0);
        descriptionArea.setText("");
        resetFieldHighlights();
        studentIdField.requestFocusInWindow();
    }

    /** Puts a red border around every field whose value is invalid. */
    private void highlightInvalidFields(ServiceType selectedType) {
        Border errorBorder = BorderFactory.createLineBorder(ERROR_COLOUR, 2);
        if (!InputValidator.isValidStudentId(studentIdField.getText())) {
            studentIdField.setBorder(errorBorder);
        }
        if (!InputValidator.isValidName(nameField.getText())) {
            nameField.setBorder(errorBorder);
        }
        if (!InputValidator.isValidEmail(emailField.getText())) {
            emailField.setBorder(errorBorder);
        }
        if (!InputValidator.isValidContactNumber(contactField.getText())) {
            contactField.setBorder(errorBorder);
        }
        if (selectedType == null) {
            serviceTypeCombo.setBorder(errorBorder);
        }
        if (!InputValidator.isValidDescription(descriptionArea.getText())) {
            descriptionArea.setBorder(errorBorder);
        }
    }

    private void resetFieldHighlights() {
        studentIdField.setBorder(defaultFieldBorder);
        nameField.setBorder(defaultFieldBorder);
        emailField.setBorder(defaultFieldBorder);
        contactField.setBorder(defaultFieldBorder);
        serviceTypeCombo.setBorder(null);
        descriptionArea.setBorder(null);
    }
}

package studentservice;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
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
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The main window of the Student Service Management System.
 * <p>
 * Layout:
 * <ul>
 *   <li>Left: form for entering a new service request</li>
 *   <li>Right: search bar, table of requests, status update controls and request details</li>
 *   <li>Bottom: status bar showing request counts and the last action</li>
 * </ul>
 * All business rules live in {@link StudentServiceSystem}; this class only
 * handles user interaction.
 */
public class StudentServiceGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Path DATA_FILE = Paths.get("service_requests.txt");
    private static final String SEARCH_BY_REQUEST_ID = "Request ID";
    private static final String SEARCH_BY_STUDENT_ID = "Student ID";
    private static final String[] TABLE_COLUMNS =
            {"Request ID", "Student ID", "Student Name", "Service Type", "Status", "Created"};
    private static final int COL_REQUEST_ID = 0;
    private static final int COL_STATUS = 4;

    private static final Color ERROR_COLOUR = new Color(200, 40, 40);
    private static final Color PENDING_COLOUR = new Color(255, 236, 179);
    private static final Color IN_PROGRESS_COLOUR = new Color(187, 222, 251);
    private static final Color RESOLVED_COLOUR = new Color(200, 230, 201);

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

    // ----- Search / table / update components -----
    private final JComboBox<String> searchByCombo =
            new JComboBox<>(new String[]{SEARCH_BY_REQUEST_ID, SEARCH_BY_STUDENT_ID});
    /** Status filter; the first (null) item means "all statuses". */
    private final JComboBox<RequestStatus> statusFilterCombo = new JComboBox<>();
    private final JTextField searchField = new JTextField(14);
    private final DefaultTableModel tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // the table is for viewing only
        }
    };
    private final JTable requestTable = new JTable(tableModel);
    private final JComboBox<RequestStatus> statusCombo = new JComboBox<>(RequestStatus.values());
    private final JButton updateStatusButton = new JButton("Update Status");
    private final JLabel selectedRequestLabel = new JLabel("No request selected");
    private final JTextArea detailsArea = new JTextArea(8, 40);

    // ----- Status bar -----
    private final JLabel summaryLabel = new JLabel();
    private final JLabel messageLabel = new JLabel(" ");

    public StudentServiceGUI(StudentServiceSystem system) {
        super("Student Service Management System");
        this.system = system;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout(8, 8));
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildRequestsPanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        loadData();
        refreshTable(system.getAllRequests());

        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
    }

    // ==================================================================
    // Building the window
    // ==================================================================

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveData(true));
        JMenuItem reloadItem = new JMenuItem("Reload from file");
        reloadItem.addActionListener(e -> {
            loadData();
            refreshTable(system.getAllRequests());
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(saveItem);
        fileMenu.add(reloadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Student Service Management System\nVersion 1.0\n\n"
                        + "Record, track, search and update student service requests.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

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
        // --- Search bar ---
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchField.addActionListener(e -> handleSearch()); // Enter key searches
        searchButton.setMnemonic(KeyEvent.VK_R);
        JButton showAllButton = new JButton("Show All");
        showAllButton.setMnemonic(KeyEvent.VK_A);
        showAllButton.addActionListener(e -> showAllRequests());

        statusFilterCombo.addItem(null);
        for (RequestStatus status : RequestStatus.values()) {
            statusFilterCombo.addItem(status);
        }
        statusFilterCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("All statuses");
                }
                return this;
            }
        });
        statusFilterCombo.setToolTipText("Show only requests with this status");
        statusFilterCombo.addActionListener(e -> handleFilter());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchByCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        searchPanel.add(Box.createHorizontalStrut(16));
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(statusFilterCombo);

        // --- Table ---
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(24);
        requestTable.setAutoCreateRowSorter(true);
        requestTable.getTableHeader().setReorderingAllowed(false);
        requestTable.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusCellRenderer());
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedRequestDetails();
            }
        });

        // --- Status update controls ---
        updateStatusButton.setEnabled(false);
        updateStatusButton.setMnemonic(KeyEvent.VK_U);
        updateStatusButton.addActionListener(e -> handleUpdateStatus());
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Request Status"));
        updatePanel.add(selectedRequestLabel);
        updatePanel.add(Box.createHorizontalStrut(12));
        updatePanel.add(new JLabel("New status:"));
        updatePanel.add(statusCombo);
        updatePanel.add(updateStatusButton);

        // --- Details ---
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setText("Select a request in the table to see its details.");
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Request Details"));

        JPanel lower = new JPanel(new BorderLayout());
        lower.add(updatePanel, BorderLayout.NORTH);
        lower.add(detailsScroll, BorderLayout.CENTER);

        JScrollPane tableScroll = new JScrollPane(requestTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Service Requests"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, lower);
        split.setResizeWeight(0.55);
        split.setBorder(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 8));
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        bar.add(summaryLabel);
        bar.add(Box.createHorizontalGlue());
        bar.add(messageLabel);
        messageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        return bar;
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

            saveData(false);
            refreshTable(system.getAllRequests());
            selectRequestInTable(request.getRequestId());
            clearForm();
            showMessage("Request " + request.getRequestId() + " submitted.", false);
            JOptionPane.showMessageDialog(this,
                    "Service request submitted successfully.\n\nRequest ID: " + request.getRequestId()
                            + "\nStatus: " + request.getStatus(),
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            highlightInvalidFields(selectedType);
            showMessage("Please correct the highlighted fields.", true);
            JOptionPane.showMessageDialog(this,
                    "Please fix the following:\n\n- " + String.join("\n- ", ex.getErrors()),
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Searches by Request ID or Student ID depending on the drop-down. */
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showMessage("Enter a Request ID or Student ID to search.", true);
            JOptionPane.showMessageDialog(this, "Please enter a value to search for.",
                    "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetFilterSilently();
        List<ServiceRequest> results;
        if (SEARCH_BY_REQUEST_ID.equals(searchByCombo.getSelectedItem())) {
            results = system.findRequestById(query).map(List::of).orElse(List.of());
        } else {
            results = system.findRequestsByStudentId(query);
        }

        refreshTable(results);
        if (results.isEmpty()) {
            showMessage("No requests found for " + searchByCombo.getSelectedItem() + " '" + query + "'.", true);
            JOptionPane.showMessageDialog(this,
                    "No requests found for " + searchByCombo.getSelectedItem() + " '" + query + "'.",
                    "No Results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showMessage(results.size() + " request(s) found.", false);
            requestTable.setRowSelectionInterval(0, 0);
        }
    }

    /** Shows only the requests with the status chosen in the filter drop-down. */
    private void handleFilter() {
        RequestStatus status = (RequestStatus) statusFilterCombo.getSelectedItem();
        searchField.setText("");
        if (status == null) {
            refreshTable(system.getAllRequests());
            showMessage("Showing all " + system.getRequestCount() + " request(s).", false);
            return;
        }
        List<ServiceRequest> results = system.findRequestsByStatus(status);
        refreshTable(results);
        showMessage(results.size() + " request(s) with status " + status + ".", false);
    }

    /** Clears the search box and the status filter, then lists every request. */
    private void showAllRequests() {
        if (statusFilterCombo.getSelectedIndex() == 0) {
            handleFilter(); // already "All statuses": just refresh the list
        } else {
            statusFilterCombo.setSelectedIndex(0); // triggers handleFilter()
        }
    }

    /** Changes the status of the request currently selected in the table. */
    private void handleUpdateStatus() {
        String requestId = getSelectedRequestId();
        if (requestId == null) {
            JOptionPane.showMessageDialog(this, "Please select a request in the table first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RequestStatus newStatus = (RequestStatus) statusCombo.getSelectedItem();
        try {
            boolean changed = system.updateRequestStatus(requestId, newStatus);
            if (!changed) {
                showMessage(requestId + " is already " + newStatus + ".", true);
                JOptionPane.showMessageDialog(this,
                        "Request " + requestId + " already has the status '" + newStatus + "'.",
                        "No Change", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            saveData(false);
            refreshTable(currentlyDisplayedRequests());
            selectRequestInTable(requestId);
            showMessage(requestId + " updated to " + newStatus + ".", false);
        } catch (ValidationException ex) {
            showMessage(ex.getMessage(), true);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================================================================
    // Helpers
    // ==================================================================

    /** Sets the filter back to "All statuses" without re-running the filter. */
    private void resetFilterSilently() {
        ActionListener[] listeners = statusFilterCombo.getActionListeners();
        for (ActionListener listener : listeners) {
            statusFilterCombo.removeActionListener(listener);
        }
        statusFilterCombo.setSelectedIndex(0);
        for (ActionListener listener : listeners) {
            statusFilterCombo.addActionListener(listener);
        }
    }

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
        updateSummary();
        showSelectedRequestDetails();
    }

    /** Returns the requests currently listed in the table (keeps an active search after updates). */
    private List<ServiceRequest> currentlyDisplayedRequests() {
        List<ServiceRequest> shown = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            system.findRequestById((String) tableModel.getValueAt(row, COL_REQUEST_ID))
                    .ifPresent(shown::add);
        }
        return shown;
    }

    private String getSelectedRequestId() {
        int viewRow = requestTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = requestTable.convertRowIndexToModel(viewRow);
        return (String) tableModel.getValueAt(modelRow, COL_REQUEST_ID);
    }

    private void selectRequestInTable(String requestId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (requestId.equals(tableModel.getValueAt(row, COL_REQUEST_ID))) {
                int viewRow = requestTable.convertRowIndexToView(row);
                requestTable.setRowSelectionInterval(viewRow, viewRow);
                requestTable.scrollRectToVisible(requestTable.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void showSelectedRequestDetails() {
        String requestId = getSelectedRequestId();
        Optional<ServiceRequest> request =
                requestId == null ? Optional.empty() : system.findRequestById(requestId);
        if (request.isPresent()) {
            detailsArea.setText(request.get().toDetailedString());
            detailsArea.setCaretPosition(0);
            selectedRequestLabel.setText("Selected: " + requestId);
            statusCombo.setSelectedItem(request.get().getStatus());
            updateStatusButton.setEnabled(true);
        } else {
            detailsArea.setText("Select a request in the table to see its details.");
            selectedRequestLabel.setText("No request selected");
            updateStatusButton.setEnabled(false);
        }
    }

    private void updateSummary() {
        var summary = system.getStatusSummary();
        summaryLabel.setText(String.format("Total: %d   |   Pending: %d   |   In Progress: %d   |   Resolved: %d",
                system.getRequestCount(),
                summary.get(RequestStatus.PENDING),
                summary.get(RequestStatus.IN_PROGRESS),
                summary.get(RequestStatus.RESOLVED)));
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

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setForeground(isError ? ERROR_COLOUR : new Color(30, 110, 50));
    }

    private void loadData() {
        try {
            int skipped = system.loadFromFile(DATA_FILE);
            showMessage("Loaded " + system.getRequestCount() + " request(s) from " + DATA_FILE + ".", false);
            if (skipped > 0) {
                JOptionPane.showMessageDialog(this,
                        skipped + " line(s) in " + DATA_FILE + " were damaged and could not be loaded.",
                        "Data Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            showMessage("Could not load saved data.", true);
            JOptionPane.showMessageDialog(this, "Could not load saved requests:\n" + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Saves all requests; shows a confirmation only when the user chose File > Save. */
    private void saveData(boolean confirm) {
        try {
            system.saveToFile(DATA_FILE);
            if (confirm) {
                showMessage("Saved " + system.getRequestCount() + " request(s).", false);
            }
        } catch (IOException e) {
            showMessage("Save failed.", true);
            JOptionPane.showMessageDialog(this, "Could not save requests:\n" + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this, "Save and exit the application?",
                "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            saveData(false);
            dispose();
            System.exit(0);
        }
    }

    // ==================================================================
    // Table cell renderer that colours the Status column
    // ==================================================================

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected && value instanceof RequestStatus status) {
                switch (status) {
                    case PENDING -> setBackground(PENDING_COLOUR);
                    case IN_PROGRESS -> setBackground(IN_PROGRESS_COLOUR);
                    case RESOLVED -> setBackground(RESOLVED_COLOUR);
                    default -> setBackground(table.getBackground());
                }
            }
            return this;
        }
    }
}

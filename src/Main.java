import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.net.URL;
import java.util.ArrayList;
import java.time.LocalDate;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.util.Date;
import java.util.Calendar;
import java.time.ZoneId;

public class Main {
    public final static boolean runInEclipse = false;
    private final int[]  windowSize = { 1000, 700 }; 
    private final String title      = "Attendance Manager";
    private JFrame frame            = null;
    private JPanel contentPane      = null;

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JPanel centerPanel;
    private Object[] currentSelectedEmployee = null;

    public static DatabaseHandler dbHandler  = null;

    private ArrayList<Object[]> employees = null;
    private JPanel bottomInfo = null;
    private JPanel attendanceCard = null;
    private JTextField searchField = null;

    private Color color1 = new Color(0, 162, 232);
    private Color color2 = new Color(17, 198, 255); 
    private Color color3 = new Color(132, 153, 161); 
    private Color color4 = new Color(172, 199, 209);

    private JButton btnCheckIn = null;
    private JButton btnCheckOut = null;
    private JButton btnSetSchedule = null;
    private JButton btnEditSchedule = null;

    public static Main window = null;
    private static AddEmployee addEmployee = null;
    private static SetShift setShift = null;
    private LocalDate selectedAttendanceDate = LocalDate.now(); 

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try { window = new Main();
            } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
        });
    }

    public Main() throws ClassNotFoundException, SQLException {
        Main.dbHandler = new DatabaseHandler();
        Main.dbHandler.configureSQL();

        Main.addEmployee = new AddEmployee(Main.dbHandler);
        Main.setShift    = new SetShift(Main.dbHandler);
        this.launch();
    }

    private final void launch() {
        this.frame = new JFrame  (this.title);
        this.frame.setLayout     (null);
        this.frame.setResizable  (false);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.contentPane = new JPanel    ();
        this.contentPane.setLayout       (null);
        this.contentPane.setPreferredSize(Utility.createDimension(this.windowSize));
        this.frame.setContentPane        (this.contentPane);

        this.addDesign();

        this.frame.pack                  ();
        this.frame.setVisible            (true);
    }

    private final void addDesign() {
        JPanel wholeLayout = new JPanel(new BorderLayout());
        wholeLayout.setBounds(Utility.createRectangle(windowSize));
        wholeLayout.setOpaque(false);

        JPanel navPanel = createNavigationPanel();
        centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setOpaque(false);

        centerPanel.add(createEmployeeTablePanel());
        centerPanel.add(viewPersonInformation());
        wholeLayout.add(navPanel, BorderLayout.NORTH);
        wholeLayout.add(centerPanel, BorderLayout.CENTER);
        this.contentPane.add(wholeLayout);
    }

    private JPanel createNavigationPanel() {
    JPanel navPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();

            g2d.setColor(color1);
            g2d.fillRect(0, 0, width, height);
        }
    };
    navPanel.setLayout(new BorderLayout());
    navPanel.setPreferredSize(new Dimension(800, 45));
    navPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

    JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    leftSection.setOpaque(false);

    CustomComponents.ImagePanel iconLabel = new CustomComponents.ImagePanel(Utility.addPath("attendance.png"));
    iconLabel.setBounds(0, 0, 30, 30);
    iconLabel.setPreferredSize(new Dimension(30, 30));

    searchField = createRoundedSearchField();
    searchField.getDocument().addDocumentListener(new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { updateModel(); }
        @Override public void removeUpdate(DocumentEvent e) { updateModel(); }
        @Override public void changedUpdate(DocumentEvent e) { updateModel(); }
    });

    leftSection.add(iconLabel);
    leftSection.add(searchField);

    JPanel centerSection = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    centerSection.setOpaque(false);

    JLabel currentDateLabel = new JLabel("Today: " + LocalDate.now());
    JLabel selectedDateLabel = new JLabel("Date: " + LocalDate.now());

    JButton datePickerButton = createStyledButton("Pick Date");
    datePickerButton.addActionListener(e -> {
        LocalDate selectedDate = showDatePickerDialog();
        if (selectedDate != null) {
            selectedAttendanceDate = selectedDate;
            selectedDateLabel.setText("Date: " + selectedDate);
            updateModel();
        }
    });

    centerSection.add(currentDateLabel);
    centerSection.add(selectedDateLabel);
    centerSection.add(datePickerButton);

    JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    rightSection.setOpaque(false);

    JButton btnCheckIn = createStyledButton("ADD EMPLOYEE");
    btnCheckIn.addActionListener(e -> Main.addEmployee.run(null));
    rightSection.add(btnCheckIn);

    navPanel.add(leftSection, BorderLayout.WEST);
    navPanel.add(centerSection, BorderLayout.CENTER);
    navPanel.add(rightSection, BorderLayout.EAST);

    selectedAttendanceDate = LocalDate.now();

    return navPanel;
}

private LocalDate showDatePickerDialog() {
    Date initialDate = Date.from(selectedAttendanceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    SpinnerDateModel model = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);

    JSpinner dateSpinner = new JSpinner(model);
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

    int result = JOptionPane.showConfirmDialog(frame, dateSpinner, "Select Date", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        Date selectedDate = (Date) dateSpinner.getValue();
        return selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    return null;
}




    private JTextField createRoundedSearchField() {
        JTextField searchF = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int arc = 20;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, width, height, arc, arc);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 20;
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };

        searchF.setOpaque(false);
        searchF.setPreferredSize(new Dimension(200, 30));
        searchF.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchF.setToolTipText("Search employee...");
        searchF.setMargin(new Insets(5, 10, 5, 10));

        return searchF;
    }


    private JButton createStyledButton(String text) {

        JButton button = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = 20; 

			if (!isEnabled()) {
                g2.setColor(color3);
            } else if (getModel().isPressed()) {
                g2.setColor(new Color(200, 200, 200));
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(225, 225, 225));
            } else {
                g2.setColor(color2);
            }

            g2.fillRoundRect(0, 0, width, height, arc, arc);
            g2.setColor(new Color(50, 111, 137));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getHeight();
            int textX = (width - textWidth) / 2;
            int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    };
    button.setOpaque(false);
    return button;
}

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int arc = 20; 

                g2.setColor(color4);
                g2.fillRoundRect(0, 0, width, height, arc, arc);
                g2.setColor(new Color(50, 111, 137));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                int textX = (width - textWidth) / 2;
                int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };
        label.setOpaque(false);
        return label;
    }

    public void updateModel() {
        Integer selectedEmployeeId = null;
        if (currentSelectedEmployee != null) {
            selectedEmployeeId = (Integer) currentSelectedEmployee[0];
        }
        tableModel.setRowCount(0);

        employees = dbHandler.getAllEmployees(this.selectedAttendanceDate, this.searchField.getText());
        for (Object[] employee : employees) {
            int id = (int) employee[0];
            String firstName = employee[1].toString();
            String lastName = employee[2].toString();
            String onLeave = employee[3].toString();

            String checkIn = employee[6] != "NA" ? employee[6].toString().substring(11) : "NA";
            String checkOut = employee[7] != "NA" ? employee[7].toString().substring(11) : "NA";

            String shiftStart = employee[4].toString();
            String shiftEnd = employee[5].toString();
            String shiftSchedule = shiftStart + " - " + shiftEnd;

            tableModel.addRow(new Object[]{id, firstName, lastName, onLeave, shiftSchedule, checkIn, checkOut});
        }

        if (selectedEmployeeId != null) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if ((int) tableModel.getValueAt(i, 0) == selectedEmployeeId) {
                    employeeTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }



    private JPanel createEmployeeTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(null);
        panel.setBackground(color3);
        String[] columnNames = {"ID", "First Name", "Last Name", "Status", "Shift", "Check-In", "Check-Out"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.updateModel();

        employeeTable = new JTable(tableModel);
        employeeTable.setFillsViewportHeight(true);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(30);
        employeeTable.setShowGrid(false);
        employeeTable.setIntercellSpacing(new Dimension(0, 0));
        employeeTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        employeeTable.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    currentSelectedEmployee = employees.get(selectedRow);
                } else currentSelectedEmployee = null;
                Main.window.updateBottomInfo();
            }
        });


        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setForeground(Color.WHITE);
        header.setBackground(color1);

        employeeTable.setBackground(Color.WHITE);
        employeeTable.setSelectionBackground(new Color(220, 240, 255));
        employeeTable.setSelectionForeground(Color.BLACK);

        DefaultTableCellRenderer centeredRenderer = new DefaultTableCellRenderer();
        centeredRenderer.setHorizontalAlignment(JLabel.CENTER);
        centeredRenderer.setVerticalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer alternatingRowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                            boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    cell.setBackground(row % 2 == 0 ? new Color(245, 245, 245) : new Color(235, 235, 235));
                } else {
                    cell.setBackground(employeeTable.getSelectionBackground());
                }
                setHorizontalAlignment(JLabel.CENTER); 
                setVerticalAlignment(JLabel.CENTER);
                return cell;
            }
        };

        for (int i = 0; i < employeeTable.getColumnCount(); i++) {
            employeeTable.getColumnModel().getColumn(i).setCellRenderer(alternatingRowRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(new Color(235, 235, 235));
        scrollPane.setOpaque(false);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    private Timestamp parseTimestamp(String dateTime) {
        if (dateTime.contains("T")) {
            dateTime = dateTime.replace("T", " ");
        }
        return Timestamp.valueOf(dateTime);
    }

    private void updateBottomInfo() {
        this.bottomInfo.removeAll();

        if (currentSelectedEmployee == null) {
            this.bottomInfo.add(createProfileLabel("First Name:", "N/A"));
            this.bottomInfo.add(createProfileLabel("Last Name:", "N/A"));
        } else {
            this.bottomInfo.add(createProfileLabel("First Name:", currentSelectedEmployee[1].toString()));
            this.bottomInfo.add(createProfileLabel("Last Name:", currentSelectedEmployee[2].toString()));
        }

        attendanceCard.removeAll();
        String shiftStartStr = (currentSelectedEmployee != null && !"No Shift".equals(currentSelectedEmployee[4]))
                ? currentSelectedEmployee[4].toString()
                : null;
        String shiftEndStr = (currentSelectedEmployee != null && !"No Shift".equals(currentSelectedEmployee[5]))
                ? currentSelectedEmployee[5].toString()
                : null;

        boolean hasShift = shiftStartStr != null && shiftEndStr != null;
        String shiftSchedule = hasShift ? shiftStartStr + " - " + shiftEndStr : "No Shift";

        String checkInStr = (currentSelectedEmployee != null && currentSelectedEmployee[6] != "NA")
                ? currentSelectedEmployee[6].toString()
                : "NA";
        String checkOutStr = (currentSelectedEmployee != null && currentSelectedEmployee[7] != "NA")
                ? currentSelectedEmployee[7].toString()
                : "NA";

        String status = (currentSelectedEmployee != null && currentSelectedEmployee[3] != null)
                ? currentSelectedEmployee[3].toString()
                : "NA";

        String minutesLate = "NA";
        String minutesOvertime = "NA";

        if (hasShift && !"NA".equals(checkInStr)) {
            try {
                Timestamp checkIn = parseTimestamp(checkInStr);

                String today2 = java.time.LocalDate.now().toString();
                Timestamp shiftStart = Timestamp.valueOf(today2 + " " + shiftStartStr);
                Timestamp shiftEnd = Timestamp.valueOf(today2 + " " + shiftEndStr);

                long diffMillisLate = checkIn.getTime() - shiftStart.getTime();

                if (diffMillisLate > 0) {
                    long totalMinutesLate = diffMillisLate / (1000 * 60);
                    long secondsLate = (diffMillisLate / 1000) % 60;
                    long hoursLate = totalMinutesLate / 60;

                    minutesLate = String.format("%d hours, %d minutes, %d seconds", hoursLate, totalMinutesLate % 60, secondsLate);
                } else {
                    minutesLate = "On time or early";
                }

                if (!"NA".equals(checkOutStr)) {
                    Timestamp checkOut = parseTimestamp(checkOutStr);
                    long diffMillisOvertime = checkOut.getTime() - shiftEnd.getTime();

                    if (diffMillisOvertime > 0) {
                        long totalMinutesOvertime = diffMillisOvertime / (1000 * 60);
                        long secondsOvertime = (diffMillisOvertime / 1000) % 60;
                        long hoursOvertime = totalMinutesOvertime / 60;

                        minutesOvertime = String.format("%d hours, %d minutes, %d seconds", hoursOvertime, totalMinutesOvertime % 60, secondsOvertime);
                    } else {
                        minutesOvertime = "0 hours, 0 minutes, 0 seconds";
                    }
                } else {
                    minutesOvertime = "N/A (No Checkout)";
                }
            } catch (Exception e) {
                e.printStackTrace();
                minutesLate = "Error";
                minutesOvertime = "Error";
            }
        }
        String[][] attendanceData = {
                {"Check-In Time:", checkInStr.replace("T", " ")},
                {"Check-Out Time:", checkOutStr.replace("T", " ")},
                {"Shift Schedule:", shiftSchedule},
                {"Minutes Late:", minutesLate},
                {"Minutes Overtime:", minutesOvertime},
                {"Status:", status}
        };

        for (String[] data : attendanceData) {
            attendanceCard.add(createDataLabel(data[0]));
            attendanceCard.add(createStyledLabel(data[1]));
        }

        if (currentSelectedEmployee != null) {
            boolean isActive = status.startsWith("Active");
            boolean canCheckIn = isActive && hasShift && "NA".equals(checkInStr);
            boolean canCheckOut = isActive && hasShift && !"NA".equals(checkInStr) && "NA".equals(checkOutStr);

            btnCheckIn.setEnabled(canCheckIn);
            btnCheckOut.setEnabled(canCheckOut);
            btnSetSchedule.setEnabled("NA".equals(checkInStr));
            btnEditSchedule.setEnabled("NA".equals(checkInStr));
        } else {
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(false);
            btnSetSchedule.setEnabled(false);
            btnEditSchedule.setEnabled(false);
        }

        this.frame.revalidate();
        this.frame.repaint();
    }

    private JPanel viewPersonInformation() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(color4);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel profileCard = new JPanel(new GridLayout(2, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();

                g2d.setColor(color3);
                g2d.fillRoundRect(0, 0, width, height, 20, 20);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 20;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };
        profileCard.setOpaque(false);
        profileCard.setPreferredSize(new Dimension(250, 0));
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        CustomComponents.ImagePanel profilePic = new CustomComponents.ImagePanel(Utility.addPath("profile.png"));
        profilePic.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePic.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        profilePic.setOpaque(false);
        profilePic.setBackground(new Color(240, 240, 240));

        bottomInfo = new JPanel();
        bottomInfo.setOpaque(false);

        profileCard.add(profilePic);
        profileCard.add(bottomInfo);

        attendanceCard = new JPanel(new GridLayout(6, 2, 5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();

                g2d.setColor(color3);
                g2d.fillRoundRect(0, 0, width, height, 20, 20);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 20;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };

        attendanceCard.setOpaque(false);
        attendanceCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        attendanceCard.setPreferredSize(new Dimension(200, 0));

        JPanel actionCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();

                g2d.setColor(color1);
                g2d.fillRoundRect(0, 0, width, height, 20, 20);
            }
        };
        actionCard.setOpaque(false);
        actionCard.setBorder(null);

        btnCheckIn = createStyledButton("CHECK IN");
        btnCheckOut = createStyledButton("CHECK OUT");
        btnSetSchedule = createStyledButton("SET SHIFT");
        btnEditSchedule = createStyledButton("TOGGLE STATUS");

        btnSetSchedule.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                if (currentSelectedEmployee == null) {
                    JOptionPane.showMessageDialog(frame, "No employee selected.", "Error", JOptionPane.ERROR_MESSAGE);
                } else 
                Main.setShift.run(null, (int) currentSelectedEmployee[0]);
            }
        });

        btnEditSchedule.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (currentSelectedEmployee == null) {
                    JOptionPane.showMessageDialog(frame, "No employee selected.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        Main.dbHandler.toggleEmployeeStatus((int) currentSelectedEmployee[0]);
                        JOptionPane.showMessageDialog(frame, "Status updated successfully!");
                        Main.window.updateModel();

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Updating Status failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnCheckIn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (currentSelectedEmployee == null) {
                    JOptionPane.showMessageDialog(frame, "No employee selected.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        Main.dbHandler.checkInEmployee((int) currentSelectedEmployee[0]);
                        JOptionPane.showMessageDialog(frame, "Check-in successful!");
                        Main.window.updateModel();

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Check-in failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnCheckOut.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                if (currentSelectedEmployee == null) {
                    JOptionPane.showMessageDialog(frame, "No employee selected.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        Main.dbHandler.checkOutEmployee((int) currentSelectedEmployee[0]);
                        JOptionPane.showMessageDialog(frame, "Check-out successful!");
                        Main.window.updateModel();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Check-out failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        actionCard.add(btnCheckIn);
        actionCard.add(btnCheckOut);
        actionCard.add(btnSetSchedule);
        actionCard.add(btnEditSchedule);

        panel.add(profileCard, BorderLayout.WEST);
        panel.add(attendanceCard, BorderLayout.CENTER);
        panel.add(actionCard, BorderLayout.SOUTH);

        this.updateBottomInfo();
        return panel;
    }

    private JPanel createProfileLabel(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(lblKey);
        panel.add(lblValue);
        return panel;
    }

    private JLabel createDataValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        return label;
    }

    private JLabel createReadOnlyField(String value) {
        JLabel field = new JLabel(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(250, 250, 250));
        return field;
    }


}
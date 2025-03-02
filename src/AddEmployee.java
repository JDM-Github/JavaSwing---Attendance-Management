import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddEmployee {

	private int[] windowSize = { 280, 220 }; 
	private String title       = "Add Employee";
    private DatabaseHandler dbHandler = null;

	private JFrame frame       = null;
	private JFrame oldFrame    = null;
	private JPanel contentPane = null;
	public  AddEmployee window = null;

	public AddEmployee() throws ClassNotFoundException, SQLException {
		this.launch();
	}
	public AddEmployee(DatabaseHandler handler) {
		try { window = new AddEmployee();
            window.dbHandler = handler;
            window.dbHandler.configureSQL();
		} catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
	}

	public void run(JFrame oldFrame) {
		window.frame.setVisible(true);
		window.oldFrame = oldFrame;
		if (oldFrame != null) oldFrame.dispose();
		else window.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

    private void launch() {
        frame = new JFrame(title);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(windowSize[0], windowSize[1]);
        frame.setLocationRelativeTo(null);

        contentPane = new JPanel(new GridBagLayout());
        contentPane.setPreferredSize(new Dimension(windowSize[0], windowSize[1]));
        contentPane.setBounds(0, 0, windowSize[0], windowSize[1]);
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        frame.setContentPane(contentPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel firstNameLabel = new JLabel("First Name:");
        gbc.gridx = 0; gbc.gridy = 0;
        contentPane.add(firstNameLabel, gbc);

        JTextField firstNameField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0;
        contentPane.add(firstNameField, gbc);

        JLabel lastNameLabel = new JLabel("Last Name:");
        gbc.gridx = 0; gbc.gridy = 1;
        contentPane.add(lastNameLabel, gbc);

        JTextField lastNameField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 1;
        contentPane.add(lastNameField, gbc);

        JLabel onLeaveLabel = new JLabel("On Leave:");
        gbc.gridx = 0; gbc.gridy = 2;
        contentPane.add(onLeaveLabel, gbc);

        JCheckBox onLeaveCheckBox = new JCheckBox();
        gbc.gridx = 1; gbc.gridy = 2;
        contentPane.add(onLeaveCheckBox, gbc);

        JLabel shiftStartLabel = new JLabel("Shift Start:");
        gbc.gridx = 0; gbc.gridy = 3;
        contentPane.add(shiftStartLabel, gbc);

        JSpinner shiftStartSpinner = createTimeSpinner("09:00:00");
        gbc.gridx = 1; gbc.gridy = 3;
        contentPane.add(shiftStartSpinner, gbc);

        // Time Picker for Shift End
        JLabel shiftEndLabel = new JLabel("Shift End:");
        gbc.gridx = 0; gbc.gridy = 4;
        contentPane.add(shiftEndLabel, gbc);

        JSpinner shiftEndSpinner = createTimeSpinner("17:00:00");
        gbc.gridx = 1; gbc.gridy = 4;
        contentPane.add(shiftEndSpinner, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addButton = new JButton("Add Employee");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 5;
        contentPane.add(buttonPanel, gbc);

        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            boolean onLeave = onLeaveCheckBox.isSelected();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Time shiftStart = new Time(((Date) shiftStartSpinner.getValue()).getTime());
                Time shiftEnd = new Time(((Date) shiftEndSpinner.getValue()).getTime());
                
                dbHandler.addEmployee(firstName, lastName, onLeave, shiftStart, shiftEnd);
                JOptionPane.showMessageDialog(frame, "Employee added successfully!");

                if (oldFrame != null) oldFrame.setVisible(true);
                frame.dispose();
                Main.window.updateModel();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to add employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            if (oldFrame != null) oldFrame.setVisible(true);
            frame.dispose();
        });
    }

    private JSpinner createTimeSpinner(String defaultTime) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm:ss");
        spinner.setEditor(editor);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date defaultDate = sdf.parse(defaultTime);
            spinner.setValue(defaultDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spinner;
    }


}
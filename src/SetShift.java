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

public class SetShift {

	private int[] windowSize = { 280, 220 }; 
	private String title       = "Add Employee";
    private DatabaseHandler dbHandler = null;

	private JFrame frame       = null;
	private JFrame oldFrame    = null;
	private JPanel contentPane = null;
	public  SetShift window = null;
    private int employee_id = 0;

	public SetShift() throws ClassNotFoundException, SQLException {
		this.launch();
	}
	public SetShift(DatabaseHandler handler) {
		try { window = new SetShift();
            window.dbHandler = handler;
            window.dbHandler.configureSQL();
		} catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
	}

	public void run(JFrame oldFrame, int employeeId) {
		window.frame.setVisible(true);
        window.employee_id = employeeId;
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

        JLabel shiftStartLabel = new JLabel("Shift Start Time:");
        gbc.gridx = 0; gbc.gridy = 0;
        contentPane.add(shiftStartLabel, gbc);

        JSpinner shiftStartSpinner = createTimeSpinner("09:00:00");
        gbc.gridx = 1; gbc.gridy = 0;
        contentPane.add(shiftStartSpinner, gbc);

        JLabel shiftEndLabel = new JLabel("Shift End Time:");
        gbc.gridx = 0; gbc.gridy = 1;
        contentPane.add(shiftEndLabel, gbc);

        JSpinner shiftEndSpinner = createTimeSpinner("17:00:00");
        gbc.gridx = 1; gbc.gridy = 1;
        contentPane.add(shiftEndSpinner, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveButton = new JButton("Set Shift");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 2;
        contentPane.add(buttonPanel, gbc);

        saveButton.addActionListener(e -> {
            try {
                Time shiftStart = new Time(((Date) shiftStartSpinner.getValue()).getTime());
                Time shiftEnd = new Time(((Date) shiftEndSpinner.getValue()).getTime());

                boolean success = dbHandler.setShiftForEmployee(employee_id, shiftStart, shiftEnd);
                if (success) {
                    JOptionPane.showMessageDialog(frame, "Shift set successfully!");
                    frame.dispose();
                    Main.window.updateModel();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to set shift.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> frame.dispose());

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
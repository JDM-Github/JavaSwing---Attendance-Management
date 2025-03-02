import java.sql.*;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.time.LocalDate;

public class DatabaseHandler {
    public final boolean debugMode = false;

    public String connectionStr    = "jdbc:mysql://localhost:3306/test";
    public String userSQL          = "root";
    public String passwordSQL      = "root"; // USE YOUR OWN PASSWORD

    public Connection connection = null;
    public Statement  statement  = null;

    public void configureSQL() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(this.connectionStr, this.userSQL, this.passwordSQL);
            this.statement = this.connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); System.exit(0); }
    }

    public ArrayList<Object[]> getAllEmployees(LocalDate date, String searchQuery) {
        ArrayList<Object[]> allEmployees = new ArrayList<>();
        String selectQuery = "CALL GetAllEmployees(?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
			preparedStatement.setString(2, searchQuery != null ? ("%" + searchQuery + "%") : "");

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					int id = resultSet.getInt("employee_id");
					String firstName = resultSet.getString("first_name");
					String lastName = resultSet.getString("last_name");
					String employeeStatus = resultSet.getString("employee_status");
					Time shiftStart = resultSet.getTime("shift_start");
					Time shiftEnd = resultSet.getTime("shift_end");
					Timestamp checkIn = resultSet.getTimestamp("check_in");
					Timestamp checkOut = resultSet.getTimestamp("check_out");

					Object[] employeeData = {
						id,
						firstName,
						lastName,
						employeeStatus,
						(shiftStart != null ? shiftStart.toString() : "No Shift"),
						(shiftEnd != null ? shiftEnd.toString() : "No Shift"),
						(checkIn != null ? checkIn.toLocalDateTime().toString() : "NA"),
						(checkOut != null ? checkOut.toLocalDateTime().toString() : "NA")
					};
					allEmployees.add(employeeData);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
        return allEmployees;
    }

    public void addEmployee(String firstName, String lastName, boolean onLeave, Time shiftStart, Time shiftEnd) throws SQLException {
        String query = "CALL CreateEmployee(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setBoolean(3, onLeave);
            ps.setTime(4, shiftStart);
            ps.setTime(5, shiftEnd);
            ps.executeUpdate();
        }
    }

    public void checkInEmployee(int employeeId) throws SQLException {
        String sql = "{CALL CheckInEmployee(?)}";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setInt(1, employeeId);
            stmt.execute();
        }
    }
	

    public void checkOutEmployee(int employeeId) throws SQLException {
        String sql = "{CALL CheckOutEmployee(?)}";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setInt(1, employeeId);
            stmt.execute();
        }
    }

	public void toggleEmployeeStatus(int employeeId) throws SQLException {
        String sql = "{CALL ToggleEmployeeLeave(?)}";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setInt(1, employeeId);
            stmt.execute();
        }
    }

    public boolean setShiftForEmployee(int employeeId, Time shiftStart, Time shiftEnd) {
        String sql = "{CALL SetEmployeeShift(?, ?, ?)}";
        try (CallableStatement stmt = this.connection.prepareCall(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setTime(2, shiftStart);
            stmt.setTime(3, shiftEnd);

            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
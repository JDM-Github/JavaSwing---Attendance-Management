DROP DATABASE IF EXISTS test;
CREATE DATABASE test;
USE test;

CREATE TABLE Employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    onLeave BOOLEAN DEFAULT FALSE
);

CREATE TABLE Attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT,
    date DATE NOT NULL,
    check_in DATETIME,
    check_out DATETIME,
    status ENUM('Present', 'Late', 'Absent', 'Not Set') DEFAULT 'Not Set',
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE Shifts (
    shift_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE CASCADE
);




DELIMITER //

DROP PROCEDURE IF EXISTS GetAllEmployees;

CREATE PROCEDURE GetAllEmployees(
    IN selectedDate DATE,
    IN searchQuery VARCHAR(100)
)
BEGIN
    INSERT INTO Attendance (employee_id, date, status)
    SELECT e.employee_id, selectedDate, 'Absent'
    FROM Employees e
    LEFT JOIN Attendance a 
        ON e.employee_id = a.employee_id 
        AND a.date = selectedDate
    WHERE a.attendance_id IS NULL;

    SELECT 
        e.employee_id,
        e.first_name,
        e.last_name,
        IF(e.onLeave, 'On Leave', 'Active') AS employee_status,
        s.start_time AS shift_start,
        s.end_time AS shift_end,
        a.check_in,
        a.check_out
    FROM 
        Employees e
    LEFT JOIN 
        Shifts s ON e.employee_id = s.employee_id
    LEFT JOIN 
        Attendance a ON e.employee_id = a.employee_id AND a.date = selectedDate
    WHERE 
        (searchQuery IS NULL OR searchQuery = '' 
         OR e.first_name LIKE CONCAT('%', searchQuery, '%') 
         OR e.last_name LIKE CONCAT('%', searchQuery, '%'));
END//

DELIMITER ;


DELIMITER //

CREATE PROCEDURE CreateEmployee(
    IN p_first_name VARCHAR(100),
    IN p_last_name VARCHAR(100),
    IN p_on_leave BOOLEAN,
    IN p_shift_start TIME,
    IN p_shift_end TIME
)
BEGIN
    DECLARE new_employee_id INT;

    INSERT INTO Employees (first_name, last_name, onLeave)
    VALUES (p_first_name, p_last_name, p_on_leave);

    SET new_employee_id = LAST_INSERT_ID();

    INSERT INTO Shifts (employee_id, start_time, end_time)
    VALUES (new_employee_id, p_shift_start, p_shift_end);

    INSERT INTO Attendance (employee_id, date, status)
    VALUES (new_employee_id, CURDATE(), 'Not Set');

END//

DELIMITER ;

DELIMITER //

CREATE PROCEDURE CheckInEmployee(
    IN emp_id INT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM Attendance 
        WHERE employee_id = emp_id 
        AND date = CURDATE()
    ) THEN
        UPDATE Attendance 
        SET check_in = NOW(), status = 'Present'
        WHERE employee_id = emp_id AND date = CURDATE();
    ELSE
        INSERT INTO Attendance (employee_id, date, check_in, status) 
        VALUES (emp_id, CURDATE(), NOW(), 'Present');
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE CheckOutEmployee(
    IN emp_id INT
)
BEGIN

    UPDATE Attendance 
    SET check_out = NOW()
    WHERE employee_id = emp_id AND date = CURDATE();
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE SetEmployeeShift(
    IN p_employee_id INT,
    IN p_startTime TIME,
    IN p_endTime TIME
)
BEGIN
    IF EXISTS (SELECT 1 FROM Shifts WHERE employee_id = p_employee_id) THEN
        UPDATE Shifts
        SET start_time = p_startTime,
            end_time = p_endTime
        WHERE employee_id = p_employee_id;
    ELSE
        INSERT INTO Shifts (employee_id, start_time, end_time)
        VALUES (p_employee_id, p_startTime, p_endTime);
    END IF;
END//

DELIMITER ;

DELIMITER //

CREATE PROCEDURE ToggleEmployeeLeave(
    IN p_employee_id INT
)
BEGIN
    DECLARE currentStatus BOOLEAN;

    SELECT onLeave INTO currentStatus
    FROM Employees
    WHERE employee_id = p_employee_id;

    IF currentStatus IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Employee not found';
    END IF;

    IF currentStatus = TRUE THEN
        UPDATE Employees SET onLeave = FALSE WHERE employee_id = p_employee_id;
    ELSE
        UPDATE Employees SET onLeave = TRUE WHERE employee_id = p_employee_id;
    END IF;
END//

DELIMITER ;

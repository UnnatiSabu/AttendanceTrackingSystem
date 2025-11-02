package src;

import java.sql.*;
import java.time.*;
import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;

public class EmployeeAttendanceTracker {
    private Connection connection;

    // Constructor: load config and connect
    public EmployeeAttendanceTracker() {
        try {
            // Try load properties from config.properties (if present)
            Properties props = new Properties();
            String url = "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=Asia/Kolkata";
            String user = "root";
            String password = "";

            try (FileInputStream fis = new FileInputStream("config.properties")) {
                props.load(fis);
                url = props.getProperty("db.url", url);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
            } catch (Exception e) {
                // If config file missing then it will use defaults/hardcoded values
            }

            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Authenticate admin using username & password
    public boolean adminLogin(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // if admin exists
            }
        } catch (SQLException e) {
            System.out.println("Error during admin login: " + e.getMessage());
            return false;
        }
    }

    // Register an employee
    public void registerEmployee(String employeeId, String employeeName) {
        String sql = "INSERT INTO employees (employee_id, employee_name) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setString(2, employeeName);
            ps.executeUpdate();  //For Insertion
            System.out.println("Employee " + employeeName + " (" + employeeId + ") registered.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Employee ID already exists.");
        } catch (SQLException e) {
            System.out.println("Error registering employee: " + e.getMessage());
        }
    }

    // Check if employee exists
    private boolean isEmployeeRegistered(String employeeId) throws SQLException {
        String sql = "SELECT 1 FROM employees WHERE employee_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check if marked today
    private boolean isAlreadyMarked(String employeeId, LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM attendance WHERE employee_id = ? AND attendance_date = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setDate(2, Date.valueOf(date)); // java.sql.Date
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Mark attendance for employee
    public void markAttendance(String employeeId) {
        try {
            if (!isEmployeeRegistered(employeeId)) {
                System.out.println("Employee ID not found. Do you want to register? (yes/no)");
                Scanner sc = new Scanner(System.in);
                if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                    System.out.print("Enter employee name: ");
                    String name = sc.nextLine();
                    registerEmployee(employeeId, name);
                }
                return;
            }

            LocalDate currentDate = LocalDate.now();
            if (isAlreadyMarked(employeeId, currentDate)) {
                System.out.println("Attendance already marked for today.");
                return;
            }

            LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            String sql = "INSERT INTO attendance (employee_id, attendance_date, attendance_time) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, employeeId);
                ps.setDate(2, Date.valueOf(currentDate)); // java.sql.Date
                ps.setTimestamp(3, Timestamp.valueOf(currentTime)); // java.sql.Timestamp
                ps.executeUpdate();
                System.out.println("Attendance marked for " + employeeId + " at " + currentTime);
            }
        } catch (SQLException e) {
            System.out.println("Error marking attendance: " + e.getMessage());
        }
    }

    // Print attendance for a specific date
    public void printAttendance(LocalDate date) {
        String sql = "SELECT e.employee_name, a.employee_id, a.attendance_time "
                   + "FROM attendance a JOIN employees e ON a.employee_id = e.employee_id "
                   + "WHERE a.attendance_date = ? ORDER BY a.attendance_time";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                boolean has = false;
                System.out.println("\nAttendance for " + date + ":");
                while (rs.next()) {
                    has = true;
                    String name = rs.getString("employee_name");
                    String id = rs.getString("employee_id");
                    Timestamp ts = rs.getTimestamp("attendance_time");
                    System.out.println(name + " (" + id + ") - " + ts.toString());
                }
                if (!has) System.out.println("No records found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching attendance: " + e.getMessage());
        }
    }

    // Print all employees
    public void printAllEmployees() {
        String sql = "SELECT employee_id, employee_name FROM employees ORDER BY employee_id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\nAll registered employees:");
            while (rs.next()) {
                System.out.println(rs.getString("employee_id") + " - " + rs.getString("employee_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employees: " + e.getMessage());
        }
    }

    // Clear attendance older than one month
    public void clearOldRecords() {
        String sql = "DELETE FROM attendance WHERE attendance_date < DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        try (Statement st = connection.createStatement()) {
            int deleted = st.executeUpdate(sql);
            System.out.println("Deleted " + deleted + " old attendance records.");
        } catch (SQLException e) {
            System.out.println("Error clearing old records: " + e.getMessage());
        }
    }

    // Role-based console menu
    public void startMonitoring() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Attendance Tracker!");
        System.out.print("Are you an Admin or Employee? (A/E): ");
        String role = sc.nextLine().trim().toUpperCase();

        if (role.equals("A")) {
            System.out.print("Enter admin username: ");
            String user = sc.nextLine().trim();
            System.out.print("Enter admin password: ");
            String pass = sc.nextLine().trim();

            if (adminLogin(user, pass)) {
                System.out.println("Admin login successful!");
                startAdminMenu(sc);
            } else {
                System.out.println("Invalid admin credentials.");
            }
        } else if (role.equals("E")) {
            startEmployeeMenu(sc);
        } else {
            System.out.println("Invalid choice. Exiting...");
        }
    }

    // Admin menu options
    private void startAdminMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Today's Attendance");
            System.out.println("2. View All Employees");
            System.out.println("3. Clear Old Records");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            String input = sc.nextLine().trim();

            switch (input) {
                case "1" -> printAttendance(java.time.LocalDate.now());
                case "2" -> printAllEmployees();
                case "3" -> clearOldRecords();
                case "4" -> { System.out.println("Logging out..."); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Employee menu options
    private void startEmployeeMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- Employee Menu ---");
            System.out.println("1. Mark Attendance");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");
            String input = sc.nextLine().trim();

            switch (input) {
                case "1" -> {
                    System.out.print("Enter your Employee ID: ");
                    String empId = sc.nextLine().trim();
                    markAttendance(empId);
                }
                case "2" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

}

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

class EmployeeAttendanceTracker {
    private TreeMap<String, String> employeeData; // ID -> Name
    private HashMap<LocalDate, LinkedHashSet<String>> attendanceRecords; // Today's attendance entries
    private HashMap<LocalDate, HashSet<String>> attendanceIds; // O(1) check for IDs
    private HashSet<String> registeredEmployeeIds; // Set of registered employee IDs
    private final String CSV_FOLDER = "D:\\Programming\\Projects\\Employee_Attendance_Tracking_Java\\Attendance\\";
    private Scanner scanner; // Single Scanner instance

    public EmployeeAttendanceTracker() {
        employeeData = new TreeMap<>();
        attendanceRecords = new HashMap<>();
        attendanceIds = new HashMap<>();
        registeredEmployeeIds = new HashSet<>();
        scanner = new Scanner(System.in);

        // Pre-register some employees
        registerEmployee("EMP332", "Shrawani Salave");
        registerEmployee("EMP329", "Ruta Chaudhari");
        registerEmployee("EMP331", "Saee Gade");
        registerEmployee("EMP330", "Unnati Sabu");
    }

    // -------------------- Registration --------------------
    private void registerNewEmployee() {
        try {
            System.out.print("Enter employee ID: ");
            String employeeId = scanner.nextLine();

            if (!employeeId.matches("EMP\\d{3}")) {
                System.out.println("Invalid ID format. Use EMPXXX.");
                return;
            }
            if (registeredEmployeeIds.contains(employeeId)) {
                System.out.println("Employee ID already exists.");
                return;
            }

            System.out.print("Enter employee name: ");
            String employeeName = scanner.nextLine();

            registeredEmployeeIds.add(employeeId);
            employeeData.put(employeeId, employeeName);
            System.out.println("Employee " + employeeName + " (" + employeeId + ") registered successfully.");
        } catch (Exception e) {
            System.out.println("Error in registration: " + e.getMessage());
        }
    }

    private void registerEmployee(String employeeId, String employeeName) {
        registeredEmployeeIds.add(employeeId);
        employeeData.put(employeeId, employeeName);
    }

    // -------------------- Attendance --------------------
    public void markAttendance(String employeeId) {
        try {
            if (!registeredEmployeeIds.contains(employeeId)) {
                System.out.println("Employee ID not found. Register? (yes/no)");
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("yes")) {
                    registerNewEmployee();
                } else {
                    return;
                }
            }

            LocalDate today = LocalDate.now();
            attendanceRecords.putIfAbsent(today, new LinkedHashSet<>());
            attendanceIds.putIfAbsent(today, new HashSet<>());

            if (attendanceIds.get(today).contains(employeeId)) {
                System.out.println("Attendance already marked for " + employeeData.get(employeeId));
                return;
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            String entry = employeeData.get(employeeId) + " (" + employeeId + ") - " + formatTime(now);

            attendanceRecords.get(today).add(entry);
            attendanceIds.get(today).add(employeeId);

            System.out.println("Attendance marked for " + employeeData.get(employeeId));
            writeToCSV(today, entry);
        } catch (Exception e) {
            System.out.println("Error marking attendance: " + e.getMessage());
        }
    }

    private void writeToCSV(LocalDate date, String entry) {
        try (FileWriter writer = new FileWriter(CSV_FOLDER + date.toString() + ".csv", true)) {
            writer.write(entry + "\n");
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }

    // -------------------- Printing --------------------
    public void printAttendance(LocalDate date) {
        if (!attendanceRecords.containsKey(date)) {
            System.out.println("No attendance recorded for " + date);
            return;
        }
        System.out.println("\nAttendance for " + date + ":\n");
        for (String entry : attendanceRecords.get(date)) {
            System.out.println(entry);
        }
    }

    public void printAllEmployees() {
        System.out.println("\nAll registered employees:\n");
        for (Map.Entry<String, String> entry : employeeData.entrySet()) {
            System.out.println("Employee ID: " + entry.getKey() + ", Name: " + entry.getValue());
        }
    }

    // -------------------- Clear Old Records --------------------
    public void clearOldRecords() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        Iterator<Map.Entry<LocalDate, LinkedHashSet<String>>> iterator = attendanceRecords.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LocalDate, LinkedHashSet<String>> entry = iterator.next();
            LocalDate date = entry.getKey();
            if (date.isBefore(oneMonthAgo)) {
                java.io.File file = new java.io.File(CSV_FOLDER + date.toString() + ".csv");
                if (file.exists() && file.delete()) {
                    iterator.remove();
                    attendanceIds.remove(date);
                    System.out.println("Deleted old records for " + date);
                } else {
                    System.out.println("Failed to delete old records for " + date);
                }
            }
        }
        System.out.println("Old record cleanup completed.");
    }

    // -------------------- Utilities --------------------
    private String formatTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // -------------------- Main Menu --------------------
    public void startMonitoring() {
        while (true) {
            try {
                System.out.println("\nMenu:");
                System.out.println("1. Mark Attendance");
                System.out.println("2. Register New Employee");
                System.out.println("3. Print Today's Attendance");
                System.out.println("4. Print All Employees");
                System.out.println("5. Clear Old Records");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1: 
                        System.out.print("Enter employee ID: ");
                        String empId = scanner.nextLine();
                        markAttendance(empId);
                        break; 
                    case 2: 
                        registerNewEmployee();
                        break;
                    case 3: 
                        printAttendance(LocalDate.now());
                        break;
                    case 4: 
                        printAllEmployees();
                        break;
                    case 5: 
                        clearOldRecords();
                        break;
                    case 6: 
                        System.out.println("Exiting...");
                        return;
                    default: System.out.println("Invalid choice. Enter 1-6.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + ". Returning to menu...");
            }
        }
    }
}

// -------------------- Main Class --------------------
public class MainTracker {
    public static void main(String[] args) {
        EmployeeAttendanceTracker tracker = new EmployeeAttendanceTracker();
        tracker.startMonitoring();
    }
}


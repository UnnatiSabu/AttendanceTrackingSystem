package src;

public class MainTracker {
    public static void main(String[] args) {
        try {
            EmployeeAttendanceTracker tracker = new EmployeeAttendanceTracker();
            tracker.startMonitoring();
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

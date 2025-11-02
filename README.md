# AttendanceTrackingSystem

This project is a Java-based application that allows organizations to efficiently track employee attendance. Key features include:

* Employee Registration: Employees can be registered using a unique ID in the format EMPXXX, where XXX represents three digits.
* Attendance Marking: Employees can mark their attendance, which is recorded with the date and time in Indian Standard Time (IST).
* CSV Storage: Attendance records are automatically stored in daily CSV files for easy access and backup.
* View Attendance: The application allows viewing of attendance records for a specific date.
* Employee Records: The system maintains a list of all registered employees and can print this list upon request.
* Old Records Management: The system can automatically delete attendance records older than one month to conserve storage.
* Menu-Driven Interface: The application offers an interactive menu for users to select actions such as marking attendance, registering new employees, and viewing records.
* Data Consistency: The system ensures that no duplicate attendance entries are made for the same employee on the same day.
* Error Handling: Includes robust error handling to manage user input and system exceptions.

package server.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory pattern implementation for command creation.
 * Registers all available commands and provides them by name.
 */
public class CommandFactory {
    private static Map<String, Command> commands = new HashMap<>();

    static {
        // Basic commands
        commands.put("ECHO", new EchoCommand());
        commands.put("LOGOUT", new LogoutCommand());
        
        // Authentication
        commands.put("LOGIN", new LoginCommand());
        
        // Grade management
        commands.put("GET_GRADES", new GetGradesCommand());
        commands.put("ADD_GRADE", new AddGradeCommand());
        commands.put("ADD_ACADEMIC_RECORD", new AddAcademicRecordCommand());
        
        // UC2: GPA Calculation
        commands.put("CALCULATE_GPA", new CalculateGPACommand());
        
        // UC3: Session Eligibility
        commands.put("CHECK_SESSION_ELIGIBILITY", new CheckSessionEligibilityCommand());
        
        // UC4: Grade Report
        commands.put("GENERATE_GRADE_REPORT", new GenerateGradeReportCommand());
        
        // UC5: Group Analytics
        commands.put("GET_GROUP_ANALYTICS", new GetGroupAnalyticsCommand());
        
        // UC6: Low Grade Alerts
        commands.put("GET_LOW_GRADE_ALERTS", new GetLowGradeAlertsCommand());
        
        // UC7: Teacher Workload
        commands.put("GET_TEACHER_WORKLOAD", new GetTeacherWorkloadCommand());
        
        // UC8: Schedule Validation
        commands.put("VALIDATE_SCHEDULE", new ValidateScheduleCommand());
        
        // UC9: Attendance Summary
        commands.put("GET_ATTENDANCE_SUMMARY", new GetAttendanceSummaryCommand());
        
        // UC10: Archive Records
        commands.put("ARCHIVE_RECORDS", new ArchiveRecordsCommand());
        
        // UC11: Course Statistics
        commands.put("GET_COURSE_STATISTICS", new GetCourseStatisticsCommand());
        
        // UC12: Export Analytics
        commands.put("EXPORT_ANALYTICS", new ExportAnalyticsCommand());
    }

    public static Command getCommand(String commandName) {
        Command cmd = commands.get(commandName);
        if (cmd == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        return cmd;
    }
    
    /**
     * Returns all registered command names.
     */
    public static String[] getRegisteredCommands() {
        return commands.keySet().toArray(new String[0]);
    }
}
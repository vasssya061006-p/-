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

        // Teacher commands
        commands.put("GET_TEACHER_GROUPS", new GetTeacherGroupsCommand());
        commands.put("GET_TEACHER_COURSES", new GetTeacherCoursesCommand());
        commands.put("GET_STUDENTS_BY_GROUP_AND_COURSE", new GetStudentsByGroupAndCourseCommand());
        commands.put("GET_STUDENT_GRADES", new GetStudentGradesCommand());
        commands.put("GET_GROUP_PERFORMANCE", new GetGroupPerformanceCommand());

        commands.put("GET_ALL_SCHEDULES", new GetAllSchedulesCommand());

        // Admin - Group management
        commands.put("ADD_GROUP", new AddGroupCommand());
        commands.put("GET_ALL_GROUPS", new GetAllGroupsCommand());

        // Admin - Course management
        commands.put("ADD_COURSE", new AddCourseCommand());
        commands.put("GET_ALL_COURSES", new GetAllCoursesCommand());

        // Admin - Student management
        commands.put("ADD_STUDENT", new AddStudentCommand());
        commands.put("GET_ALL_STUDENTS", new GetAllStudentsCommand());

        // Update and Delete commands
        commands.put("UPDATE_GROUP", new UpdateGroupCommand());
        commands.put("DELETE_GROUP", new DeleteGroupCommand());
        commands.put("UPDATE_COURSE", new UpdateCourseCommand());
        commands.put("DELETE_COURSE", new DeleteCourseCommand());
        commands.put("UPDATE_STUDENT", new UpdateStudentCommand());
        commands.put("DELETE_STUDENT", new DeleteStudentCommand());

        // Report commands
        commands.put("REPORT_GROUP_PERFORMANCE", new ReportGroupPerformanceCommand());
        commands.put("REPORT_TEACHER_LOAD", new ReportTeacherLoadCommand());
        commands.put("REPORT_LOW_GRADES", new ReportLowGradesCommand());
        commands.put("REPORT_ATTENDANCE", new ReportAttendanceCommand());
        commands.put("REPORT_FULL_ANALYTICS", new ReportFullAnalyticsCommand());

        // Schedule management
        commands.put("GET_ALL_TEACHERS", new GetAllTeachersCommand());
        commands.put("GET_ALL_SCHEDULES_FOR_LIST", new GetAllSchedulesForListCommand());
        commands.put("ADD_SCHEDULE", new AddScheduleCommand());
        commands.put("DELETE_SCHEDULE", new DeleteScheduleCommand());

        commands.put("GET_ALL_TEACHERS_FOR_MANAGEMENT", new GetAllTeachersForManagementCommand());
        commands.put("GET_TEACHER_DETAILS", new GetTeacherDetailsCommand());
        commands.put("UPDATE_TEACHER", new UpdateTeacherCommand());

        commands.put("ADD_TEACHER", new AddTeacherCommand());
        commands.put("DELETE_TEACHER", new DeleteTeacherCommand());
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

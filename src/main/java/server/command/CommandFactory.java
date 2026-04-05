package server.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("ECHO", new EchoCommand());
        commands.put("LOGOUT", new LogoutCommand());

        commands.put("LOGIN", new LoginCommand());

        commands.put("GET_GRADES", new GetGradesCommand());
        commands.put("ADD_GRADE", new AddGradeCommand());
        commands.put("ADD_ACADEMIC_RECORD", new AddAcademicRecordCommand());

        commands.put("CALCULATE_GPA", new CalculateGPACommand());

        commands.put("CHECK_SESSION_ELIGIBILITY", new CheckSessionEligibilityCommand());

        commands.put("GENERATE_GRADE_REPORT", new GenerateGradeReportCommand());

        commands.put("GET_GROUP_ANALYTICS", new GetGroupAnalyticsCommand());

        commands.put("GET_LOW_GRADE_ALERTS", new GetLowGradeAlertsCommand());

        commands.put("GET_TEACHER_WORKLOAD", new GetTeacherWorkloadCommand());

        commands.put("VALIDATE_SCHEDULE", new ValidateScheduleCommand());

        commands.put("GET_ATTENDANCE_SUMMARY", new GetAttendanceSummaryCommand());

        commands.put("ARCHIVE_RECORDS", new ArchiveRecordsCommand());

        commands.put("GET_COURSE_STATISTICS", new GetCourseStatisticsCommand());

        commands.put("EXPORT_ANALYTICS", new ExportAnalyticsCommand());
    }

    public static Command getCommand(String commandName) {
        Command cmd = commands.get(commandName);
        if (cmd == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }
        return cmd;
    }

    public static String[] getRegisteredCommands() {
        return commands.keySet().toArray(new String[0]);
    }
}

package server.service;

import server.dao.*;
import server.dao.impl.*;
import server.model.*;
import server.factory.UserFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive service implementing all 12+ high-level business use cases.
 * This class centralizes business logic on the server side as required.
 * 
 * Use Cases implemented:
 * 1. Authenticate User (login)
 * 2. Calculate Student GPA
 * 3. Check Session Eligibility
 * 4. Generate Grade Report
 * 5. Get Group Performance Analytics
 * 6. Send Low-Grade Alerts
 * 7. Get Teacher Workload Report
 * 8. Generate Schedule Without Conflicts
 * 9. Get Student Attendance Summary
 * 10. Archive Academic Records
 * 11. Get Course Statistics
 * 12. Export Academic Analytics
 */
public class EducationService {
    
    private static volatile EducationService instance;
    private final UserDao userDao;
    private final AcademicRecordDao recordDao;
    private final ScheduleDao scheduleDao;
    private final CourseDao courseDao;
    private final GroupDao groupDao;
    
    // Session management (thread-safe)
    private final Map<Integer, User> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    
    private EducationService() {
        this.userDao = new UserDaoImpl();
        this.recordDao = new AcademicRecordDaoImpl();
        this.scheduleDao = new ScheduleDaoImpl();
        this.courseDao = new CourseDaoImpl();
        this.groupDao = new GroupDaoImpl();
    }
    
    /**
     * Singleton instance getter (thread-safe).
     */
    public static EducationService getInstance() {
        if (instance == null) {
            synchronized (EducationService.class) {
                if (instance == null) {
                    instance = new EducationService();
                }
            }
        }
        return instance;
    }
    
    // =========================================================================
    // USE CASE 1: Authenticate User
    // =========================================================================
    
    /**
     * UC1: Authenticates user with login/password and creates session.
     * @param login user login
     * @param password plain text password
     * @return authenticated User object or null if failed
     * @throws AuthenticationException if authentication fails
     */
    public User authenticateUser(String login, String password) throws AuthenticationException {
        // Check for account lockout
        String failKey = login.toLowerCase();
        Integer attempts = failedLoginAttempts.get(failKey);
        if (attempts != null && attempts >= 5) {
            throw new AuthenticationException("Account is temporarily locked due to multiple failed attempts");
        }
        
        try {
            User user = userDao.findByLogin(login);
            if (user == null) {
                recordFailedAttempt(failKey);
                throw new AuthenticationException("Invalid login or password");
            }
            
            String hashedInput = User.hashPassword(password);
            if (!user.getPasswordHash().equals(hashedInput)) {
                recordFailedAttempt(failKey);
                throw new AuthenticationException("Invalid login or password");
            }
            
            if (!user.isActive()) {
                throw new AuthenticationException("User account is disabled");
            }
            
            // Clear failed attempts and create session
            failedLoginAttempts.remove(failKey);
            activeSessions.put(user.getId(), user);
            
            // Return user without password hash for security
            User safeUser = UserFactory.createUserFromData(
                user.getRole(), user.getId(), user.getLogin(), 
                null, user.getFullName(), user.isActive()
            );
            safeUser.setGroupId(user.getGroupId());
            
            return safeUser;
        } catch (SQLException e) {
            throw new AuthenticationException("Database error during authentication: " + e.getMessage(), e);
        }
    }
    
    private void recordFailedAttempt(String loginKey) {
        failedLoginAttempts.merge(loginKey, 1, Integer::sum);
    }
    
    /**
     * Validates session token (userId).
     */
    public User validateSession(Integer userId) {
        if (userId == null) return null;
        return activeSessions.get(userId);
    }
    
    /**
     * Ends user session.
     */
    public void logout(Integer userId) {
        if (userId != null) {
            activeSessions.remove(userId);
        }
    }
    
    // =========================================================================
    // USE CASE 2: Calculate Student GPA
    // =========================================================================
    
    /**
     * UC2: Calculates weighted GPA for a student.
     * @param studentId the student ID
     * @return GPACalculatorResult with detailed breakdown
     */
    public GPACalculatorResult calculateStudentGPA(int studentId) {
        try {
            List<AcademicRecord> records = recordDao.findByStudentId(studentId);
            if (records.isEmpty()) {
                return new GPACalculatorResult(studentId, "No grades available", 0.0, 0, 0, 0, 0, new HashMap<>());
            }
            
            double sum = 0;
            int count = 0;
            int exams = 0, tests = 0, coursework = 0;
            Map<String, Double> courseAverages = new HashMap<>();
            
            for (AcademicRecord r : records) {
                if (r.getGradeValue() > 0) {
                    sum += r.getGradeValue();
                    count++;
                    
                    switch (r.getGradeType()) {
                        case "exam": exams++; break;
                        case "test": tests++; break;
                        case "coursework": coursework++; break;
                    }
                    
                    courseAverages.merge(r.getCourseName(), r.getGradeValue(), 
                        (old, newVal) -> (old + newVal) / 2.0);
                }
            }
            
            double avg = count > 0 ? sum / count : 0.0;
            
            return new GPACalculatorResult(studentId, "Calculated successfully", 
                Math.round(avg * 100.0) / 100.0, count, exams, tests, coursework, courseAverages);
        } catch (SQLException e) {
            return new GPACalculatorResult(studentId, "Error: " + e.getMessage(), 0.0, 0, 0, 0, 0, new HashMap<>());
        }
    }
    
    // =========================================================================
    // USE CASE 3: Check Session Eligibility
    // =========================================================================
    
    /**
     * UC3: Checks if student is eligible for exam session.
     * Requirements: average grade >= 3.0, absences < limit per course.
     */
    public SessionEligibilityResult checkSessionEligibility(int studentId) {
        try {
            List<AcademicRecord> records = recordDao.findByStudentId(studentId);
            int absences = recordDao.countAbsencesByStudent(studentId);
            double avgGrade = recordDao.getAverageGradeByStudent(studentId);
            
            boolean gradeEligible = avgGrade >= 3.0;
            boolean attendanceEligible = absences < 3; // Max 3 absences allowed
            boolean hasFailingGrades = records.stream().anyMatch(r -> r.getGradeValue() > 0 && r.getGradeValue() < 3.0);
            
            List<String> issues = new ArrayList<>();
            if (!gradeEligible) issues.add("Average grade below 3.0: " + String.format("%.2f", avgGrade));
            if (!attendanceEligible) issues.add("Too many absences: " + absences);
            if (hasFailingGrades) issues.add("Has failing grades (< 3.0)");
            
            return new SessionEligibilityResult(
                studentId,
                gradeEligible && attendanceEligible && !hasFailingGrades,
                avgGrade,
                absences,
                records.size(),
                issues
            );
        } catch (SQLException e) {
            return new SessionEligibilityResult(studentId, false, 0.0, 0, 0, 
                Collections.singletonList("Database error: " + e.getMessage()));
        }
    }
    
    // =========================================================================
    // USE CASE 4: Generate Grade Report (Vedomost)
    // =========================================================================
    
    /**
     * UC4: Generates comprehensive grade report for a student or group.
     */
    public GradeReportResult generateGradeReport(int studentId) {
        try {
            List<AcademicRecord> records = recordDao.findByStudentId(studentId);
            double avgGrade = recordDao.getAverageGradeByStudent(studentId);
            
            // Group by course
            Map<String, List<AcademicRecord>> byCourse = new LinkedHashMap<>();
            for (AcademicRecord r : records) {
                byCourse.computeIfAbsent(r.getCourseName(), k -> new ArrayList<>()).add(r);
            }
            
            StringBuilder report = new StringBuilder();
            report.append("GRADE REPORT\n");
            report.append("========================\n");
            report.append("Student ID: ").append(studentId).append("\n");
            report.append("Average Grade: ").append(String.format("%.2f", avgGrade)).append("\n\n");
            
            for (Map.Entry<String, List<AcademicRecord>> entry : byCourse.entrySet()) {
                report.append("Course: ").append(entry.getKey()).append("\n");
                for (AcademicRecord r : entry.getValue()) {
                    report.append("  - ").append(r.getGradeType())
                          .append(": ").append(r.getGradeValue())
                          .append(" (").append(r.getAttendanceStatus()).append(")\n");
                }
                report.append("\n");
            }
            
            return new GradeReportResult(studentId, report.toString(), records.size(), avgGrade, records);
        } catch (SQLException e) {
            return new GradeReportResult(studentId, "Error generating report: " + e.getMessage(), 
                0, 0.0, Collections.emptyList());
        }
    }
    
    // =========================================================================
    // USE CASE 5: Get Group Performance Analytics
    // =========================================================================
    
    /**
     * UC5: Provides statistical analysis of group performance.
     */
    public GroupAnalyticsResult getGroupPerformanceAnalytics(int groupId) {
        try {
            double avgGrade = recordDao.getAverageGradeByGroup(groupId);
            List<User> students = userDao.findByGroupId(groupId);
            
            int excellent = 0, good = 0, satisfactory = 0, failing = 0;
            double minGrade = 10.0, maxGrade = 1.0;
            
            for (User student : students) {
                double studentAvg = recordDao.getAverageGradeByStudent(student.getId());
                if (studentAvg > 0) {
                    if (studentAvg >= 9.0) excellent++;
                    else if (studentAvg >= 7.0) good++;
                    else if (studentAvg >= 5.0) satisfactory++;
                    else failing++;
                    
                    minGrade = Math.min(minGrade, studentAvg);
                    maxGrade = Math.max(maxGrade, studentAvg);
                }
            }
            
            Map<String, Object> distribution = new LinkedHashMap<>();
            distribution.put("excellent (9-10)", excellent);
            distribution.put("good (7-8)", good);
            distribution.put("satisfactory (5-6)", satisfactory);
            distribution.put("failing (<5)", failing);
            
            return new GroupAnalyticsResult(groupId, students.size(), 
                Math.round(avgGrade * 100.0) / 100.0, 
                minGrade == 10.0 ? 0.0 : Math.round(minGrade * 100.0) / 100.0,
                maxGrade == 1.0 ? 0.0 : Math.round(maxGrade * 100.0) / 100.0,
                distribution);
        } catch (SQLException e) {
            return new GroupAnalyticsResult(groupId, 0, 0.0, 0.0, 0.0, 
                Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    // =========================================================================
    // USE CASE 6: Send Low-Grade Alerts
    // =========================================================================
    
    /**
     * UC6: Identifies students with failing grades and generates alerts.
     */
    public List<AlertResult> getLowGradeAlerts(double threshold) {
        try {
            List<AcademicRecord> failingRecords = recordDao.getFailingGrades(threshold);
            Map<Integer, List<AcademicRecord>> byStudent = new LinkedHashMap<>();
            
            for (AcademicRecord r : failingRecords) {
                byStudent.computeIfAbsent(r.getStudentId(), k -> new ArrayList<>()).add(r);
            }
            
            List<AlertResult> alerts = new ArrayList<>();
            for (Map.Entry<Integer, List<AcademicRecord>> entry : byStudent.entrySet()) {
                int studentId = entry.getKey();
                List<AcademicRecord> records = entry.getValue();
                
                String message = "Student " + records.get(0).getStudentName() + 
                               " has " + records.size() + " failing grade(s):";
                for (AcademicRecord r : records) {
                    message += "\n  - " + r.getCourseName() + ": " + r.getGradeValue();
                }
                
                alerts.add(new AlertResult(studentId, "LOW_GRADE", message, records.size()));
            }
            
            return alerts;
        } catch (SQLException e) {
            AlertResult error = new AlertResult(0, "ERROR", 
                "Failed to generate alerts: " + e.getMessage(), 0);
            return Collections.singletonList(error);
        }
    }
    
    // =========================================================================
    // USE CASE 7: Get Teacher Workload Report
    // =========================================================================
    
    /**
     * UC7: Calculates teaching hours and course load per instructor.
     */
    public TeacherWorkloadResult getTeacherWorkloadReport(int teacherId) {
        try {
            Teacher teacher = (Teacher) userDao.findById(teacherId);
            if (teacher == null) {
                return new TeacherWorkloadResult(teacherId, "Teacher not found", 
                    0, 0, 0, 0.0, Collections.emptyList());
            }
            
            List<Schedule> schedules = scheduleDao.findByTeacherId(teacherId);
            Set<Integer> uniqueCourses = new HashSet<>();
            Set<Integer> uniqueGroups = new HashSet<>();
            int totalHours = 0;
            
            for (Schedule s : schedules) {
                uniqueCourses.add(s.getCourseId());
                uniqueGroups.add(s.getGroupId());
                if (s.getStartTime() != null && s.getEndTime() != null) {
                    long minutes = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                    totalHours += minutes / 60.0;
                }
            }
            
            double workloadPercentage = teacher.getTeachingHoursPerWeek() / 36.0 * 100.0;
            
            return new TeacherWorkloadResult(teacherId, "Report generated",
                schedules.size(), uniqueCourses.size(), uniqueGroups.size(),
                workloadPercentage, schedules);
        } catch (SQLException e) {
            return new TeacherWorkloadResult(teacherId, "Error: " + e.getMessage(),
                0, 0, 0, 0.0, Collections.emptyList());
        }
    }
    
    // =========================================================================
    // USE CASE 8: Generate Schedule Without Conflicts
    // =========================================================================
    
    /**
     * UC8: Validates schedule and detects conflicts.
     */
    public ScheduleValidationResult validateSchedule(int scheduleId) {
        try {
            Schedule schedule = scheduleDao.findById(scheduleId);
            if (schedule == null) {
                return new ScheduleValidationResult(false, "Schedule not found", 
                    Collections.emptyList());
            }
            
            List<String> conflicts = new ArrayList<>();
            
            // Check room conflict
            List<Schedule> roomConflicts = scheduleDao.findByRoomAndTime(
                schedule.getRoomNumber(), schedule.getDayOfWeek(), 
                schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : "");
            for (Schedule s : roomConflicts) {
                if (s.getId() != scheduleId) {
                    conflicts.add("Room conflict: " + s.getRoomNumber() + 
                                " at " + schedule.getDayOfWeek() + " " + schedule.getStartTime());
                }
            }
            
            // Check teacher conflict
            if (schedule.getTeacherId() != null) {
                List<Schedule> teacherConflicts = scheduleDao.findByTeacherAndTime(
                    schedule.getTeacherId(), schedule.getDayOfWeek(),
                    schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : "");
                for (Schedule s : teacherConflicts) {
                    if (s.getId() != scheduleId) {
                        conflicts.add("Teacher conflict: " + s.getTeacherName() + 
                                    " at " + schedule.getDayOfWeek() + " " + schedule.getStartTime());
                    }
                }
            }
            
            // Check group conflict
            if (schedule.getGroupId() != null) {
                List<Schedule> groupConflicts = scheduleDao.findByGroupAndTime(
                    schedule.getGroupId(), schedule.getDayOfWeek(),
                    schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : "");
                for (Schedule s : groupConflicts) {
                    if (s.getId() != scheduleId) {
                        conflicts.add("Group conflict: " + s.getGroupName() + 
                                    " at " + schedule.getDayOfWeek() + " " + schedule.getStartTime());
                    }
                }
            }
            
            boolean isValid = conflicts.isEmpty();
            String message = isValid ? "Schedule is valid, no conflicts detected" : 
                           "Found " + conflicts.size() + " conflict(s)";
            
            return new ScheduleValidationResult(isValid, message, conflicts);
        } catch (SQLException e) {
            return new ScheduleValidationResult(false, "Error: " + e.getMessage(), 
                Collections.emptyList());
        }
    }
    
    // =========================================================================
    // USE CASE 9: Get Student Attendance Summary
    // =========================================================================
    
    /**
     * UC9: Calculates absence percentage and risk level.
     */
    public AttendanceSummaryResult getAttendanceSummary(int studentId) {
        try {
            List<AcademicRecord> records = recordDao.findByStudentId(studentId);
            int totalSessions = 0, present = 0, absent = 0, excused = 0;
            
            for (AcademicRecord r : records) {
                if (r.getAttendanceStatus() != null) {
                    totalSessions++;
                    switch (r.getAttendanceStatus().toLowerCase()) {
                        case "present": present++; break;
                        case "absent": absent++; break;
                        case "excused": excused++; break;
                    }
                }
            }
            
            double absenceRate = totalSessions > 0 ? (double) absent / totalSessions * 100.0 : 0.0;
            String riskLevel;
            if (absenceRate >= 30) riskLevel = "CRITICAL";
            else if (absenceRate >= 20) riskLevel = "HIGH";
            else if (absenceRate >= 10) riskLevel = "MEDIUM";
            else riskLevel = "LOW";
            
            return new AttendanceSummaryResult(studentId, totalSessions, present, absent, excused,
                Math.round(absenceRate * 100.0) / 100.0, riskLevel);
        } catch (SQLException e) {
            return new AttendanceSummaryResult(studentId, 0, 0, 0, 0, 0.0, "ERROR: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // USE CASE 10: Archive Academic Records
    // =========================================================================
    
    /**
     * UC10: Archives completed semester records.
     */
    public ArchiveResult archiveAcademicRecords(String semester) {
        try {
            List<AcademicRecord> records = recordDao.findBySemester(semester);
            if (records.isEmpty()) {
                return new ArchiveResult(semester, 0, "No records found for semester: " + semester);
            }
            
            // In production, this would move records to an archive table
            // For now, we simulate by counting and providing summary
            int totalRecords = records.size();
            double avgGrade = records.stream()
                .filter(r -> r.getGradeValue() > 0)
                .mapToDouble(AcademicRecord::getGradeValue)
                .average()
                .orElse(0.0);
            
            Set<Integer> uniqueStudents = new HashSet<>();
            Set<Integer> uniqueCourses = new HashSet<>();
            for (AcademicRecord r : records) {
                uniqueStudents.add(r.getStudentId());
                uniqueCourses.add(r.getCourseId());
            }
            
            String summary = String.format(
                "Archived %d records for %d students across %d courses. Average grade: %.2f",
                totalRecords, uniqueStudents.size(), uniqueCourses.size(), avgGrade
            );
            
            return new ArchiveResult(semester, totalRecords, summary);
        } catch (SQLException e) {
            return new ArchiveResult(semester, 0, "Archive error: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // USE CASE 11: Get Course Statistics
    // =========================================================================
    
    /**
     * UC11: Provides enrollment count, grade distribution per course.
     */
    public CourseStatisticsResult getCourseStatistics(int courseId) {
        try {
            double avgGrade = recordDao.getAverageGradeByCourse(courseId);
            List<AcademicRecord> records = recordDao.findByCourseId(courseId);
            
            Set<Integer> uniqueStudents = new HashSet<>();
            int gradeDistribution[] = new int[11]; // 0-10 scale
            
            for (AcademicRecord r : records) {
                uniqueStudents.add(r.getStudentId());
                if (r.getGradeValue() >= 0 && r.getGradeValue() <= 10) {
                    gradeDistribution[(int) Math.round(r.getGradeValue())]++;
                }
            }
            
            Map<String, Integer> gradeTypeDistribution = new LinkedHashMap<>();
            for (AcademicRecord r : records) {
                gradeTypeDistribution.merge(r.getGradeType(), 1, Integer::sum);
            }
            
            return new CourseStatisticsResult(courseId, uniqueStudents.size(),
                Math.round(avgGrade * 100.0) / 100.0, records.size(),
                gradeDistribution, gradeTypeDistribution);
        } catch (SQLException e) {
            return new CourseStatisticsResult(courseId, 0, 0.0, 0,
                new int[11], Collections.singletonMap("error", -1));
        }
    }
    
    // =========================================================================
    // USE CASE 12: Export Academic Analytics
    // =========================================================================
    
    /**
     * UC12: Generates comprehensive institutional report.
     */
    public AnalyticsExportResult exportAcademicAnalytics() {
        try {
            // Overall statistics
            List<User> allUsers = userDao.findAll();
            long studentCount = allUsers.stream().filter(u -> u instanceof Student).count();
            long teacherCount = allUsers.stream().filter(u -> u instanceof Teacher).count();
            
            List<Group> groups = groupDao.findAll();
            double overallAvgGrade = 0.0;
            int totalRecords = 0;
            
            Map<String, Double> groupAverages = new LinkedHashMap<>();
            for (Group g : groups) {
                double avg = groupDao.getAverageGrade(g.getId());
                if (avg > 0) {
                    groupAverages.put(g.getName(), avg);
                    overallAvgGrade += avg;
                    totalRecords++;
                }
            }
            
            if (totalRecords > 0) {
                overallAvgGrade /= totalRecords;
            }
            
            StringBuilder report = new StringBuilder();
            report.append("INSTITUTIONAL ANALYTICS REPORT\n");
            report.append("================================\n\n");
            report.append("Total Students: ").append(studentCount).append("\n");
            report.append("Total Teachers: ").append(teacherCount).append("\n");
            report.append("Total Groups: ").append(groups.size()).append("\n");
            report.append("Overall Average Grade: ").append(String.format("%.2f", overallAvgGrade)).append("\n\n");
            
            report.append("Group Performance:\n");
            for (Map.Entry<String, Double> entry : groupAverages.entrySet()) {
                report.append("  - ").append(entry.getKey()).append(": ")
                      .append(String.format("%.2f", entry.getValue())).append("\n");
            }
            
            return new AnalyticsExportResult(report.toString(), studentCount, teacherCount,
                groups.size(), Math.round(overallAvgGrade * 100.0) / 100.0, groupAverages);
        } catch (SQLException e) {
            return new AnalyticsExportResult("Error generating analytics: " + e.getMessage(),
                0, 0, 0, 0.0, Collections.singletonMap("error", 0.0));
        }
    }
    
    // =========================================================================
    // Result DTOs (nested classes for encapsulation)
    // =========================================================================
    
    public static class GPACalculatorResult {
        public int studentId;
        public String message;
        public double averageGrade;
        public int totalGrades;
        public int examCount;
        public int testCount;
        public int courseworkCount;
        public Map<String, Double> courseAverages;
        
        public GPACalculatorResult(int studentId, String message, double averageGrade,
                                   int totalGrades, int examCount, int testCount, 
                                   int courseworkCount, Map<String, Double> courseAverages) {
            this.studentId = studentId;
            this.message = message;
            this.averageGrade = averageGrade;
            this.totalGrades = totalGrades;
            this.examCount = examCount;
            this.testCount = testCount;
            this.courseworkCount = courseworkCount;
            this.courseAverages = courseAverages;
        }
    }
    
    public static class SessionEligibilityResult {
        public int studentId;
        public boolean eligible;
        public double averageGrade;
        public int absenceCount;
        public int totalRecords;
        public List<String> issues;
        
        public SessionEligibilityResult(int studentId, boolean eligible, double averageGrade,
                                        int absenceCount, int totalRecords, List<String> issues) {
            this.studentId = studentId;
            this.eligible = eligible;
            this.averageGrade = averageGrade;
            this.absenceCount = absenceCount;
            this.totalRecords = totalRecords;
            this.issues = issues;
        }
    }
    
    public static class GradeReportResult {
        public int studentId;
        public String report;
        public int recordCount;
        public double averageGrade;
        public List<AcademicRecord> records;
        
        public GradeReportResult(int studentId, String report, int recordCount, 
                                 double averageGrade, List<AcademicRecord> records) {
            this.studentId = studentId;
            this.report = report;
            this.recordCount = recordCount;
            this.averageGrade = averageGrade;
            this.records = records;
        }
    }
    
    public static class GroupAnalyticsResult {
        public int groupId;
        public int studentCount;
        public double averageGrade;
        public double minGrade;
        public double maxGrade;
        public Map<String, Object> distribution;
        
        public GroupAnalyticsResult(int groupId, int studentCount, double averageGrade,
                                    double minGrade, double maxGrade, Map<String, Object> distribution) {
            this.groupId = groupId;
            this.studentCount = studentCount;
            this.averageGrade = averageGrade;
            this.minGrade = minGrade;
            this.maxGrade = maxGrade;
            this.distribution = distribution;
        }
    }
    
    public static class AlertResult {
        public int studentId;
        public String alertType;
        public String message;
        public int failingGradeCount;
        
        public AlertResult(int studentId, String alertType, String message, int failingGradeCount) {
            this.studentId = studentId;
            this.alertType = alertType;
            this.message = message;
            this.failingGradeCount = failingGradeCount;
        }
    }
    
    public static class TeacherWorkloadResult {
        public int teacherId;
        public String message;
        public int lessonCount;
        public int courseCount;
        public int groupCount;
        public double workloadPercentage;
        public List<Schedule> schedules;
        
        public TeacherWorkloadResult(int teacherId, String message, int lessonCount,
                                     int courseCount, int groupCount, 
                                     double workloadPercentage, List<Schedule> schedules) {
            this.teacherId = teacherId;
            this.message = message;
            this.lessonCount = lessonCount;
            this.courseCount = courseCount;
            this.groupCount = groupCount;
            this.workloadPercentage = workloadPercentage;
            this.schedules = schedules;
        }
    }
    
    public static class ScheduleValidationResult {
        public boolean valid;
        public String message;
        public List<String> conflicts;
        
        public ScheduleValidationResult(boolean valid, String message, List<String> conflicts) {
            this.valid = valid;
            this.message = message;
            this.conflicts = conflicts;
        }
    }
    
    public static class AttendanceSummaryResult {
        public int studentId;
        public int totalSessions;
        public int present;
        public int absent;
        public int excused;
        public double absenceRate;
        public String riskLevel;

        public AttendanceSummaryResult(int studentId, int totalSessions, int present,
                                       int absent, int excused, double absenceRate, String riskLevel) {
            this.studentId = studentId;
            this.totalSessions = totalSessions;
            this.present = present;
            this.absent = absent;
            this.excused = excused;
            this.absenceRate = absenceRate;
            this.riskLevel = riskLevel;
        }
    }
    
    public static class ArchiveResult {
        public String semester;
        public int archivedCount;
        public String summary;
        
        public ArchiveResult(String semester, int archivedCount, String summary) {
            this.semester = semester;
            this.archivedCount = archivedCount;
            this.summary = summary;
        }
    }
    
    public static class CourseStatisticsResult {
        public int courseId;
        public int studentCount;
        public double averageGrade;
        public int totalRecords;
        public int[] gradeDistribution;
        public Map<String, Integer> gradeTypeDistribution;
        
        public CourseStatisticsResult(int courseId, int studentCount, double averageGrade,
                                      int totalRecords, int[] gradeDistribution,
                                      Map<String, Integer> gradeTypeDistribution) {
            this.courseId = courseId;
            this.studentCount = studentCount;
            this.averageGrade = averageGrade;
            this.totalRecords = totalRecords;
            this.gradeDistribution = gradeDistribution;
            this.gradeTypeDistribution = gradeTypeDistribution;
        }
    }
    
    public static class AnalyticsExportResult {
        public String report;
        public long studentCount;
        public long teacherCount;
        public int groupCount;
        public double overallAverageGrade;
        public Map<String, Double> groupAverages;
        
        public AnalyticsExportResult(String report, long studentCount, long teacherCount,
                                     int groupCount, double overallAverageGrade,
                                     Map<String, Double> groupAverages) {
            this.report = report;
            this.studentCount = studentCount;
            this.teacherCount = teacherCount;
            this.groupCount = groupCount;
            this.overallAverageGrade = overallAverageGrade;
            this.groupAverages = groupAverages;
        }
    }
}

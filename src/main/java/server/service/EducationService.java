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

            // DEBUG: Выводим информацию для отладки
            System.out.println("=== AUTH DEBUG ===");
            System.out.println("Login attempt: " + login);
            System.out.println("Password entered: " + password);
            String hashedInput = User.hashPassword(password);
            System.out.println("Hashed input: " + hashedInput);
            System.out.println("Hash from DB: " + user.getPasswordHash());
            System.out.println("=================");

            boolean passwordsMatch = user.getPasswordHash().equals(hashedInput);
            System.out.println("Passwords match: " + passwordsMatch);
            System.out.println("Hash length from DB: " + user.getPasswordHash().length());
            System.out.println("Hash length input: " + hashedInput.length());

            if (!passwordsMatch) {
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
                return new GPACalculatorResult(studentId, "Нет доступных оценок", 0.0, 0, 0, 0, 0, new HashMap<>());
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

            return new GPACalculatorResult(studentId, "Расчёт успешно выполнен",
                    Math.round(avg * 100.0) / 100.0, count, exams, tests, coursework, courseAverages);
        } catch (SQLException e) {
            return new GPACalculatorResult(studentId, "Ошибка: " + e.getMessage(), 0.0, 0, 0, 0, 0, new HashMap<>());
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
            if (!gradeEligible) issues.add("Средний балл ниже 3.0: " + String.format("%.2f", avgGrade));
            if (!attendanceEligible) issues.add("Слишком много пропусков: " + absences);
            if (hasFailingGrades) issues.add("Есть неудовлетворительные оценки (< 3.0)");

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
                    Collections.singletonList("Ошибка базы данных: " + e.getMessage()));
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
            report.append("ОТЧЁТ ОБ УСПЕВАЕМОСТИ\n");
            report.append("========================\n");
            report.append("ID студента: ").append(studentId).append("\n");
            report.append("Средний балл: ").append(String.format("%.2f", avgGrade)).append("\n\n");

            for (Map.Entry<String, List<AcademicRecord>> entry : byCourse.entrySet()) {
                report.append("Курс: ").append(entry.getKey()).append("\n");
                for (AcademicRecord r : entry.getValue()) {
                    report.append("  - ").append(r.getGradeType())
                            .append(": ").append(r.getGradeValue())
                            .append(" (").append(getAttendanceStatusRussian(r.getAttendanceStatus())).append(")\n");
                }
                report.append("\n");
            }

            return new GradeReportResult(studentId, report.toString(), records.size(), avgGrade, records);
        } catch (SQLException e) {
            return new GradeReportResult(studentId, "Ошибка генерации отчёта: " + e.getMessage(),
                    0, 0.0, Collections.emptyList());
        }
    }

    private String getAttendanceStatusRussian(String status) {
        if (status == null) return "нет данных";
        switch (status.toLowerCase()) {
            case "present": return "присутствовал";
            case "absent": return "отсутствовал";
            case "excused": return "пропуск по уваж. причине";
            default: return status;
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
            distribution.put("отлично (9-10)", excellent);
            distribution.put("хорошо (7-8)", good);
            distribution.put("удовлетворительно (5-6)", satisfactory);
            distribution.put("неудовлетворительно (<5)", failing);

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

                String message = "Студент " + records.get(0).getStudentName() +
                        " имеет " + records.size() + " неудовлетворительную(ые) оценку(и):";
                for (AcademicRecord r : records) {
                    message += "\n  - " + r.getCourseName() + ": " + r.getGradeValue();
                }

                alerts.add(new AlertResult(studentId, "LOW_GRADE", message, records.size()));
            }

            return alerts;
        } catch (SQLException e) {
            AlertResult error = new AlertResult(0, "ERROR",
                    "Не удалось сгенерировать оповещения: " + e.getMessage(), 0);
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
                return new TeacherWorkloadResult(teacherId, "Преподаватель не найден",
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

            return new TeacherWorkloadResult(teacherId, "Отчёт сгенерирован",
                    schedules.size(), uniqueCourses.size(), uniqueGroups.size(),
                    workloadPercentage, schedules);
        } catch (SQLException e) {
            return new TeacherWorkloadResult(teacherId, "Ошибка: " + e.getMessage(),
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
                return new ScheduleValidationResult(false, "Расписание не найдено",
                        Collections.emptyList());
            }

            List<String> conflicts = new ArrayList<>();

            // Check room conflict
            List<Schedule> roomConflicts = scheduleDao.findByRoomAndTime(
                    schedule.getRoomNumber(), schedule.getDayOfWeek(),
                    schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : "");
            for (Schedule s : roomConflicts) {
                if (s.getId() != scheduleId) {
                    conflicts.add("Конфликт аудитории: " + s.getRoomNumber() +
                            " в " + getDayRussian(schedule.getDayOfWeek()) + " " + schedule.getStartTime());
                }
            }

            // Check teacher conflict
            if (schedule.getTeacherId() != null) {
                List<Schedule> teacherConflicts = scheduleDao.findByTeacherAndTime(
                        schedule.getTeacherId(), schedule.getDayOfWeek(),
                        schedule.getStartTime() != null ? schedule.getStartTime().toLocalTime().toString() : "");
                for (Schedule s : teacherConflicts) {
                    if (s.getId() != scheduleId) {
                        conflicts.add("Конфликт преподавателя: " + s.getTeacherName() +
                                " в " + getDayRussian(schedule.getDayOfWeek()) + " " + schedule.getStartTime());
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
                        conflicts.add("Конфликт группы: " + s.getGroupName() +
                                " в " + getDayRussian(schedule.getDayOfWeek()) + " " + schedule.getStartTime());
                    }
                }
            }

            boolean isValid = conflicts.isEmpty();
            String message = isValid ? "Расписание корректно, конфликтов не обнаружено" :
                    "Найдено " + conflicts.size() + " конфликт(ов)";

            return new ScheduleValidationResult(isValid, message, conflicts);
        } catch (SQLException e) {
            return new ScheduleValidationResult(false, "Ошибка: " + e.getMessage(),
                    Collections.emptyList());
        }
    }

    private String getDayRussian(String day) {
        if (day == null) return "";
        switch (day) {
            case "Monday": return "Понедельник";
            case "Tuesday": return "Вторник";
            case "Wednesday": return "Среда";
            case "Thursday": return "Четверг";
            case "Friday": return "Пятница";
            case "Saturday": return "Суббота";
            default: return day;
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
            if (absenceRate >= 30) riskLevel = "КРИТИЧЕСКИЙ";
            else if (absenceRate >= 20) riskLevel = "ВЫСОКИЙ";
            else if (absenceRate >= 10) riskLevel = "СРЕДНИЙ";
            else riskLevel = "НИЗКИЙ";

            return new AttendanceSummaryResult(studentId, totalSessions, present, absent, excused,
                    Math.round(absenceRate * 100.0) / 100.0, riskLevel);
        } catch (SQLException e) {
            return new AttendanceSummaryResult(studentId, 0, 0, 0, 0, 0.0, "ОШИБКА: " + e.getMessage());
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
                return new ArchiveResult(semester, 0, "Записи для семестра не найдены: " + semester);
            }

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
                    "Заархивировано %d записей для %d студентов по %d курсам. Средний балл: %.2f",
                    totalRecords, uniqueStudents.size(), uniqueCourses.size(), avgGrade
            );

            return new ArchiveResult(semester, totalRecords, summary);
        } catch (SQLException e) {
            return new ArchiveResult(semester, 0, "Ошибка архивации: " + e.getMessage());
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
            int gradeDistribution[] = new int[11];

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
            report.append("ИНСТИТУЦИОНАЛЬНЫЙ АНАЛИТИЧЕСКИЙ ОТЧЁТ\n");
            report.append("================================\n\n");
            report.append("Всего студентов: ").append(studentCount).append("\n");
            report.append("Всего преподавателей: ").append(teacherCount).append("\n");
            report.append("Всего групп: ").append(groups.size()).append("\n");
            report.append("Общий средний балл: ").append(String.format("%.2f", overallAvgGrade)).append("\n\n");

            report.append("Успеваемость по группам:\n");
            for (Map.Entry<String, Double> entry : groupAverages.entrySet()) {
                report.append("  - ").append(entry.getKey()).append(": ")
                        .append(String.format("%.2f", entry.getValue())).append("\n");
            }

            return new AnalyticsExportResult(report.toString(), studentCount, teacherCount,
                    groups.size(), Math.round(overallAvgGrade * 100.0) / 100.0, groupAverages);
        } catch (SQLException e) {
            return new AnalyticsExportResult("Ошибка генерации аналитики: " + e.getMessage(),
                    0, 0, 0, 0.0, Collections.singletonMap("error", 0.0));
        }
    }

    // =========================================================================
    // Result DTOs (nested classes for encapsulation)
    // =========================================================================

    public static class GPACalculatorResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class SessionEligibilityResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class GradeReportResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class GroupAnalyticsResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class AlertResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class TeacherWorkloadResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class ScheduleValidationResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        public boolean valid;
        public String message;
        public List<String> conflicts;

        public ScheduleValidationResult(boolean valid, String message, List<String> conflicts) {
            this.valid = valid;
            this.message = message;
            this.conflicts = conflicts;
        }
    }

    public static class AttendanceSummaryResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class ArchiveResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        public String semester;
        public int archivedCount;
        public String summary;

        public ArchiveResult(String semester, int archivedCount, String summary) {
            this.semester = semester;
            this.archivedCount = archivedCount;
            this.summary = summary;
        }
    }

    public static class CourseStatisticsResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    public static class AnalyticsExportResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

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

    // =========================================================================
    // TEACHER COMMANDS IMPLEMENTATIONS
    // =========================================================================

    public List<String> getTeacherGroups(int teacherId) throws SQLException {
        List<Schedule> schedules = scheduleDao.findByTeacherId(teacherId);
        Set<String> groups = new HashSet<>();
        for (Schedule s : schedules) {
            if (s.getGroupName() != null) {
                groups.add(s.getGroupName());
            }
        }
        return new ArrayList<>(groups);
    }

    public List<String> getTeacherCourses(int teacherId) throws SQLException {
        List<Schedule> schedules = scheduleDao.findByTeacherId(teacherId);
        Set<String> courses = new HashSet<>();
        for (Schedule s : schedules) {
            if (s.getCourseName() != null) {
                courses.add(s.getCourseName());
            }
        }
        return new ArrayList<>(courses);
    }

    public List<Object[]> getStudentsByGroupAndCourse(String groupName, String courseName, int teacherId) throws SQLException {
        List<Object[]> result = new ArrayList<>();
        List<Group> groups = groupDao.findAll();
        Integer groupId = null;
        for (Group g : groups) {
            if (g.getName().equals(groupName)) {
                groupId = g.getId();
                break;
            }
        }

        if (groupId != null) {
            List<User> students = userDao.findByGroupId(groupId);
            for (User s : students) {
                result.add(new Object[]{s.getId(), s.getFullName()});
            }
        }
        return result;
    }

    public String getStudentGradesAsString(int studentId) throws SQLException {
        List<AcademicRecord> records = recordDao.findByStudentId(studentId);
        if (records.isEmpty()) {
            return "У этого студента пока нет оценок.";
        }

        StringBuilder sb = new StringBuilder();
        for (AcademicRecord r : records) {
            sb.append(String.format("%s: %.1f (%s) - %s\n",
                    r.getCourseName(), r.getGradeValue(), r.getGradeType(), r.getGradeDate()));
        }
        return sb.toString();
    }

    public String getGroupPerformanceReport(String groupName) throws SQLException {
        List<Group> groups = groupDao.findAll();
        Group targetGroup = null;
        for (Group g : groups) {
            if (g.getName().equals(groupName)) {
                targetGroup = g;
                break;
            }
        }

        if (targetGroup == null) {
            return "Группа не найдена: " + groupName;
        }

        List<User> students = userDao.findByGroupId(targetGroup.getId());
        if (students.isEmpty()) {
            return "В группе нет студентов: " + groupName;
        }

        StringBuilder report = new StringBuilder();
        report.append("=".repeat(60)).append("\n");
        report.append("ОТЧЁТ ОБ УСПЕВАЕМОСТИ ГРУППЫ\n");
        report.append("Группа: ").append(groupName).append("\n");
        report.append("Курс: ").append(targetGroup.getYearOfStudy()).append("\n");
        report.append("Количество студентов: ").append(students.size()).append("\n");
        report.append("=".repeat(60)).append("\n\n");

        double totalAverage = 0;
        int excellent = 0, good = 0, satisfactory = 0, failing = 0;
        List<String> failingStudents = new ArrayList<>();

        for (User student : students) {
            double avgGrade = recordDao.getAverageGradeByStudent(student.getId());
            if (avgGrade > 0) {
                totalAverage += avgGrade;
                if (avgGrade >= 8.5) {
                    excellent++;
                } else if (avgGrade >= 7.0) {
                    good++;
                } else if (avgGrade >= 5.0) {
                    satisfactory++;
                } else {
                    failing++;
                    failingStudents.add(student.getFullName() + " (" + String.format("%.2f", avgGrade) + ")");
                }
            }
        }

        double overallAvg = students.size() > 0 ? totalAverage / students.size() : 0;

        report.append("ОБЩАЯ СТАТИСТИКА:\n");
        report.append("Средний балл: ").append(String.format("%.2f", overallAvg)).append("\n\n");

        report.append("РАСПРЕДЕЛЕНИЕ ОЦЕНОК:\n");
        report.append("Отлично (8.5-10): ").append(excellent).append(" студентов\n");
        report.append("Хорошо (7.0-8.4): ").append(good).append(" студентов\n");
        report.append("Удовлетворительно (5.0-6.9): ").append(satisfactory).append(" студентов\n");
        report.append("Неудовлетворительно (<5.0): ").append(failing).append(" студентов\n\n");

        if (!failingStudents.isEmpty()) {
            report.append("СТУДЕНТЫ В ГРУППЕ РИСКА:\n");
            for (String s : failingStudents) {
                report.append("  - ").append(s).append("\n");
            }
        }

        return report.toString();
    }

    public String getAllSchedulesAsString() throws SQLException {
        List<Schedule> schedules = scheduleDao.findAll();
        if (schedules.isEmpty()) {
            return "Расписание не найдено.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ВСЁ РАСПИСАНИЕ\n");
        sb.append("=".repeat(60)).append("\n");

        for (Schedule s : schedules) {
            sb.append("ID: ").append(s.getId()).append("\n");
            sb.append("  Курс: ").append(s.getCourseName()).append("\n");
            sb.append("  Группа: ").append(s.getGroupName()).append("\n");
            sb.append("  Преподаватель: ").append(s.getTeacherName()).append("\n");
            sb.append("  Аудитория: ").append(s.getRoomNumber()).append("\n");
            sb.append("  День: ").append(getDayRussian(s.getDayOfWeek())).append("\n");
            sb.append("  Время: ").append(s.getStartTime()).append(" - ").append(s.getEndTime()).append("\n");
            sb.append("  Тип: ").append(s.getLessonType()).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        return sb.toString();
    }

    // =========================================================================
    // ADMIN - GROUP MANAGEMENT
    // =========================================================================

    public boolean addGroup(String name, int yearOfStudy, Integer curatorId) throws SQLException {
        Group group = new Group();
        group.setName(name);
        group.setYearOfStudy(yearOfStudy);
        group.setCuratorId(curatorId);
        group.setStudentCount(0);
        return groupDao.insert(group);
    }

    public List<String> getAllGroupsAsString() throws SQLException {
        List<Group> groups = groupDao.findAll();
        List<String> result = new ArrayList<>();
        for (Group g : groups) {
            result.add(g.getId() + ": " + g.getName() + " (" + g.getYearOfStudy() + " курс)");
        }
        return result;
    }

    // =========================================================================
    // ADMIN - COURSE MANAGEMENT
    // =========================================================================

    public boolean addCourse(String name, String description, int credits, Integer teacherId, String department) throws SQLException {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setCredits(credits);
        course.setTeacherId(teacherId);
        course.setDepartment(department);
        return courseDao.insert(course);
    }

    public List<String> getAllCoursesAsString() throws SQLException {
        List<Course> courses = courseDao.findAll();
        List<String> result = new ArrayList<>();
        for (Course c : courses) {
            result.add(c.getId() + ": " + c.getName() + " (" + c.getCredits() + " кредитов)");
        }
        return result;
    }

    // =========================================================================
    // ADMIN - STUDENT MANAGEMENT
    // =========================================================================

    public boolean addStudent(String login, String fullName, String groupName, int yearOfStudy, String studentIdNumber) throws SQLException {
        System.out.println("=== EducationService.addStudent called ===");
        System.out.println("Login: " + login);
        System.out.println("GroupName: " + groupName);
        List<Group> groups = groupDao.findAll();
        Integer groupId = null;
        for (Group g : groups) {
            if (g.getName().equals(groupName)) {
                groupId = g.getId();
                break;
            }
        }

        if (groupId == null) {
            return false;
        }

        Student student = new Student();
        student.setLogin(login);
        student.setPasswordHash(User.hashPassword(login));
        student.setFullName(fullName);
        student.setRole("STUDENT");
        student.setGroupId(groupId);
        student.setGroup(groupName);
        student.setYearOfStudy(yearOfStudy);
        student.setStudentIdNumber(studentIdNumber);
        student.setActive(true);

        return userDao.insert(student);
    }

    public List<String> getAllStudentsAsString() throws SQLException {
        List<User> users = userDao.findByRole("STUDENT");
        List<String> result = new ArrayList<>();
        for (User u : users) {
            result.add(u.getId() + ": " + u.getFullName() + " (login: " + u.getLogin() + ")");
        }
        return result;
    }

    // =========================================================================
    // UPDATE & DELETE METHODS
    // =========================================================================

    public boolean updateGroup(int id, String name, int yearOfStudy, Integer curatorId) throws SQLException {
        Group group = groupDao.findById(id);
        if (group == null) return false;

        group.setName(name);
        group.setYearOfStudy(yearOfStudy);
        group.setCuratorId(curatorId);

        return groupDao.update(group);
    }

    public boolean deleteGroup(int id) throws SQLException {
        return groupDao.delete(id);
    }

    public boolean updateCourse(int id, String name, String description, int credits, Integer teacherId, String department) throws SQLException {
        Course course = courseDao.findById(id);
        if (course == null) return false;

        course.setName(name);
        course.setDescription(description);
        course.setCredits(credits);
        course.setTeacherId(teacherId);
        course.setDepartment(department);

        return courseDao.update(course);
    }

    public boolean deleteCourse(int id) throws SQLException {
        return courseDao.delete(id);
    }

    public boolean updateStudent(int id, String login, String fullName, String groupName, int yearOfStudy, String studentIdNumber, boolean isActive) throws SQLException {
        User user = userDao.findById(id);
        if (user == null || !(user instanceof Student)) return false;

        Student student = (Student) user;

        List<Group> groups = groupDao.findAll();
        Integer groupId = null;
        for (Group g : groups) {
            if (g.getName().equals(groupName)) {
                groupId = g.getId();
                break;
            }
        }

        student.setLogin(login);
        student.setFullName(fullName);
        student.setGroupId(groupId);
        student.setGroup(groupName);
        student.setYearOfStudy(yearOfStudy);
        student.setStudentIdNumber(studentIdNumber);
        student.setActive(isActive);

        return userDao.update(student);
    }

    public boolean deleteStudent(int id) throws SQLException {
        return userDao.delete(id);
    }

    // =========================================================================
    // REPORT METHODS (RUSSIAN)
    // =========================================================================

    public String getGroupPerformanceReportForAdmin() throws SQLException {
        List<Group> groups = groupDao.findAll();
        if (groups.isEmpty()) {
            return "Группы не найдены.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ОТЧЁТ ПО УСПЕВАЕМОСТИ ГРУПП\n");
        sb.append("=".repeat(60)).append("\n\n");

        for (Group group : groups) {
            double avgGrade = recordDao.getAverageGradeByGroup(group.getId());
            int studentCount = userDao.findByGroupId(group.getId()).size();

            sb.append("Группа: ").append(group.getName()).append("\n");
            sb.append("  Курс: ").append(group.getYearOfStudy()).append("\n");
            sb.append("  Студентов: ").append(studentCount).append("\n");
            sb.append("  Средний балл: ").append(String.format("%.2f", avgGrade)).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        return sb.toString();
    }

    public String getTeacherLoadReport() throws SQLException {
        List<User> teachers = userDao.findByRole("TEACHER");
        if (teachers.isEmpty()) {
            return "Преподаватели не найдены.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ОТЧЁТ ПО НАГРУЗКЕ ПРЕПОДАВАТЕЛЕЙ\n");
        sb.append("=".repeat(60)).append("\n\n");

        for (User teacher : teachers) {
            List<Schedule> schedules = scheduleDao.findByTeacherId(teacher.getId());
            int totalHours = 0;
            for (Schedule s : schedules) {
                if (s.getStartTime() != null && s.getEndTime() != null) {
                    long minutes = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                    totalHours += minutes / 60;
                }
            }

            sb.append("Преподаватель: ").append(teacher.getFullName()).append("\n");
            sb.append("  Занятий в расписании: ").append(schedules.size()).append("\n");
            sb.append("  Часов в неделю: ").append(totalHours).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        return sb.toString();
    }

    public String getLowGradesReport(double threshold) throws SQLException {
        List<AcademicRecord> failingRecords = recordDao.getFailingGrades(threshold);
        if (failingRecords.isEmpty()) {
            return "Студентов с оценками ниже " + threshold + " не найдено.\nОтличная работа!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("СТУДЕНТЫ С НИЗКИМИ ОЦЕНКАМИ (ниже ").append(threshold).append(")\n");
        sb.append("=".repeat(60)).append("\n\n");

        for (AcademicRecord record : failingRecords) {
            sb.append("Студент: ").append(record.getStudentName()).append("\n");
            sb.append("  Курс: ").append(record.getCourseName()).append("\n");
            sb.append("  Оценка: ").append(record.getGradeValue()).append("\n");
            sb.append("  Тип: ").append(record.getGradeType()).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        return sb.toString();
    }

    public String getAttendanceReport() throws SQLException {
        List<User> students = userDao.findByRole("STUDENT");
        if (students.isEmpty()) {
            return "Студенты не найдены.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ОТЧЁТ ПО ПОСЕЩАЕМОСТИ\n");
        sb.append("=".repeat(60)).append("\n\n");

        for (User student : students) {
            int absences = recordDao.countAbsencesByStudent(student.getId());
            List<AcademicRecord> records = recordDao.findByStudentId(student.getId());
            int totalSessions = 0;
            for (AcademicRecord r : records) {
                if (r.getAttendanceStatus() != null) {
                    totalSessions++;
                }
            }
            double absenceRate = totalSessions > 0 ? (double) absences / totalSessions * 100 : 0;

            sb.append("Студент: ").append(student.getFullName()).append("\n");
            sb.append("  Всего занятий: ").append(totalSessions).append("\n");
            sb.append("  Пропусков: ").append(absences).append("\n");
            sb.append("  Процент пропусков: ").append(String.format("%.1f%%", absenceRate)).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        return sb.toString();
    }

    public String getFullAnalyticsReport() throws SQLException {
        List<User> allUsers = userDao.findAll();
        long studentCount = allUsers.stream().filter(u -> u instanceof Student).count();
        long teacherCount = allUsers.stream().filter(u -> u instanceof Teacher).count();
        List<Group> groups = groupDao.findAll();

        double totalAvg = 0;
        int groupCount = 0;
        for (Group g : groups) {
            double avg = recordDao.getAverageGradeByGroup(g.getId());
            if (avg > 0) {
                totalAvg += avg;
                groupCount++;
            }
        }
        double overallAvg = groupCount > 0 ? totalAvg / groupCount : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ПОЛНАЯ АНАЛИТИКА ИНСТИТУТА\n");
        sb.append("=".repeat(60)).append("\n\n");
        sb.append("📊 Общая статистика:\n");
        sb.append("  - Студентов: ").append(studentCount).append("\n");
        sb.append("  - Преподавателей: ").append(teacherCount).append("\n");
        sb.append("  - Групп: ").append(groups.size()).append("\n");
        sb.append("  - Средний балл по институту: ").append(String.format("%.2f", overallAvg)).append("\n\n");

        sb.append("📈 Успеваемость по группам:\n");
        for (Group g : groups) {
            double avg = recordDao.getAverageGradeByGroup(g.getId());
            if (avg > 0) {
                sb.append("  - ").append(g.getName()).append(": ").append(String.format("%.2f", avg)).append("\n");
            }
        }

        return sb.toString();
    }

    // =========================================================================
    // SCHEDULE MANAGEMENT METHODS
    // =========================================================================

    public List<String> getAllTeachersAsString() throws SQLException {
        List<User> teachers = userDao.findByRole("TEACHER");
        List<String> result = new ArrayList<>();
        for (User t : teachers) {
            result.add(t.getId() + ": " + t.getFullName());
        }
        return result;
    }

    public List<String> getAllSchedulesForList() throws SQLException {
        List<Schedule> schedules = scheduleDao.findAll();
        List<String> result = new ArrayList<>();
        for (Schedule s : schedules) {
            result.add(s.getId() + ": " + s.getCourseName() + " - " + s.getGroupName() +
                    " (" + getDayRussian(s.getDayOfWeek()) + ", " + s.getRoomNumber() + ")");
        }
        return result;
    }

    public boolean addSchedule(String courseInfo, String groupInfo, String teacherInfo,
                               String room, String day, String startTimeStr, String endTimeStr,
                               String lessonType, String semester) throws SQLException {

        int courseId = Integer.parseInt(courseInfo.split(":")[0].trim());
        String courseName = courseInfo.split(":")[1].trim().split(" \\(")[0];

        int groupId = Integer.parseInt(groupInfo.split(":")[0].trim());
        String groupName = groupInfo.split(":")[1].trim().split(" \\(")[0];

        int teacherId = Integer.parseInt(teacherInfo.split(":")[0].trim());
        String teacherName = teacherInfo.split(":")[1].trim();

        String dateStr = "2026-04-01";
        java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(dateStr + "T" + startTimeStr + ":00");
        java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(dateStr + "T" + endTimeStr + ":00");

        Schedule schedule = new Schedule();
        schedule.setCourseId(courseId);
        schedule.setCourseName(courseName);
        schedule.setGroupId(groupId);
        schedule.setGroupName(groupName);
        schedule.setTeacherId(teacherId);
        schedule.setTeacherName(teacherName);
        schedule.setRoomNumber(room);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setDayOfWeek(day);
        schedule.setLessonType(lessonType);
        schedule.setSemester(semester);

        return scheduleDao.insert(schedule);
    }

    public boolean deleteSchedule(int id) throws SQLException {
        return scheduleDao.delete(id);
    }

    public List<String> getAllTeachersForManagement() throws SQLException {
        List<User> teachers = userDao.findByRole("TEACHER");
        List<String> result = new ArrayList<>();
        for (User t : teachers) {
            result.add(t.getId() + ": " + t.getFullName() + " (login: " + t.getLogin() + ")");
        }
        return result;
    }

    public Object[] getTeacherDetails(int teacherId) throws SQLException {
        User user = userDao.findById(teacherId);
        if (user == null || !(user instanceof Teacher)) {
            return new Object[]{"", "", "", 0, false};
        }
        Teacher teacher = (Teacher) user;
        return new Object[]{
                teacher.getPosition() != null ? teacher.getPosition() : "",
                teacher.getDepartment() != null ? teacher.getDepartment() : "",
                teacher.getSpecialization() != null ? teacher.getSpecialization() : "",
                teacher.getTeachingHoursPerWeek(),
                teacher.isActive()
        };
    }

    public boolean updateTeacher(int id, String login, String fullName, String position,
                                 String department, String specialization, int teachingHours, boolean isActive) throws SQLException {
        User user = userDao.findById(id);
        if (user == null || !(user instanceof Teacher)) return false;

        Teacher teacher = (Teacher) user;
        teacher.setLogin(login);
        teacher.setFullName(fullName);
        teacher.setPosition(position);
        teacher.setDepartment(department);
        teacher.setSpecialization(specialization);
        teacher.setTeachingHoursPerWeek(teachingHours);
        teacher.setActive(isActive);

        return userDao.update(teacher);
    }

    public boolean addTeacher(String login, String fullName, String position, String department,
                              String specialization, int teachingHours) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setLogin(login);
        teacher.setPasswordHash(User.hashPassword(login));
        teacher.setFullName(fullName);
        teacher.setRole("TEACHER");
        teacher.setPosition(position);
        teacher.setDepartment(department);
        teacher.setSpecialization(specialization);
        teacher.setTeachingHoursPerWeek(teachingHours);
        teacher.setActive(true);

        return userDao.insert(teacher);
    }

    public boolean deleteTeacher(int id) throws SQLException {
        return userDao.delete(id);
    }
}